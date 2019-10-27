package org.geored.repomigrator.boundary.vertx;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.ListingUrls;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geored.repomigrator.control.cache.RemoteRepositoriesCache.urlList;

public class ListingUrlProcessingVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(this.getClass().getName());

    @Inject RemoteRepositoriesCache cache;


    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("listing.url.processing",(data) -> {
            ListingUrls listingUrl = Json.decodeValue((String) data.body(), ListingUrls.class);
            fetchContentUrl(listingUrl.getListingUrl());
        });
    }


    public String fetchContentUrl(String url) {

        if(!url.endsWith("/")) {
            // process this url

            logger.log(Level.INFO, "[[CONTENT.URL]] {0}", url);
            try {
                cache.urlList.add(new URL(url));
            } catch (MalformedURLException e) {
                logger.log(Level.INFO, "[[BAD.CONTENT.URL]] {0}", url);
            }

            // send event for processing this url with source url...
            // { content: [url] , source: [url] }

            return url;
        }

        getClient()
          .getAbs(url)
          // Authentication will be done from secure account config map
          .basicAuthentication("", "")
          .followRedirects(true)
          .send((resp) -> {
            if(resp.succeeded()) {
                @Nullable BrowsedStore browsedStore = resp.result().bodyAsJson(BrowsedStore.class);
                if(browsedStore.getListingUrls() != null) {
                    for(ListingUrls listingUrl : browsedStore.getListingUrls()) {
                        vertx.eventBus().publish("listing.url.processing", Json.encode(listingUrl));
                    }
                }
            } else {
                logger.log(Level.WARNING, "[[EXCEPTION.GET.CONTENT.URL]]: {0}", resp.result() );

            }
        });




        return "placeholder";
    }

    public WebClient getClient() {
        return WebClient.create(vertx);
    }
}
