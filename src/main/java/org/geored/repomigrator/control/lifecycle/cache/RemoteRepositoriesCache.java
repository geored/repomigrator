package org.geored.repomigrator.control.lifecycle.cache;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import org.geored.repomigrator.entity.RemoteRepository;

@ApplicationScoped
public class RemoteRepositoriesCache implements Serializable {

	Map<String, RemoteRepository> repos = new ConcurrentHashMap<>();
	static final Logger logger = Logger.getLogger(RemoteRepositoriesCache.class.getName());
	
	private String fileName = "remote_repos.ser";

	@PostConstruct
	public void initialize() {
		// deserialize
		try {
			File file = new File(fileName);
			if(!file.exists()) { file.createNewFile(); }
			
			if (file.length() != 0l) {
				System.out.println("\t\t\t--- Reading values from serialized object ....");
				try (ObjectInputStream inObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
					repos = (Map<String, RemoteRepository>) inObject.readObject();
				}
			}
		} catch (IOException | ClassNotFoundException e) { logger.log(Level.SEVERE, null, e); }
	}

	@PreDestroy
	public void destroy() {
		// serialize
		try {
			System.out.println("\t\t\t--- Writing values to serialized object ....");
			try (ObjectOutputStream outObject = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
				outObject.writeObject(repos);
			}
		} catch (IOException ex) { logger.log(Level.SEVERE, null, ex); }

	}

	public Map<String, RemoteRepository> getRepos() {
		return repos;
	}

	public void setRepos(Map<String, RemoteRepository> repos) {
		this.repos = repos;
	}

	public void setRepo(String repoName, RemoteRepository repository) {
		this.repos.put(repoName, repository);
	}
}
