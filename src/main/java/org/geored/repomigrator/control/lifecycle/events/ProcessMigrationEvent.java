package org.geored.repomigrator.control.lifecycle.events;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import org.geored.repomigrator.control.lifecycle.ProcessLifecycle;
import org.geored.repomigrator.entity.BrowsedStore;
import org.geored.repomigrator.entity.ListingUrls;
import org.geored.repomigrator.entity.RemoteRepository;

/**
 *
 * @author gorgigeorgievski
 */
public class ProcessMigrationEvent implements Serializable {
	
	LocalDateTime timestamp;
	
	ProcessLifecycle stage;
	
	RemoteRepository remoteRepository;
	
	BrowsedStore browsedStore;
	
	List<ListingUrls> listingUrls;
	

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public ProcessLifecycle getStage() {
		return stage;
	}

	public void setStage(ProcessLifecycle stage) {
		this.stage = stage;
	}

	public RemoteRepository getRemoteRepository() {
		return remoteRepository;
	}

	public void setRemoteRepository(RemoteRepository remoteRepository) {
		this.remoteRepository = remoteRepository;
	}

	public BrowsedStore getBrowsedStore() {
		return browsedStore;
	}

	public void setBrowsedStore(BrowsedStore browsedStore) {
		this.browsedStore = browsedStore;
	}

	public List<ListingUrls> getListingUrls() {
		return listingUrls;
	}

	public void setListingUrls(List<ListingUrls> listingUrls) {
		this.listingUrls = listingUrls;
	}

	@Override
	public String toString() {
		return "{" + "timestamp:" + timestamp + ", stage:" + stage + ", remoteRepository:" + remoteRepository + ", browsedStore:" + browsedStore + ", listingUrls:" + listingUrls + "}";
	}
	
	
	
}
