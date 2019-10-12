package org.geored.repomigrator.entity;

import java.util.List;

/**
 *
 * @author gorgigeorgievski
 */
public class ListingUrls {

	private String path;
	private String listingUrl;
	private List<String> sources;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getListingUrl() {
		return listingUrl;
	}

	public void setListingUrl(String listingUrl) {
		this.listingUrl = listingUrl;
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}
	
	
}
