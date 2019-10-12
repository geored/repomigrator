package org.geored.repomigrator.control.lifecycle;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class AppLifecycleBean {
	
	static final Logger logger = Logger.getLogger(AppLifecycleBean.class.getName());
	
	
	void onStart(@Observes StartupEvent event) {
		logger.log(Level.INFO, "\n\t\t Application is initialized at {0}\n", LocalDateTime.now());
	}
	
	void onStop(@Observes ShutdownEvent event) {
		logger.log(Level.INFO, "\n\t\t Application is shutdown at {0}\n", LocalDateTime.now());
	}
	
}
