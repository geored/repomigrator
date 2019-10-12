package org.geored.repomigrator.boundary.client.filters;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultRequestFilter implements ClientRequestFilter  {
	
	Logger logger = Logger.getLogger(this.getClass().getName());



	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
//		logger.log(Level.INFO , 
//		  "HTTP [{0}] | {1}", new Object[]{ requestContext.getMethod(), requestContext.getUri() });
	}
	
}