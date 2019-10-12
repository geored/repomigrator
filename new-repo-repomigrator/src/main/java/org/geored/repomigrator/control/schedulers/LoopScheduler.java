package org.geored.repomigrator.control.schedulers;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class LoopScheduler {
	
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Inject
	RemoteRepositoriesCache cache;
	
	@Inject
	@ConfigProperty(name="redhat.sso.authorization")
	String auth;
	
	@Inject
	@ConfigProperty(name="redhat.indy.dev.env")
	String url;
	
	@Inject Event<ProcessLifecycle> processEvent;
	
	@PostConstruct
	public void init() {
		logger.log(Level.INFO,"{0}",cache.getRemoteRepos().size());
		if (cache.getRemoteRepos().isEmpty()) {
			RemoteRepositoriesService restClient = getRestClient(url);
			restClient.getRemoteRepos("maven", auth).get("items")
			  .stream()
			  .filter(repo -> !repo.getDisabled())
			  .forEach((repo) -> { cache.setRepo(repo.getName(), repo); });
			
			
			restClient.getRemoteRepos("npm", auth).get("items")
			  .stream()
			  .filter(repo -> !repo.getDisabled())
			  .forEach((repo) -> { cache.setRepo(repo.getName(), repo); });
//			restClient.getRemoteRepos("generic-http", auth).get("items").stream().distinct().forEach((repo) -> { cache.setRepo(repo.getName(), repo); });
			
		} else {
			logger.log(Level.INFO,"# of remote repos {0}" , cache.getRemoteRepos().size());
		}
	}

	@Scheduled(every = "10s")
	public void increase(ScheduledExecution se) throws URISyntaxException {
//		logger.log(Level.INFO, "{0}", cache.getRemoteRepos());
//		
		Iterator<RemoteRepository> iterator = cache.getRemoteRepos().values().iterator();
		
		while (iterator.hasNext()) {
			RemoteRepository next = iterator.next();
			logger.log(Level.INFO, "=> Processing Store: {0}" , next);
			BrowsedStore browsedStore = getRestClient(url).browseEndpointStores(next.getPackageType(), next.getType(), next.getName(), auth);
			logger.log(Level.INFO,"-- Returned Result: {0}" , browsedStore);
		}
		
		// Check Process Lifecycle state [ if it is RUNNING or WAITING pass , if it is START or STOP raise event ]	

	}

	RemoteRepositoriesService getRestClient(String url) {
		RemoteRepositoriesService restClient = null;
		try {
			restClient = RestClientBuilder.newBuilder()
			  .property("", "")
			  
			  .baseUrl(new URL(url)).build(RemoteRepositoriesService.class);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, " | MailformedURLException | {0}", LocalDateTime.now());
		}
		return restClient;
	}
	
	List<BrowsedStore> getBrowsedStores() {
		return 
		  cache
			.getRemoteRepos()
			.values().stream()
			.map(repo -> getRestClient(url).browseEndpointStores(repo.getPackageType(), repo.getType(), repo.getName(), auth))
			.collect(Collectors.toList())
			;
	}
}


//		browsedStores.stream().forEach(System.out::println);
//		BrowsedStore bes = getRestClient(url).browseEndpointStores("npm", "group", "build-81", auth);
//		logger.log(Level.INFO, "\nbase url: {0} \n content url: {1} \n listings: {2} \n sources: {3} \n browse url: {4} \n store key: {5}",
//		  new Object[] {
//			  bes.getBaseBrowseUrl(),
//			  bes.getBaseContentUrl(), 
//			  bes.getListingUrls().stream().map(l -> l.getListingUrl()).collect(Collectors.toList()),
//			  bes.getSources() , 
//			  bes.getStoreBrowseUrl(),
//			  bes.getStoreKey() 
//		  });