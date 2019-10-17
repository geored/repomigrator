package org.geored.repomigrator.boundary.client.filters;


import org.jboss.resteasy.microprofile.client.ExceptionMapping;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 *
 * @author gorgigeorgievski
 */

@Provider
public class FilterAll implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        JsonObject exc = Json.createObjectBuilder().add("exception", exception.getMessage()).build();
        return Response.ok(exc).build();
    }
}
