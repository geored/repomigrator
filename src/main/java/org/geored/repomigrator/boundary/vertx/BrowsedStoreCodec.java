package org.geored.repomigrator.boundary.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.geored.repomigrator.entity.BrowsedStore;

import io.vertx.core.json.JsonObject;
import org.geored.repomigrator.entity.ListingUrls;

import java.util.ArrayList;
import java.util.List;

public class BrowsedStoreCodec
  implements MessageCodec<BrowsedStore, BrowsedStore>
{


    @Override
    public void encodeToWire(Buffer buffer, BrowsedStore browsedStore) {
        JsonObject jsonToEncode = new JsonObject();
        jsonToEncode.put("storeKey",browsedStore.getStoreKey());
        jsonToEncode.put("path", browsedStore.getPath());
        jsonToEncode.put("storeBrowseUrl", browsedStore.getStoreBrowseUrl());
        jsonToEncode.put("storeContentUrl", browsedStore.getStoreContentUrl());
        jsonToEncode.put("baseBrowseUrl", browsedStore.getBaseBrowseUrl());
        jsonToEncode.put("sources",browsedStore.getSources());
        jsonToEncode.put("listingUrls",browsedStore.getListingUrls());

        String jsonToStr = jsonToEncode.encode();
        int length = jsonToStr.getBytes().length;
        buffer.appendInt(length);
        buffer.appendString(jsonToStr);

    }

    @Override
    public BrowsedStore decodeFromWire(int pos, Buffer buffer) {
        int _pos = pos;
        int length = buffer.getInt(_pos);

        String jsonStr = buffer.getString(_pos+=4, _pos+=length);
        JsonObject contentJson = new JsonObject(jsonStr);

        // Get fields
        String storeKey = contentJson.getString("storeKey");
        String path = contentJson.getString("path");
        String storeBrowseUrl = contentJson.getString("storeBrowseUrl");
        String storeContentUrl = contentJson.getString("storeContentUrl");
        String baseBrowseUrl = contentJson.getString("baseBrowseUrl");
        List<String> sources = (ArrayList<String>) contentJson.getValue("sources");
        List<ListingUrls> listingUrls = (ArrayList<ListingUrls>) contentJson.getValue("listingUrls");

        return new BrowsedStore(storeKey, path, storeBrowseUrl,storeContentUrl,baseBrowseUrl,sources,listingUrls);
    }

    @Override
    public BrowsedStore transform(BrowsedStore browsedStore) {
        return null;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
