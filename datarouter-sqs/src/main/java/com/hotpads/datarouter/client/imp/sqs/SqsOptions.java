package com.hotpads.datarouter.client.imp.sqs;

import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class SqsOptions extends TypedProperties{
	
	private final String clientPrefix;
	
	public SqsOptions(DatarouterContext datarouterContext, String clientName){
		super(DrPropertiesTool.fromFiles(datarouterContext.getConfigFilePaths()));
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
