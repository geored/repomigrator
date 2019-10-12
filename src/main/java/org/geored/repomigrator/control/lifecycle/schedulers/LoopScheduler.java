package org.geored.repomigrator.control.lifecycle.schedulers;

import io.quarkus.scheduler.Scheduled;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.geored.repomigrator.control.lifecycle.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;

@ApplicationScoped
public class LoopScheduler {

	@Inject
	RemoteRepositoriesCache cache;
	
	@Inject
	@ConfigProperty(name="redhat.sso.authorization")
	private String auth;
	
	@Inject
	@ConfigProperty(name="redhat.indy.dev.env")
	private String url;
	
	static final Logger logger = Logger.getLogger(LoopScheduler.class.getName());

	@PostConstruct
	public void init() {
		if (cache.getRepos().isEmpty()) {
			getRestClient(url).getRemoteRepos(auth).get("items")
			  .forEach((repo) -> {
				  cache.setRepo(repo.getName(), repo);
			  });

			cache.getRepos().values().stream()
			  .forEach(repo -> System.out.println(repo.getUrl()));
		} else {
			cache.getRepos().values().stream().forEach(repo -> logger.log(Level.INFO, "Repo URL: {0}" , repo.getUrl()));
		}
	}

	@Scheduled(every = "10s")
	public void increase() throws URISyntaxException {
		logger.log(Level.INFO, "{0}", LocalDateTime.now());
		
		BrowsedStore bes = getRestClient(url).browseEndpointStores("npm", "group", "build-81", auth);
		logger.log(Level.INFO, "base url: {0} \n content url: {1} \n listings: {2} \n sources: {3} \n browse url: {4} \n store key: {5}", 
		  new Object[] { 
			  bes.getBaseBrowseUrl(), 
			  bes.getBaseContentUrl(), 
			  bes.getListingUrls().stream().map(l -> l.getListingUrl()).collect(Collectors.toList()),
			  bes.getSources() , 
			  bes.getStoreBrowseUrl(),
			  bes.getStoreKey() 
		  });
	}

	private RemoteRepositoriesService getRestClient(String url) {
		RemoteRepositoriesService restClient = null;
		try {
			restClient = RestClientBuilder.newBuilder().baseUrl(new URL(url)).build(RemoteRepositoriesService.class);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, " | MailformedURLException | {0}", LocalDateTime.now());
		}
		return restClient;
	}
}
