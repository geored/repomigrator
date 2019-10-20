package org.geored.repomigrator.control.schedulers;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.control.KnownConstants;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.ListingUrls;
import org.geored.repomigrator.entity.RemoteRepository;

import static org.geored.repomigrator.control.KnownConstants.AUTHORIZATION;

@ApplicationScoped
public class LoopScheduler {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@Inject
	@RestClient
	RemoteRepositoriesService repositoriesService;

	@Inject RemoteRepositoriesCache cache;

	@Inject
	@ConfigProperty(name = "redhat.sso.authorization")
	String auth;

	@Inject
	@ConfigProperty(name = "redhat.indy.dev.env")
	String url;

	@Inject Event<ProcessLifecycle> processEvent;

    @Resource()
    ManagedExecutorService mes;


    @Scheduled(every = "10000s")
	public void increase(ScheduledExecution se) {
		logger.log(Level.INFO, "[[CACHE.SIZE]] {0}", cache.getRemoteRepos().size());

		getRemoteRepositories()

			.thenApply(this::getItemsFrom)

			.thenApply(this::filterDisabledRemoteStores)

//		.thenApply(el -> printElements(el))

			.thenCompose(this::getBrowsedStores)

//			.thenApply(el -> printElements(el))


//			.whenComplete((el,t) -> processForContent(el))

//			.thenCompose(this::processForContent)



			.exceptionally((throwable) -> {

					logger.log(Level.WARNING, "[[Exception.STREAM]]: {0}", throwable.getMessage());

				return null;
			})
		;
	}

	// Functional Methods
	public List<RemoteRepository> filterDisabledRemoteStores(List<RemoteRepository> repos) {
    	logger.info(">>>>> Filtering RemoteRepositories");
    	return repos.stream().filter(repo -> !repo.getDisabled()).collect(Collectors.toList());
	}

	public List<RemoteRepository> getItemsFrom(Map<String, List<RemoteRepository>> items) {
		return items.get("items");
	}

	public List<RemoteRepository> printElements(List<RemoteRepository> le) {
		for(RemoteRepository bs : le) {
			logger.log(Level.INFO, "Repo: {0} , Disabled: {1}", new Object[] {bs.getName(),bs.getDisabled()});
		}
		return le;
	}

	public BrowsedStore processBrowsedStore(BrowsedStore store) {

		logger.log(Level.INFO, "[[PROCESSING.STORE]] {0}", store.getStoreKey());
		if(Objects.isNull( store) ) {
			return new BrowsedStore();
		}
		List<ListingUrls> newListingUrls =
			store.getListingUrls()
				.parallelStream()
				.map(lu -> {
					String previousPathUrl = lu.getListingUrl().split(lu.getPath())[0];
					int index = store.getListingUrls().indexOf(lu);
					String fetchedUrl = recursiveFetchContentUrl(lu.getListingUrl(), previousPathUrl,index);
					lu.setContentUrl(fetchedUrl);
					return lu;
				})
//					.collect(Collectors.toCollection(() -> store.getListingUrls()))
					.collect(Collectors.toList())
		;
		cache.getBrowsedStores().add(store);
//			store.getListingUrls().clear();
//			store.getListingUrls().addAll(newListingUrls);
		return store;
	}

	public boolean checkMalformedUrl(String url) {
		try {
			URL malUrl = new URL(url);
			malUrl.toURI();
		} catch (MalformedURLException | URISyntaxException e) {
			logger.log(Level.INFO, "[[MAILFORMED.URL]] {0}",url);
			return false;
		}
		return true;
	}

	public String recursiveFetchContentUrl(String rS , String pS,int index) {

		if(rS != null && !rS.endsWith("/")) {
			logger.log(Level.INFO, "[[CONTENT.URL]] {0}", rS);
			cache.getContentUrls().add(rS);
			return rS;
		}
//		logger.log(Level.INFO, "[[CLIENT.URL]] {0}", rS);

		ClientBuilder.newClient().target(url)
			.request(MediaType.APPLICATION_JSON)
			.header(AUTHORIZATION, auth)
			.rx()
			.get(Response.class)
			.whenCompleteAsync(((response, throwable) -> {
				if(throwable != null) {
					logger.info("EXCEPTION IN RESPONSE");
				} else {
					if(response.getStatus() < 400) {
						String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
						if(KnownConstants.HEADER_VALUES.contains(contentType)) {
							BrowsedStore browsedStore = response.readEntity(BrowsedStore.class);
							browsedStore.getListingUrls().get(index).setContentUrl(browsedStore.getListingUrls().get(index).getListingUrl());
							logger.log(Level.INFO, "[[STORED.CONTENT.URL]] {0}", browsedStore);
						}
						BrowsedStore browsedStore = response.readEntity(BrowsedStore.class);
						String listingUrl = browsedStore.getListingUrls().get(index).getListingUrl();

						recursiveFetchContentUrl(browsedStore.getListingUrls().get(index).getListingUrl(), listingUrl.split(browsedStore.getPath())[0],index);
					} else {
						logger.log(Level.INFO, "[[Response.BrowsedStore.BAD]] {0}", response.readEntity(BrowsedStore.class).getStoreKey());
					}
				}
			}))
		;


		return pS;
	}

	public BrowsedStore addToBrowsedStoreCache(BrowsedStore bs) {
    	cache.getBrowsedStores().add(bs);
    	return bs;
	}


	// TEST
	CompletableFuture<Stream<BrowsedStore>> test(List<BrowsedStore> bs) {
    	return CompletableFuture.supplyAsync(new Supplier<Stream<BrowsedStore>>() {
			@Override
			public Stream<BrowsedStore> get() {
				return bs.stream();
			}
		});
	}

	// CompletableFuture of RemoteRepositories
	CompletableFuture<Map<String, List<RemoteRepository>>> getRemoteRepositories() {
		return CompletableFuture.supplyAsync(new Supplier<Map<String, List<RemoteRepository>>>() {
			@Override
			public Map<String, List<RemoteRepository>> get() {
				logger.info(">>> Fettching RemoteRepository: ");
				return repositoriesService.getRemoteRepos("maven");
			}
		});
	}

	// CompletableFuture of BrowsedStore
	CompletableFuture<List<BrowsedStore>> getBrowsedStores(List<RemoteRepository> remoteRepos) {
		return CompletableFuture.supplyAsync(new Supplier<List<BrowsedStore>>() {
			@Override
			public List<BrowsedStore> get() {
				logger.info(">>>>> Fetching BrowsedStore");
				return remoteRepos
					.parallelStream()
//					.stream()
					.map(repo -> repositoriesService.getBrowsedStoreByPackageType(repo.getPackageType(), repo.getName()))
					.filter(bs -> {
						try {
//							logger.log(Level.INFO, "[[BrowsedStore.EMPTY]] {0} {1}",new Object[] {bs.toCompletableFuture().get().getListingUrls().isEmpty(),bs.toCompletableFuture().get().getStoreKey()});
							return !bs.toCompletableFuture().get().getListingUrls().isEmpty();
						} catch (InterruptedException e) {} catch (ExecutionException e) {}
						return true;
					})
					.map(
						el -> {
						try {
							final BrowsedStore browsedStore = el.toCompletableFuture().get();
//							logger.log(Level.INFO, "[[BrowsedStore]] {0}", browsedStore.getStoreKey());
							return browsedStore;
						} catch (InterruptedException e) {} catch (ExecutionException e) {}
						el.exceptionally((throwable) -> {
						    logger.log(Level.INFO, "[[Exception]]: {0}",throwable.getMessage());
						    return new BrowsedStore();
						});
						return new BrowsedStore();
					}
					)
					.map(bs -> processBrowsedStore(bs))

//					.map(el -> addToBrowsedStoreCache(el))
//					.collect(Collectors.toCollection(() -> cache.getBrowsedStores()))
					.collect(Collectors.toList())
				;
			}
		});
	}

	// CompletableFuture of ListingUrls
	CompletableFuture<List<BrowsedStore>> processForContent(List<BrowsedStore> browsedStores) {
		return CompletableFuture.supplyAsync(new Supplier<List<BrowsedStore>>() {
			@Override
			public List<BrowsedStore> get() {
				logger.info(">>>>> Proccessing BrowsedStore");
				return browsedStores
						.parallelStream()
//						.stream()
						.map(bs -> processBrowsedStore(bs))
						.collect(Collectors.toList())
				;
			}
		});
	}

	// CompletableFuture of ContentUrls
}
