package org.geored.repomigrator.control.cache;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class RemoteRepositoriesCache implements Serializable {

	Logger logger = Logger.getLogger(this.getClass().getName());
	public static Map<String, RemoteRepository> remoteRepos = new ConcurrentHashMap<>();

	@Inject
	@ConfigProperty(name = "redhat.indy.cache.rrc")
	String fileName;

	@PostConstruct
	public void init() {
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (SecurityException | IOException e) {
				logger.log(Level.WARNING, "!!!Create New File IOException");
			}
		} 
		if(file.length()>0) {
			try (ObjectInputStream inObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
				remoteRepos = (Map<String, RemoteRepository>) inObject.readObject();
			} 
			catch (FileNotFoundException ffe) {logger.log(Level.WARNING, "!!!FileNotFound");} 
			catch (IOException | ClassNotFoundException e) {logger.log(Level.WARNING, "!!!IOException");}
		}
	}

	@PreDestroy
	public void destroy() {
		// serialize
		try {
			try (ObjectOutputStream outObject = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
				outObject.writeObject(remoteRepos);
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

	}

	public Map<String, RemoteRepository> getRemoteRepos() {
		return remoteRepos;
	}

	public void setRepo(String repoName, RemoteRepository repository) {
		remoteRepos.put(repoName, repository);
	}
}
