package org.geored.repomigrator.control.schedulers;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.ListingUrls;
import org.geored.repomigrator.entity.RemoteRepository;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

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

	@Inject
	Vertx vertx;


//    @Scheduled(every = "10000s")
	public void increase(ScheduledExecution se) {

		logger.log(Level.INFO, "[[CACHE.SIZE]] {0}", cache.getRemoteRepos().size());

		getRemoteRepositories("maven")

			.thenApply(this::getItemsFrom)

			.thenApply(this::filterDisabledRemoteStores)

			.thenCompose(this::getBrowsedStores)

//			.thenApplyAsync(this::printElements)

			.thenCompose(this::processBrowsedStores)

			.exceptionally((throwable) -> {

					logger.log(Level.WARNING, "[[Exception.STREAM]]: {0}", throwable.getMessage());

				return null;
			})
		;

//		WebClient client = WebClient.create(vertx,new WebClientOptions().setDefaultPort(80).setDefaultHost("indy-admin-master-devel.psi.redhat.com"));
//		Flowable<JsonObject> indyRemoteRepoFlow =
//			client.get("/api/admin/stores/maven/remote")
//			.basicAuthentication("","")
//			.rxSend().map(HttpResponse::bodyAsJsonObject).toFlowable();
//
//		Flowable<JsonObject> indyBrowsedStoreFlow =
//			client.get("/api/browse/maven/remote/central")
//			.basicAuthentication("","")
//			.rxSend().map(HttpResponse::bodyAsJsonObject).toFlowable();
//
//		indyRemoteRepoFlow.subscribe(content -> System.out.println("Content: " + content),
//										err -> System.out.println("Cannot read content: " + err.getMessage()));
//		indyBrowsedStoreFlow.subscribe(content -> System.out.println("Content: " + content),
//										err -> System.out.println("Cannot read the file: " + err.getMessage()));

	}

	// Functional Methods
	public List<RemoteRepository> filterDisabledRemoteStores(List<RemoteRepository> repos) {
    	logger.info(">>>>> Filtering RemoteRepositories");
    	return repos.stream().filter(repo -> !repo.getDisabled()).collect(Collectors.toList());
	}

	public List<RemoteRepository> getItemsFrom(Map<String, List<RemoteRepository>> items) {
		return items.get("items");
	}

	public List<BrowsedStore> printElements(List<BrowsedStore> le) {
		for(BrowsedStore bs : le) {
			logger.log(Level.INFO, "Content: {0} , ListingUrls: {1}", new Object[] {bs.getStoreKey(),bs.getListingUrls()});
		}
		return le;
	}

	public List<ListingUrls> processBrowsedStore(BrowsedStore store) {
		logger.log(Level.INFO, "[[PROCESSING.STORE]] {0}", store.getStoreKey());
		return
			store
				.getListingUrls()
				.parallelStream()
				.map(lu -> {
					String previousPathUrl = lu.getListingUrl().split(lu.getPath())[0];
					int index = store.getListingUrls().indexOf(lu);
					String fetchedUrl = recursiveFetchContentUrl(lu.getListingUrl(), previousPathUrl,index);
					lu.setContentUrl(fetchedUrl);
					return lu;
				})
					.collect(Collectors.toList())
		;
	}

	CompletableFuture<List<ListingUrls>> processBrowsedStores(List<BrowsedStore> browsedStores) {
    	return CompletableFuture.supplyAsync(new Supplier<List<ListingUrls>>() {
			@Override
			public List<ListingUrls> get() {
				return browsedStores
							.parallelStream()
							.flatMap(bs -> processBrowsedStore(bs).stream())
							.collect(Collectors.toList())
				;
			}
		});
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
//			cache.getContentUrls().add(rS);
			return rS;
		}
//		logger.log(Level.INFO, "[[CLIENT.URL]] {0}", rS);

		ClientBuilder.newClient().target(url)
			.request(MediaType.APPLICATION_JSON)
			.header(AUTHORIZATION, auth)
			.rx()
			.get(Response.class)
			.toCompletableFuture()
			.whenCompleteAsync(((response, throwable) -> {
				if(throwable != null) {
					logger.info("EXCEPTION IN RESPONSE");
				} else {
					if(response.getStatus() < 400) {
						String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
						BrowsedStore browsedStore = response.readEntity(BrowsedStore.class);

//						if(KnownConstants.HEADER_VALUES.contains(contentType)) {
//							browsedStore.getListingUrls().get(index).setContentUrl(browsedStore.getListingUrls().get(index).getListingUrl());
//							logger.log(Level.INFO, "[[STORED.CONTENT.URL]] {0}", browsedStore);
//						} else {
							String listingUrl = browsedStore.getListingUrls().get(index).getListingUrl();
							recursiveFetchContentUrl(
								browsedStore.getListingUrls().get(index).getListingUrl(),
								listingUrl.split(browsedStore.getPath())[0],index
							);
//						}

					} else {
						logger.log(Level.INFO, "[[Response.BrowsedStore.BAD]] {0}", response.readEntity(BrowsedStore.class).getStoreKey());
					}
				}
			})).join()
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
	CompletableFuture<Map<String, List<RemoteRepository>>> getRemoteRepositories(String packageType) {
		return CompletableFuture.supplyAsync(new Supplier<Map<String, List<RemoteRepository>>>() {
			@Override
			public Map<String, List<RemoteRepository>> get() {
				logger.info(">>> Fettching RemoteRepository: ");
				return repositoriesService.getRemoteRepos(packageType);
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
					.map(repo -> repositoriesService.getBrowsedStoreByPackageType(repo.getPackageType(), repo.getName()))
//					.filter(bs -> {
//						try {
////							logger.log(Level.INFO, "[[BrowsedStore.EMPTY]] {0} {1}",new Object[] {bs.toCompletableFuture().get().getListingUrls().isEmpty(),bs.toCompletableFuture().get().getStoreKey()});
//							return !bs.toCompletableFuture().get().getListingUrls().isEmpty();
//						} catch (InterruptedException e) {} catch (ExecutionException e) {}
//						return true;
//					})
//					.map(el -> {
//						try {
//							return el.get();
//						} catch (InterruptedException e) {} catch (ExecutionException e) {}
//						el.exceptionally((throwable) -> {
//						    logger.log(Level.INFO, "[[Exception]]: {0}",throwable.getMessage());
//						    return new BrowsedStore();
//						});
//						return new BrowsedStore();
//					})
//					.flatMap(bs -> { return processBrowsedStore(bs).stream(); })
					.collect(Collectors.toList())
				;
			}
		});
	}

	// CompletableFuture of ListingUrls
//	CompletableFuture<List<BrowsedStore>> processForContent(List<BrowsedStore> browsedStores) {
//		return CompletableFuture.supplyAsync(new Supplier<List<BrowsedStore>>() {
//			@Override
//			public List<BrowsedStore> get() {
//				logger.info(">>>>> Proccessing BrowsedStore");
//				return browsedStores
//						.parallelStream()
//						.map(bs -> processBrowsedStore(bs))
//						.collect(Collectors.toList())
//				;
//			}
//		});
//	}

	// CompletableFuture of ContentUrls
}
