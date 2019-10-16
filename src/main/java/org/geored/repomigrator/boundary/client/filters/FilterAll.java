package org.geored.repomigrator.boundary.client.filters;


import org.jboss.resteasy.microprofile.client.ExceptionMapping;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
        return Response.ok().build();
    }
}
