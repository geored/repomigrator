package org.geored.repomigrator.control.schedulers;


import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.boundary.client.resource.RemoteRepositoryResource;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;
import org.geored.repomigrator.boundary.vertx.BrowsedStoreCodec;
import org.geored.repomigrator.boundary.vertx.ContentProcessVerticle;
import org.geored.repomigrator.boundary.vertx.ListingUrlProcessingVerticle;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.RemoteRepository;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class RxScheduler {


    Logger logger = Logger.getLogger(this.getClass().getName());


    @Inject @RestClient
    RemoteRepositoriesService repositoryService;

    @Inject
    RemoteRepositoriesCache cache;

    @Inject
    Vertx vertx;

    @PostConstruct
    public void init() {

        System.out.println("Bean initialized...");
        DeploymentOptions options = new DeploymentOptions();
        options.setWorker(true);
        options.setWorkerPoolName("test-worker-pool");
        options.setWorkerPoolSize(16);
//        vertx.eventBus().registerDefaultCodec(BrowsedStore.class, new BrowsedStoreCodec());
        vertx.deployVerticle(new ContentProcessVerticle(),options);
        vertx.deployVerticle(new ListingUrlProcessingVerticle(), options);

    }


    @Scheduled(every = "1000s")
    public void timer(ScheduledExecution se) {

          repositoryService
            .getRemoteReposAsync("maven")
//            .thenComposeAsync(this::getNpmRepositories)
          .thenApplyAsync(this::getItemsFrom)
            .thenApplyAsync(this::filterDisabledRemoteStores)
            .toCompletableFuture()
            .join()
              .stream()
              .map(this::getBrowsedStore)
              .map(CompletableFuture::join)
              .map(this::publishBrowsedStore)
              .collect(Collectors.toList())
          ;

    }


    public List<RemoteRepository> getItemsFrom(Map<String, List<RemoteRepository>> items) {
        return items.get("items");
    }
    public List<RemoteRepository> filterDisabledRemoteStores(List<RemoteRepository> repos) {
        logger.info(">>>>> Filtering RemoteRepositories");
        return repos.stream().filter(repo -> !repo.getDisabled()).collect(Collectors.toList());
    }

    public BrowsedStore publishBrowsedStore(BrowsedStore bs) {

        vertx.eventBus().publish("bs.data", Json.encode (bs) );

        return bs;
    }
    CompletableFuture<BrowsedStore> getBrowsedStore(RemoteRepository r) {
        return CompletableFuture.supplyAsync(new Supplier<BrowsedStore>() {
            @Override
            public BrowsedStore get() {
                return repositoryService.getBrowsedStoreByPackageType(r.getPackageType(), r.getName());
            }
        });
    }

    CompletionStage<Map<String,List<RemoteRepository>>> getNpmRepositories(Map<String,List<RemoteRepository>> mavenRepos) {
        return repositoryService.getRemoteReposAsync("npm");
    }
}
