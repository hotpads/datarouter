package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.Properties;

import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
	public static final String CLIENT_NAME_memory = "memory";

	public RouterOptions(Collection<Properties> propertiesList){
		super(propertiesList);
	}
	
	public RouterOptions(String propertiesPath){
		super(propertiesPath);
	}
	
	
	protected String getRouterPrefix(String routerName){
		return "router."+routerName+".";
	}
	
	protected String getClientPrefix(String clientName){
		return "client."+clientName+".";
	}
	
	
	protected String prependRouterPrefix(String routerName, String toAppend){
		return getRouterPrefix(routerName)+toAppend;
	}
	
	protected String prependClientPrefix(String clientName, String toAppend){
		return getClientPrefix(clientName)+toAppend;
	}
	
	
	/***************** actual variables *********************************/
	
	public ClientType getClientType(String clientName){
		if(CLIENT_NAME_memory.equals(clientName)){ return ClientType.memory; }
		String type = getString(prependClientPrefix(clientName, "type"));
		ClientType clientType = ClientType.fromString(type);
//		return Preconditions.checkNotNull(clientType, "unknown clientType:"+clientType+" for clientName:"+clientName);
		return clientType==null?ClientType.hibernate:clientType;
	}
	
	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseDataRouter.MODE_production);
	}
	
}






