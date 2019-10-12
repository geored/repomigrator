/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geored.repomigrator.boundary.client.filters;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultRequestFilter implements ContainerRequestFilter  {
	
	static final Logger logger = Logger.getLogger(DefaultRequestFilter.class.getName());


	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.log(Level.INFO , 
		  "HTTP [{0}] | {1}", new Object[]{ requestContext.getMethod(), requestContext.getUriInfo().getAbsolutePath().toString() });
	}
	
}
