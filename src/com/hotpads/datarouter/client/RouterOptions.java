package com.hotpads.datarouter.client;

import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
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
		String type = getString(prependClientPrefix(clientName, "type"));
		return type==null?null:ClientType.fromString(type);
	}
	
	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseDataRouter.MODE_dev);
	}
	
}






