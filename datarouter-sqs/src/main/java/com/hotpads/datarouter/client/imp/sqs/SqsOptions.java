package com.hotpads.datarouter.client.imp.sqs;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class SqsOptions extends TypedProperties{

	private final DatarouterProperties datarouterProperties;
	private final String clientPrefix;

	public SqsOptions(DatarouterProperties datarouterProperties, Datarouter datarouter, String clientName){
		super(DrPropertiesTool.fromFiles(datarouter.getConfigFilePaths()));
		this.datarouterProperties = datarouterProperties;
		this.clientPrefix = "client." + clientName + ".";
	}

	public String getAccessKey(){
		return getRequiredString(clientPrefix + "accessKey");
	}

	public String getSecretKey(){
		return getRequiredString(clientPrefix + "secretKey");
	}

	//SQS max queue name length is 80 chars.
	//TODO limit this to 30
	public String getNamespace(){
		return optString(clientPrefix + "namespace")
				.orElse(datarouterProperties.getEnvironment() + "-" + datarouterProperties.getServiceName());
	}
}
