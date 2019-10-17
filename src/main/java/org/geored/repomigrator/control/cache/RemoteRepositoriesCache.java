package org.geored.repomigrator.control.cache;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.certs.PemReader;
import org.geored.repomigrator.control.lifecycle.CacheLifecycle;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.control.lifecycle.events.ProcessMigrationEvent;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class RemoteRepositoriesCache implements Serializable {

	Logger logger = Logger.getLogger(this.getClass().getName());
	
	// Process Migration Events Cache
	public static Map<String , ProcessMigrationEvent> migrationEvents = new ConcurrentHashMap<>();
	
	// Remote Repositories Cache
	public static Map<String, RemoteRepository> remoteRepos = new ConcurrentHashMap<>();
	
	// Browsed Remote Repositories Cache
	public static Map<String, BrowsedStore> browsedRepos = new ConcurrentHashMap<>();

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
			
			restClient.getRemoteRepos("maven", auth).get("items")
			  .stream()
//			  .filter(repo -> !repo.getDisabled())
			  .filter(repo -> { 
				  logger.log(Level.INFO,"\n [[FILTER]] {0} protocol:{1} disabled:{2}" ,new Object[] { repo.getUrl() , getUrl(url).getProtocol(), repo.getDisabled() });
				  return getUrl(repo.getUrl()) != null && ( getUrl(repo.getUrl()).getProtocol().equalsIgnoreCase("http") || repo.getDisabled());
			  })
			  .forEach((repo) -> { setRepo(repo.getName(), repo);logger.log(Level.INFO,"\n [[ADDED]] {0}",repo.getUrl()); });
			
			
			restClient.getRemoteRepos("npm", auth).get("items")
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
