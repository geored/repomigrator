package org.geored.repomigrator.control.schedulers;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.certs.PemReader;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.entity.ListingUrls;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class LoopScheduler {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@Inject
	@RestClient
	RemoteRepositoriesService repositoriesService;

	@Inject
	RemoteRepositoriesCache cache;

	@Inject
	@ConfigProperty(name = "redhat.sso.authorization")
	String auth;

	@Inject
	@ConfigProperty(name = "redhat.indy.dev.env")
	String url;

	@Inject
	Event<ProcessLifecycle> processEvent;

	@Scheduled(every = "20s")
	public void increase(ScheduledExecution se) throws URISyntaxException, IOException, GeneralSecurityException {

		logger.log(Level.INFO, "-- Cache Size: {0}", cache.getRemoteRepos().size());

		Iterator<RemoteRepository> iterator = cache.getRemoteRepos().values().iterator();
		
		while (iterator.hasNext()) {

			try {

				RemoteRepository remoteRepository = iterator.next();
				logger.log(Level.INFO, "=> Processing Store: {0}", remoteRepository.getName());
//			
//
//				CompletionStage<BrowsedStore> browseDirectoryAsync = repositoriesService
//				  .browseDirectoryAsync(remoteRepository.getPackageType(), remoteRepository.getType(), remoteRepository.getName(), auth);
				
				
				CompletionStage<BrowsedStore> browseDirectoryAsync = 
				  getRestClient(url)
					.browseDirectoryAsync(remoteRepository.getPackageType(), remoteRepository.getType(), remoteRepository.getName(), auth);
				
				
				browseDirectoryAsync.thenApply(browsedStore -> {
					logger.log(Level.INFO, "----> Browsed Store {0}", browsedStore);
					
					List<ListingUrls> listingUrls = browsedStore.getListingUrls();
					listingUrls.stream().forEach(lu -> {
						RemoteRepositoriesService restClient = getRestClient(lu.getListingUrl());
						restClient.browseDirectoryAsync(remoteRepository.getPackageType(), remoteRepository.getType(), remoteRepository.getName(), auth)
							.thenAccept(ac -> {
								logger.log(Level.INFO, "[Level.DOWN] Browsed Store {0}", ac);
							  });
					});
					
					return listingUrls;
				});
				browseDirectoryAsync.toCompletableFuture().join();
				
//				BrowsedStore browsedStore = browseDirectoryAsync.toCompletableFuture().get();
//				
//				browsedStore.getListingUrls().stream().forEach(bs -> {
//					
//					RemoteRepositoriesService restClient = getRestClient(bs.getListingUrl());
//					restClient.browseDirectoryAsync(remoteRepository.getPackageType(), remoteRepository.getType(), remoteRepository.getName(), auth)
//					  .thenAcceptAsync(bsr -> logger.log(Level.INFO,"=> Browsed Store: {0}", bsr));
//				});
//				

			} catch (Exception e) {
				logger.log(Level.WARNING, " | BrowsedStoreException | {0}", e.getMessage());
			}

//			if(remoteRepository.getServerCertificatePem() != null 
//			  && !remoteRepository.getServerCertificatePem().isEmpty()) {
//				
//				if(getRestClientBuilder(url) != null) {
//					RestClientBuilder restClientBuilder = getRestClientBuilder(url);
//					restClientBuilder.trustStore(PemReader.loadTrustStore(remoteRepository.getServerCertificatePem()));
//					
//					BrowsedStore browsedStore = restClientBuilder
//					  .build(RemoteRepositoriesService.class)
//					  .browseEndpointStores(
//						remoteRepository.getPackageType(),
//						remoteRepository.getType(), 
//						remoteRepository.getName(), auth);
//					logger.log(Level.INFO,"-- Returned Result: {0}" , browsedStore);
//				}
//			} else {
//				String testUrl = "https://test.som";
//				BrowsedStore browsedStore = getRestClient(url)
//				  .browseEndpointStores(
//					remoteRepository.getPackageType(),
//					remoteRepository.getType(),
//					remoteRepository.getName(), auth);
//				logger.log(Level.INFO,"-- Returned Result: {0}" , browsedStore);
//			}	
		}
	}

	RemoteRepositoriesService getRestClient(String url) {
		RemoteRepositoriesService restClient = null;
		try {
			restClient = RestClientBuilder.newBuilder()
			  .baseUrl(new URL(url)).build(RemoteRepositoriesService.class);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, " | MailformedURLException | {0}", LocalDateTime.now());
		}
		return restClient;
	}

	RestClientBuilder getRestClientBuilder(String url) {
		try {
			return RestClientBuilder.newBuilder().baseUrl(new URL(url));
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "!!! MailformedURLException | {0}", ex.getMessage());
		}
		return null;
	}

	List<BrowsedStore> getBrowsedStores() {
		return cache
		  .getRemoteRepos()
		  .values().stream()
		  .map(repo -> getRestClient(url).browseEndpointStores(repo.getPackageType(), repo.getType(), repo.getName(), auth))
		  .collect(Collectors.toList());
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
