package org.geored.repomigrator.control.lifecycle;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geored.repomigrator.control.cache.RemoteRepositoriesCache;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class AppLifecycleBean {

	Logger logger = Logger.getLogger(this.getClass().getName());

	void onStart(@Observes StartupEvent event) {
		logger.log(Level.INFO, "\n\t\t\t === Init @ {0} ===", LocalDateTime.now());
	}

	void onStop(@Observes ShutdownEvent event) {
		logger.log(Level.INFO, "\n\t\t\t === Shutdown @ {0} ===", LocalDateTime.now());
	}

}
