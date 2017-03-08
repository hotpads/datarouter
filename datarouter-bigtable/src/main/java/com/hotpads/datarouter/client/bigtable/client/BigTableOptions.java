package com.hotpads.datarouter.client.bigtable.client;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class BigTableOptions extends TypedProperties{

	private final String clientPrefix;

	public BigTableOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client." + clientName + ".bigtable.";
	}

	public String projectId(){
		return getString(clientPrefix + "projectId");
	}

	public String instanceId(){
		return getString(clientPrefix + "instanceId");
	}

}
