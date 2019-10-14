package org.geored.repomigrator.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author gorgigeorgievski
 */

public class BrowsedStore implements Serializable {

	String storeKey;
	String path;
	String storeBrowseUrl;
	String storeContentUrl;
	String baseBrowseUrl;
	String baseContentUrl;
	
	List<String> sources;
	List<ListingUrls> listingUrls;

	public String getStoreKey() {
		return storeKey;
	}

	public void setStoreKey(String storeKey) {
		this.storeKey = storeKey;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStoreBrowseUrl() {
		return storeBrowseUrl;
	}

	public void setStoreBrowseUrl(String storeBrowseUrl) {
		this.storeBrowseUrl = storeBrowseUrl;
	}

	public String getStoreContentUrl() {
		return storeContentUrl;
	}

	public void setStoreContentUrl(String storeContentUrl) {
		this.storeContentUrl = storeContentUrl;
	}

	public String getBaseBrowseUrl() {
		return baseBrowseUrl;
	}

	public void setBaseBrowseUrl(String baseBrowseUrl) {
		this.baseBrowseUrl = baseBrowseUrl;
	}

	public String getBaseContentUrl() {
		return baseContentUrl;
	}

	public void setBaseContentUrl(String baseContentUrl) {
		this.baseContentUrl = baseContentUrl;
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	public List<ListingUrls> getListingUrls() {
		return listingUrls;
	}

	public void setListingUrls(List<ListingUrls> listingUrls) {
		this.listingUrls = listingUrls;
	}

	@Override
	public String toString() {
		return "{" + "storeKey:" + storeKey + ", path:" + path + ", storeBrowseUrl:" + storeBrowseUrl + ", storeContentUrl:" + storeContentUrl + ", baseBrowseUrl:" + baseBrowseUrl + ", baseContentUrl:" + baseContentUrl + ", sources:" + sources + ", listingUrls:" + listingUrls + "}";
	}
	
	
	
}
