package com.hotpads.datarouter.client.imp.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.hotpads.util.core.properties.TypedProperties;

public class HttpClientOptions extends TypedProperties{
	
	protected String clientPrefix;

	public HttpClientOptions(List<Properties> multiProperties, String clientName){
		super(multiProperties);
		this.clientPrefix = "client."+clientName+".http.";
	}
	
	public URL getUrl(){
		String urlString = getRequiredString(clientPrefix+"numServers");
		try{
			return new URL(urlString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid URL:"+urlString);
		}
	}
	
}
