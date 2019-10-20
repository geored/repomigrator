package org.geored.repomigrator.boundary.client.resource;


import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Generated;
import javax.enterprise.context.RequestScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.entity.RemoteRepository;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;

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
    @Path("/cache/listingurls")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRemoteContentListingUrls() {
        return Response.ok(cache.getContentUrls()).build();
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
}
