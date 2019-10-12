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
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 *
 * @author gorgigeorgievski
 */
public class DefaultResponseFilter implements ContainerResponseFilter {
	
	static final Logger logger = Logger.getLogger(DefaultResponseFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.log(Level.INFO , 
		  "Req Headers: {0} | Res Headers: {1}", new Object[]{ requestContext.getHeaders(), responseContext.getHeaders() });
	}
	
}
