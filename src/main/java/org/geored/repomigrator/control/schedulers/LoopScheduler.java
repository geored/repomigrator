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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

	@Scheduled(every = "120s")
	public void increase(ScheduledExecution se) {

		logger.log(Level.INFO, "[[CACHE.SIZE]] {0}", cache.getRemoteRepos().size());

		cache.getRemoteRepos().values().stream()
			.filter(repo -> !repo.getDisabled())
			.forEach(repo -> System.out.println(repo.getKey()));

			cache.getRemoteRepos().values().stream()
					.filter(repo -> !repo.getDisabled())
					.map(repo -> getBrowsedStoreAsync(repo.getPackageType(), repo.getType(), repo.getName(), auth))
					.filter(bs -> {
						return filterNoListingsUrls(bs);
					})
					.map(bs -> {
						return printProcessingStore(bs);
					})
					.map(CompletableFuture::join)
					.map(bs -> bs.getListingUrls())
					.map(lu -> getContent(lu.get(0).getListingUrl()))
					.forEach(System.out::println)
			;

	}

	RemoteRepositoriesService getRestClient(String url) {
		RemoteRepositoriesService restClient = null;
		try {
			restClient = RestClientBuilder.newBuilder()
			  .baseUrl(new URL(url)).build(RemoteRepositoriesService.class);
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "[[REST.CLIENT.EXCEPTION]] | MailformedURLException | {0}", LocalDateTime.now());
		}
		return restClient;
	}

	RestClientBuilder getRestClientBuilder(String url) {
		try {
			return RestClientBuilder.newBuilder().baseUrl(new URL(url));
		} catch (MalformedURLException ex) {
			logger.log(Level.WARNING, "[[REST.CLIENT.EXCEPTION]] | MailformedURLException | {0}", ex.getMessage());
		}
		return null;
	}

	CompletableFuture<BrowsedStore> getBrowsedStoreAsync(String pt,String t,String n,String a) {
		CompletableFuture<BrowsedStore> bs = CompletableFuture.supplyAsync(new Supplier<BrowsedStore>() {
			@Override
			public BrowsedStore get() {
				BrowsedStore bscf = repositoriesService.browseDirectoryAsync(pt, t, n, a);
				return bscf;
			}
		});
		return bs;
	}

	CompletableFuture<BrowsedStore> printProcessingStore(CompletableFuture<BrowsedStore> repo) {
		try {
			logger.log(Level.INFO, "[[PROCESSING.STORE]] {0}", repo.get().getStoreKey());
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "[[PRINT.PROCESSING.STORE]] | InterruptedException | {0}", e.getMessage());
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "[[PRINT.PROCESSING.STORE]] | ExecutionException | {0}", e.getMessage());
		}
		return repo;
	}

	String getContent(String url) {
		//  	!url.equals("")
		if(url != null && !url.endsWith("/")) {
			logger.log(Level.INFO, "[[WEB.CLIENT.CONTENT.URL]] url:{0}", url);
			return url;
		}
		Client client = null;
		try {

			client = ClientBuilder.newClient();
//			client.register(RepomigrationResponseException.class);
			Invocation.Builder clientInvocation =
			client
				.target(url)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", auth);
			Response response = clientInvocation.get(Response.class);
			if (response.getStatus() >= 400) {
				return "";
			}
			if (response.getStatus() == 200) {
				BrowsedStore browsedStore = response.readEntity(BrowsedStore.class);
				if(browsedStore.getListingUrls()!= null && browsedStore.getListingUrls().size()>0) {
//					logger.log(Level.INFO, "[[READING.LISTING]] {0}",browsedStore.getListingUrls());
					String listingUrl = browsedStore.getListingUrls().get(0).getListingUrl();
					getContent(listingUrl);
				}
			} else {
				logger.log(Level.INFO,"[[WEB.CLIENT]] url:{0} status:{1}", new Object[]{response.readEntity(BrowsedStore.class).getListingUrls().get(0).getListingUrl(),String.valueOf(response.getStatus())});
			}
		} catch (Exception e) {
			logger.log(Level.INFO,"[[EXCEPTION.WEB.CLIENT]] {0}", e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return "";
	}

	boolean filterNoListingsUrls(CompletableFuture<BrowsedStore> cbs) {
		try {
			return cbs.get().getListingUrls() == null ? false : true ;
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "[[FILTER.PROCESSING.STORE]] | InterruptedException | {0}", e.getMessage());
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "[[FILTER.PROCESSING.STORE]] | ExecutionException | {0}", e.getMessage());
		}
		return true;
	}
}

