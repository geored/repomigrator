package org.geored.repomigrator.boundary.client.filters;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;


@Provider
public class DefaultResponseFilter implements ClientResponseFilter {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

//		logger.log(Level.INFO , 
//		  "Req Headers: {0} | Res Headers: {1} | {2}", 
//		  new Object[]{ 
//			  requestContext.getHeaders(), 
//			  responseContext.getHeaders() ,
//			  Json.createReader( responseContext.getEntityStream() )
//		  });
	}
	
}
