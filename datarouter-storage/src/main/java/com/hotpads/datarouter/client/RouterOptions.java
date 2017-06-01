package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	private static final Logger logger = LoggerFactory.getLogger(RouterOptions.class);

	//TODO change all configuration files to specify a client type and remove the default
	private static final String DEFAULT_CLIENT_TYPE = "jdbc";

	public RouterOptions(Collection<Properties> propertiesList){
		super(propertiesList);
	}

	public RouterOptions(String propertiesPath){
		super(propertiesPath);
	}

	protected String getRouterPrefix(String routerName){
		return "router." + routerName + ".";
	}

	protected String getClientPrefix(String clientName){
		return "client." + clientName + ".";
	}

	protected String prependRouterPrefix(String routerName, String toAppend){
		return getRouterPrefix(routerName) + toAppend;
	}

	protected String prependClientPrefix(String clientName, String toAppend){
		return getClientPrefix(clientName) + toAppend;
	}

	/***************** actual variables *********************************/

	public String getClientType(String clientName){
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(typeName != null){
			return typeName;
		}
		logger.error("Client {} does not have a client type in its configuration file", clientName);
		return DEFAULT_CLIENT_TYPE;
	}

	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseRouter.MODE_production);
	}

}