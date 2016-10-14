package com.hotpads.datarouter.client.imp.kinesis.client;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class KinesisOptions extends TypedProperties{

	private static final boolean ENABLED = true;

	private final String clientPrefix;

	public KinesisOptions(Datarouter datarouter, String clientName){
		super(DrPropertiesTool.fromFiles(datarouter.getConfigFilePaths()));
		this.clientPrefix = "client." + clientName + ".";
	}

	public String getAccessKey(){
		if(!isEnabled()){
			return null;
		}
		return getRequiredString(clientPrefix + "accessKey");
	}

	public String getSecretKey(){
		if(!isEnabled()){
			return null;
		}
		return getRequiredString(clientPrefix + "secretKey");
	}

	public String getNamespace(){
		if(!isEnabled()){
			return null;
		}
		return getRequiredString(clientPrefix + "namespace");
	}

	public String getArnRole(){
		if(!isEnabled()){
			return null;
		}
		return getString(clientPrefix + "arnRole");
	}

	public String getKclNamespace(){
		if(!isEnabled()){
			return null;
		}
		return getRequiredString(clientPrefix + "kcl.namespace");
	}

	public boolean isEnabled(){
		return getBoolean(clientPrefix + "enabled", ENABLED);
	}
}
