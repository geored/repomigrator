package org.geored.repomigrator.control.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author gorgigeorgievski
 */


@ApplicationScoped
public class LifecycleManager {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	
	void listenToProcessEvent(@Observes ProcessLifecycle processLifecycle) {
		switch(processLifecycle) {
			case START:
				// Write to EventSource File
				logger.log(Level.INFO, "= Process Started =");
				break;
			case END:
				// Write to EventSource File
				logger.log(Level.INFO, "= Process Ended =");
				break;
			case RUNNING:
				// Write to EventSource File
				logger.log(Level.INFO, "= Process Running =");
				break;
			case STOPED:
				// Write to EventSource File
				logger.log(Level.INFO, "= Process Stoped =");
				break;
			case WAITING:
				// Write to EventSource File
				logger.log(Level.INFO, "= Process Waiting =");
				break;
			default:
				// Write to EventSource File
				break;
		}
	}
	
	void listenToCacheEvent(@Observes CacheLifecycle cacheLifecycle) {
		switch(cacheLifecycle) {
			case LOADING:
				// Write to EventSource File
				logger.log(Level.INFO, "= Cache Loading =");
				break;
			case PROCESSED:
				// Write to EventSource File
				logger.log(Level.INFO, "= Cache Proccessing =");
				break;
			case LOADED:
				// Write to EventSource File
				logger.log(Level.INFO, "= Cache Loaded =");
				break;
			default:
				// Write to EventSource File
				break;
		}
	}
	
	
}
