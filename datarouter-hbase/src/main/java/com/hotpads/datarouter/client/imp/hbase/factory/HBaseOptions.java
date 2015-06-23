package com.hotpads.datarouter.client.imp.hbase.factory;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class HBaseOptions extends TypedProperties{
	
	private String clientPrefix;

	public HBaseOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client." + clientName + ".hbase.";
	}

	public String zookeeperQuorum(){
		return getString(clientPrefix + "zookeeper.quorum");
	}

	public boolean checkTables(){
		return getBoolean(clientPrefix + "checkTables", false);
	}

	public boolean createTables(){
		return getBoolean(clientPrefix + "createTables", false);
	}

	@Deprecated
	public Integer minPoolSize(int def){
		return getInteger(clientPrefix + "minPoolSize", def);
	}

	public Integer maxHTables(int defaultValue){
		return getInteger(clientPrefix + "maxHTables", defaultValue);
	}

	public Integer minThreadsPerHTable(int defaultValue){
		return getInteger(clientPrefix + "minThreadsPerHTable", defaultValue);
	}

	public Integer maxThreadsPerHTable(int defaultValue){
		return getInteger(clientPrefix + "maxThreadsPerHTable", defaultValue);
	}
	
}
