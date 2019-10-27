package org.geored.repomigrator.control.lifecycle;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class AppLifecycleBean {

	Logger logger = Logger.getLogger(this.getClass().getName());
	
	Event<ProcessLifecycle> appLifecycleEvent;

	void onStart(@Observes StartupEvent event) {
		logger.log(Level.INFO, "\n\t\t\t === [[APP.INIT]] @ {0} ===", LocalDateTime.now());
	}

	void onStop(@Observes ShutdownEvent event) {
		logger.log(Level.INFO, "\n\t\t\t === [[SHUTDOWN]] @ {0} ===", LocalDateTime.now());
	}

}
