package org.geored.repomigrator.entity;

import java.io.Serializable;

public class RemoteRepository implements Serializable {

	String type;
	String key;
	Boolean disabled;
	String host;
	Integer port;
	String packageType;
	String name;
	String url;
	Metadata metadata;

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "RemoteRepository{" + "type:" + type + ", key:" + key + ", disabled:" + disabled + ", host:" + host + ", port:" + port + ", packageType:" + packageType + ", name:" + name + ", url:" + url + ", metadata:" + metadata + '}';
	}
	
	
}
