package org.geored.repomigrator.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author gorgigeorgievski
 */
public class ListingUrls implements Serializable{

	String path;
	String listingUrl;
	List<String> sources;

	String contentUrl;

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

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

	@Override
	public String toString() {
		return "{" + "path:" + path + ", listingUrl:" + listingUrl + ", sources:" + sources + ",contentUrl:" + contentUrl+ "}";
	}
	
	
	
}
