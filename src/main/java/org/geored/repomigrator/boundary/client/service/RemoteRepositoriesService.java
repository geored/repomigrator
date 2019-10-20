package org.geored.repomigrator.boundary.client.service;


import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.geored.repomigrator.entity.RemoteRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.geored.repomigrator.entity.ArtifactStore;
import org.geored.repomigrator.entity.BrowsedStore;

@Path("/api")
@RegisterRestClient(baseUri = "")
@Produces(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name="Authorization",value = "")
public interface RemoteRepositoriesService {


    @GET
    @Path("/admin/stores/{packageType}/remote")
    Map<String,List<RemoteRepository>> getRemoteRepos(
	  @PathParam("packageType") String packageType);

	@GET
	@Path("/admin/stores/{packageType}/remote")
	CompletionStage<Map<String,List<RemoteRepository>>> getRemoteReposAsync(
	@PathParam("packageType") String packageType);

    @GET
    @Path("/admin/stores/{packageType}/remote/{name}")
    RemoteRepository getRemoteByName(
	  @PathParam("packageType") String packageType,
	  @PathParam("name") String name);
	
	@GET
	@Path("/admin/stores/{packageType}/remote/{name}")
	CompletionStage<RemoteRepository> getByNameAsync(
	  @PathParam("packageType") String packageType,
	  @PathParam("name") String name);

	@GET
	@Path("/browse/{packageType}/{type}/{name}")
	CompletionStage<BrowsedStore> browseDirectoryAsync(
	  @PathParam("packageType") String packageType,
	  @PathParam("type") String type,
	  @PathParam("name") String name);
	
	@GET
	@Path("/stats/all-endpoints")
	Map<String,ArtifactStore> getAllArtifactStoresEndpoints();

	@GET
	@Path("/browse/{packageType}/{type}")
	List<BrowsedStore> getBrowsedStores(
		@PathParam("packageType") String packageType,
		@PathParam("type") String type
	);

	@GET
	@Path("/browse/{packageType}/{type}/{name}")
	BrowsedStore getBrowsedStore(
		@PathParam("packageType") String packageType,
		@PathParam("type") String type,
		@PathParam("name") String name
	);

	@GET
	@Path("/browse/{packageType}/remote/{name}")
	CompletionStage<BrowsedStore> getBrowsedStoreByPackageType(
		@PathParam("packageType") String packageType,
		@PathParam("name") String name
	);



	default String createBasicAuthHeaderValue(String auth) {
        return auth;
    }
}
