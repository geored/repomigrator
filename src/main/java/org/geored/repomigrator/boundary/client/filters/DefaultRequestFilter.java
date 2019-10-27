package org.geored.repomigrator.boundary.client.filters;

import org.geored.repomigrator.control.KnownConstants;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultRequestFilter implements ClientRequestFilter, ClientResponseFilter {
	
	Logger logger = Logger.getLogger(this.getClass().getName());



	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

	}
}
