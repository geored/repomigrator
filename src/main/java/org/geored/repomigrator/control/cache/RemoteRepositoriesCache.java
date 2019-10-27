package org.geored.repomigrator.control.cache;

import io.reactivex.Flowable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.lifecycle.CacheLifecycle;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.control.lifecycle.events.ProcessMigrationEvent;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.RemoteRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RemoteRepositoriesCache implements Serializable {

	Logger logger = Logger.getLogger(this.getClass().getName());
	
	// Process Migration Events Cache
	public static Map<String , ProcessMigrationEvent> migrationEvents = new ConcurrentHashMap<>();
	
	// Remote Repositories Cache
	public static Map<String, RemoteRepository> remoteRepos = new ConcurrentHashMap<>();
	
	// Browsed Remote Repositories Cache
	public static Map<String, BrowsedStore> browsedRepos = new ConcurrentHashMap<>();

	public static Queue<BrowsedStore> browsedStoreList = new ConcurrentLinkedQueue<BrowsedStore>();

	public static Flowable<BrowsedStore> browsedStoreFlowable = Flowable.fromIterable(browsedStoreList);

	public static List<URL> urlList = Collections.synchronizedList(new ArrayList<>());

	public static Flowable<URL> urlsFlow = Flowable.fromIterable(urlList);


	// Listing Urls Cache
	public static Map<String, String> listedUrls = new ConcurrentHashMap<>();

	public static List<BrowsedStore> browsedStores = new ArrayList<>();

	public static List<String> contentUrls = new ArrayList<>();

	@Inject
	@ConfigProperty(name = "redhat.indy.cache.rrc")
	String fileName;
	
	@Inject
	@ConfigProperty(name="redhat.sso.authorization")
	String auth;
	
	@Inject
	@ConfigProperty(name="redhat.indy.dev.env")
	String url;
	
	@Inject Event<ProcessLifecycle> processLifecycle;
	@Inject Event<CacheLifecycle> cacheLifecycle;

	@PostConstruct
	public void init() {
		cacheLifecycle.fire(CacheLifecycle.LOADING);
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (SecurityException | IOException e) {
				logger.log(Level.WARNING, "[[CREATE]] IOException | {0}",e.getMessage());
			}
		}
		if(file.length()>0) {
			
			try (ObjectInputStream inObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
				logger.log(Level.INFO,"\n [[READING.FROM.FILE]] {0}",file.getCanonicalPath());
				remoteRepos = (Map<String, RemoteRepository>) inObject.readObject();
				cacheLifecycle.fire(CacheLifecycle.LOADED);
			} 
			catch (FileNotFoundException ffe) {logger.log(Level.WARNING, "[[READING]] FileNotFound | {0}",ffe.getMessage());}
			catch (IOException | ClassNotFoundException e) {logger.log(Level.WARNING, "[[READING]] IOException | {0}",e.getMessage());}
		} else {
			logger.log(Level.INFO,"[[READING.FROM.REST.CLIENT.URL]] {0}", url);
			
			RemoteRepositoriesService restClient = getRestClient(url);
			
			restClient.getRemoteRepos("maven").get("items")
			  .stream()
//			  .filter(repo -> !repo.getDisabled())
			  .filter(repo -> { 
				  logger.log(Level.INFO,"\n [[FILTER]] {0} protocol:{1} disabled:{2}" ,new Object[] { repo.getUrl() , getUrl(url).getProtocol(), repo.getDisabled() });
				  return getUrl(repo.getUrl()) != null && ( getUrl(repo.getUrl()).getProtocol().equalsIgnoreCase("http") || repo.getDisabled());
			  })
			  .forEach((repo) -> { setRepo(repo.getName(), repo);logger.log(Level.INFO,"\n [[ADDED]] {0}",repo.getUrl()); });
			
			
			restClient.getRemoteRepos("npm").get("items")
			  .stream()
//			  .filter(repo -> !repo.getDisabled())
			  .filter(repo -> {
				  logger.log(Level.INFO,"\n [[FILTER]] {0} protocol:{1} disabled:{2}" ,new Object[] { repo.getUrl() , getUrl(url).getProtocol(), repo.getDisabled() });
				  return getUrl(repo.getUrl()) != null && ( getUrl(repo.getUrl()).getProtocol().equalsIgnoreCase("http") || repo.getDisabled());
			  })
			  .forEach((repo) -> { setRepo(repo.getName(), repo); logger.log(Level.INFO,"\n [[ADDED]] {0}",repo.getUrl()); })
			  ;
			cacheLifecycle.fire(CacheLifecycle.LOADED);
		}
		
		//cacheLifecycle.fire(CacheLifecycle.LOADED);
		processLifecycle.fire(ProcessLifecycle.START);
	}

	@PreDestroy
	public void destroy() {
		// serialize
		try {
			try (ObjectOutputStream outObject = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
				outObject.writeObject(remoteRepos);
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING, "[[WRITING]] IOException | {0}", ex.getMessage());
		}

	}

	public List<URL> getListingsUrls() {
		return urlList;
	}

	public static Flowable<URL> getUrlsFlow() {
		return urlsFlow;
	}

	public static void setUrlsFlow(Flowable<URL> urlsFlowable) {
		RemoteRepositoriesCache.urlsFlow = urlsFlowable;
	}


	public static Flowable<BrowsedStore> getBrowsedStoreFlowable() {
		return browsedStoreFlowable;
	}

	public static void setBrowsedStoreFlowable(Flowable<BrowsedStore> browsedStoreFlowable) {
		RemoteRepositoriesCache.browsedStoreFlowable = browsedStoreFlowable;
	}

	public static List<BrowsedStore> getBrowsedStores() {
		return browsedStores;
	}

	public static void setBrowsedStores(List<BrowsedStore> browsedStores) {
		RemoteRepositoriesCache.browsedStores = browsedStores;
	}

	public static List<String> getContentUrls() {
		return contentUrls;
	}

	public static void setContentUrls(List<String> contentUrls) {
		RemoteRepositoriesCache.contentUrls = contentUrls;
	}

	public static void addContentUrl(String url) {
		RemoteRepositoriesCache.contentUrls.add(url);
	}

	public static Map<String, String> getListedUrls() {
		return listedUrls;
	}

	public static void setListedUrls(Map<String, String> listedUrls) {
		RemoteRepositoriesCache.listedUrls = listedUrls;
	}

	public static void setListedUrl(String k, String v) {
		RemoteRepositoriesCache.listedUrls.put(k, v);
	}

	public Map<String, RemoteRepository> getRemoteRepos() {
		return remoteRepos;
	}

	public void setRepo(String repoName, RemoteRepository repository) {
		remoteRepos.put(repoName, repository);
	}
	
	RemoteRepositoriesService getRestClient(String url) {
		RemoteRepositoriesService restClient = null;
		try {
			restClient = RestClientBuilder.newBuilder()
			  .baseUrl(new URL(url)).build(RemoteRepositoriesService.class);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "[[REST.CLIENT.BUILDER]] MailformedURLException | {0}", ex.getMessage());
		}
		return restClient;
	}
	
	RestClientBuilder getRestClientBuilder(String url) {
		try {
			return RestClientBuilder.newBuilder().baseUrl(new URL(url));
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "[[REST.CLIENT.BUILDER]] MailformedURLException | {0}", ex.getMessage());
		}
		return null;
	}
	
	URL getUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "[[REST.CLIENT.FILTER]] MailformedURLException | {0}", ex.getMessage());
		}
		return null;
	}
	
}
