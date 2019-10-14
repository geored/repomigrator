package org.geored.repomigrator.entity;
 
import java.io.Serializable;

public class Metadata implements Serializable {
    
	
	String changelog;

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

	@Override
	public String toString() {
		return "{" + "changelog:" + changelog + "}";
	}
	
	
}
