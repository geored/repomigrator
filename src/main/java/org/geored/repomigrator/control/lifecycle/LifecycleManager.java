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
				logger.log(Level.INFO, "= [[PROCESS.STARTED]] =");
				break;
			case END:
				logger.log(Level.INFO, "= [[PROCESS.END]] =");
				break;
			case RUNNING:
				logger.log(Level.INFO, "= [[PROCESS.RUNNING]] =");
				break;
			case STOPED:
				logger.log(Level.INFO, "= [[PROCESS.STOPED]] =");
				break;
			case WAITING:
				logger.log(Level.INFO, "= [[PROCESS.WAITING]] =");
				break;
			default:
				break;
		}
	}
	
	void listenToCacheEvent(@Observes CacheLifecycle cacheLifecycle) {
		switch(cacheLifecycle) {
			case LOADING:
				logger.log(Level.INFO, "= [[CACHE.LOADING]] =");
				break;
			case PROCESSED:
				logger.log(Level.INFO, "= [[CACHE.PROCESSED]] =");
				break;
			case LOADED:
				logger.log(Level.INFO, "= [[CACHE.LOADED]] =");
				break;
			default:
				break;
		}
	}
	
	
}
