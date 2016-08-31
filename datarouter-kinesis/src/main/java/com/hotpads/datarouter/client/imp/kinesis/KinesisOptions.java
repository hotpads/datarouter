package com.hotpads.datarouter.client.imp.kinesis;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class KinesisOptions extends TypedProperties{

	private final String clientPrefix;
	private final String streamName;
	private final String regionName;
	private final String arnRole;

	public KinesisOptions(Datarouter datarouter, String clientName, String streamName, String regionName,
			String arnRole){
		super(DrPropertiesTool.fromFiles(datarouter.getConfigFilePaths()));
		this.clientPrefix = "client." + clientName + ".";
		this.streamName = streamName;
		this.regionName = regionName;
		this.arnRole = arnRole;
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
		return arnRole;
	}

	public String getStreamName(){
		return streamName;
	}

	public String getRegionName(){
		return regionName;
	}
}
