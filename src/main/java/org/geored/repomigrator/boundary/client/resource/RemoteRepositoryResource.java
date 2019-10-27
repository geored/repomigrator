package org.geored.repomigrator.boundary.client.resource;


import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.entity.RemoteRepository;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.reactivestreams.Publisher;

@Path("/repos")
@RequestScoped
public class RemoteRepositoryResource {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
    @Inject
    @RestClient
    RemoteRepositoriesService remoteService;
	
	@Inject
	RemoteRepositoriesCache cache;


	@GET
    @Path("/cache/urls")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRemoteContentListingUrls() {
        return Response.ok(cache.getListingsUrls()).build();
    }

    @GET
    @Path("/cache/browsedstores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBrowsedStoresFromCache() {
        return Response.ok(cache.getBrowsedStores()).build();
    }
	
	
    @GET
    @Path("/{packageType}/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRemoteRepositories(@PathParam("packageType") String packageType) {
        return Response.ok(remoteService.getRemoteRepos(packageType)).build();
    }
    
    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRemoteRepositoryByName(@PathParam("packageType") String packageType,@PathParam("name") String name) {
        return Response.ok(remoteService.getRemoteByName(packageType,name)).build();
    }
	
	@GET
	@Path("/async/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public CompletionStage<RemoteRepository> getRemoteRepositoryByNameAsync(@PathParam("packageType") String packageType,@PathParam("name") String name) {
		return remoteService.getByNameAsync(packageType,name);
	}
	
	@GET
    @Path("{cache}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRemoteRepositoryCache() {
        return Response.ok(cache.getRemoteRepos()).build();
    }

    @GET
    @Path("/sse/urls")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> publishUrls() {
	    return RemoteRepositoriesCache.urlsFlow.map(String::valueOf);
    }
}
