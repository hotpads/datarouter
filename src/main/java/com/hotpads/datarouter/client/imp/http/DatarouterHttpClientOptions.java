package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class DatarouterHttpClientOptions extends TypedProperties{
	
	protected String clientPrefix;

	public DatarouterHttpClientOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client."+clientName+".http.";
	}
	
	public String getUrl(){
		return getRequiredString(clientPrefix+"url");
	}
	
}