package org.geored.repomigrator.boundary.client;


import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.geored.repomigrator.entity.RemoteRepository;
import org.geored.repomigrator.boundary.client.service.RemoteRepositoriesService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/repos")
public class RemoteRepositoryResource {


    @Inject
    @RestClient
    RemoteRepositoriesService remoteService;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRemoteRepositories(@HeaderParam("Authorization") String auth) {
        System.out.println("Authentication: " + auth);
        return Response.ok(remoteService.getRemoteRepos(auth)).build();
    }
    
    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRemoteRepositoryByName(@PathParam("name") String name,@HeaderParam("Authorization") String auth) {
        System.out.println("Authentication: " + auth);
        return Response.ok(remoteService.getRemoteByName(name, auth)).build();
    }
	
	@GET
	@Path("/async/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public CompletionStage<RemoteRepository> getRemoteRepositoryByNameAsync(@PathParam("name") String name,@HeaderParam("Authorization") String auth) {
		System.out.println("Authentication: " + auth);
		return remoteService.getByNameAsync(name, auth);
	}
}
