package org.geored.repomigrator.boundary.vertx;


import io.reactivex.Flowable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.ListingUrls;
import rx.Observable;
import rx.Single;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentProcessVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public void start() throws Exception {
        logger.info("Verticle started....");

        vertx.eventBus().consumer("bs.data", (data) -> {
            BrowsedStore browsedStore = null;
             try {
                 browsedStore =Json.decodeValue((String) data.body(), BrowsedStore.class);
             } catch (Exception e) {
                 logger.log(Level.WARNING, "[[FAILED.DECODE]]: {0}", data.body() );

             }
            logger.log(Level.INFO, "[[DATA]]: {0}", browsedStore.getStoreKey() );


            if(browsedStore != null && browsedStore.getListingUrls() != null && !browsedStore.getListingUrls().isEmpty()) {

                List<ListingUrls> listingUrls = browsedStore.getListingUrls();


                Flowable<ListingUrls> listingsFlow =
                  Flowable.fromIterable(listingUrls);

                Flowable<Long> interval =
                  Flowable.interval(3, TimeUnit.SECONDS);

                Flowable
                  .zip(listingsFlow, interval, (obs,timer) -> obs)
                  .doOnNext(item -> {
                      logger.log(Level.INFO, "[[PROCESSING.LISTING]] {0}", item);
                      vertx.eventBus().publish("listing.url.processing", Json.encode(item));
                  }).toList().test()

                  ;

//                for (ListingUrls lu : listingUrls) {
//                    try {Thread.sleep(2000);} catch (InterruptedException ie) {}
//                    vertx.eventBus().publish("listing.url.processing", Json.encode(lu));
//
//                }

            }

        });
    }


    public WebClient getWebClient() {
        WebClient client = WebClient.create(vertx);
        return client;
    }
}
