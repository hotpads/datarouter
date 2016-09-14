package com.hotpads.datarouter.client.imp.kinesis.client;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class KinesisOptions extends TypedProperties{

	private final String clientPrefix;

	public KinesisOptions(Datarouter datarouter, String clientName){
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

	public String getArnRole(){
		return getRequiredString(clientPrefix + "arnRole");
	}

	public String getKclNamespace(){
		return getRequiredString(clientPrefix + "kcl.namespace");
	}
}
