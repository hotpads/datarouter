package com.hotpads.datarouter.client.imp.sqs;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class SqsOptions extends TypedProperties{
	
	private final String clientPrefix;
	
	public SqsOptions(Datarouter datarouter, String clientName){
		super(DrPropertiesTool.fromFiles(datarouter.getConfigFilePaths()));
		this.clientPrefix = "client." + clientName + ".";
	}

	public String getAccessKey(){
		return getRequiredString(clientPrefix + "accessKey");
	}
	
	public String getSecretKey(){
		return getRequiredString(clientPrefix + "secretKey");
	}
	
	public String getNamespace(){
		return getRequiredString(clientPrefix + "namespace");
	}
}
