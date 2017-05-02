package com.hotpads.datarouter.client.imp.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.properties.TypedProperties;

public class SqsOptions extends TypedProperties{
	private static final Logger logger = LoggerFactory.getLogger(SqsOptions.class);

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
		String namespace = optString(clientPrefix + "namespace")
				.orElse(datarouterProperties.getEnvironment() + "-" + datarouterProperties.getServiceName());
		logger.warn("clientPrefix={}, namespace={}", clientPrefix, namespace);
		return namespace;
	}
}
