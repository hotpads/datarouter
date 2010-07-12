package com.hotpads.datarouter.client;

import com.hotpads.util.core.properties.TypedProperties;

public class RouterOptions extends TypedProperties{
	
	public RouterOptions(String propertiesPath){
		super(propertiesPath);
	}
	
	protected String getClientPrefix(String clientName){
		return "client."+clientName+".";
	}
	
	protected String prependClientPrefix(String clientName, String toAppend){
		return getClientPrefix(clientName)+toAppend;
	}
	
	public ClientType getClientType(String clientName){
		String type = getString(prependClientPrefix(clientName, "type"));
		return type==null?null:ClientType.fromString(type);
	}

	public ClientFactory getFactory(String clientName){
		ClientType type = getClientType(clientName);
		return type==null?null:type.getClientFactory();
	}
	
}






