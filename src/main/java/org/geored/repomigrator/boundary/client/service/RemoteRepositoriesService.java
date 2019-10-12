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
public interface RemoteRepositoriesService {


    @GET // test resource call
    @ClientHeaderParam(name="Authorization",value = "{createBasicAuthHeaderValue}")
    @Path("/admin/stores/maven/remote")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String,List<RemoteRepository>> getRemoteRepos(@HeaderParam("Authorization") String auth);

    @GET // test resource call
    @ClientHeaderParam(name = "Authorization",value = "{createBasicAuthHeaderValue}")
    @Path("/admin/stores/maven/remote/{name}")
    RemoteRepository getRemoteByName(@PathParam("name") String name,@HeaderParam("Authorization") String auth);
	
	@GET // test resource call
	@ClientHeaderParam(name = "Authorization",value = "{createBasicAuthHeaderValue}")
	@Path("/admin/stores/maven/remote/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	CompletionStage<RemoteRepository> getByNameAsync(@PathParam("name") String name,@HeaderParam("Authorization") String auth);
	
	@GET // /api/stats/all-endpoints
	@ClientHeaderParam(name = "Authorization",value = "{createBasicAuthHeaderValue}")
	@Path("/stats/all-endpoints")
	@Produces(MediaType.APPLICATION_JSON)
	Map<String,ArtifactStore> getAllArtifactStoresEndpoints();
	
	
	@GET //api/browse/npm/group/build-81
	@ClientHeaderParam(name = "Authorization",value = "{createBasicAuthHeaderValue}")
	@Path("/browse/{packageType}/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	BrowsedStore browseEndpointStores(@PathParam("packageType") String packageType,@PathParam("type") String type,@PathParam("name") String name,@HeaderParam("Authorization") String auth);
	
	
	
    default String createBasicAuthHeaderValue(String auth) {
        return auth;
    }
}
