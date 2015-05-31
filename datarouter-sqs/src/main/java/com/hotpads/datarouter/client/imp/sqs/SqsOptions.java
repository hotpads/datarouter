package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class SqsOptions extends TypedProperties{
	
	private final String clientPrefix;
	
	public SqsOptions(Collection<Properties> properties, String clientName){
		super(properties);
		this.clientPrefix = "client." + clientName + ".";
	}

	public String getAccessKey(){
		return getString(clientPrefix + "accessKey");
	}
	
	public String getSecretKey(){
		return getString(clientPrefix + "secretKey");
	}
}
