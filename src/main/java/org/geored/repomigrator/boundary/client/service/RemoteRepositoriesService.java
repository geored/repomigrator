package org.geored.repomigrator.boundary.client.service;


import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.geored.repomigrator.entity.RemoteRepository;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.core.MediaType;
import org.geored.repomigrator.entity.ArtifactStore;
import org.geored.repomigrator.entity.BrowsedStore;

@Path("/api")
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name="Authorization",value = "{createBasicAuthHeaderValue}")
public interface RemoteRepositoriesService {


    @GET
    @Path("/admin/stores/{packageType}/remote")
    Map<String,List<RemoteRepository>> getRemoteRepos(
	  @PathParam("packageType") String packageType,
	  @HeaderParam("Authorization") String auth);

    @GET
    @Path("/admin/stores/{packageType}/remote/{name}")
    RemoteRepository getRemoteByName(
	  @PathParam("packageType") String packageType,
	  @PathParam("name") String name,
	  @HeaderParam("Authorization") String auth);
	
	@GET
	@Path("/admin/stores/{packageType}/remote/{name}")
	CompletionStage<RemoteRepository> getByNameAsync(
	  @PathParam("packageType") String packageType,
	  @PathParam("name") String name,
	  @HeaderParam("Authorization") String auth);
	
	@GET
	@Path("/stats/all-endpoints")
	Map<String,ArtifactStore> getAllArtifactStoresEndpoints(
	  @HeaderParam("Authorization") String auth);
	
	
	@GET //api/browse/npm/group/build-81 <- example
	@Path("/browse/{packageType}/{type}/{name}")
	BrowsedStore browseEndpointStores(
	  @PathParam("packageType") String packageType,
	  @PathParam("type") String type,
	  @PathParam("name") String name,
	  @HeaderParam("Authorization") String auth);
	
	
	
    default String createBasicAuthHeaderValue(String auth) {
        return auth;
    }
}
