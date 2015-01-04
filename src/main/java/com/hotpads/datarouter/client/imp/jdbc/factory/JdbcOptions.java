package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.Properties;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class JdbcOptions extends TypedProperties{
	
	protected String clientPrefix;

	public JdbcOptions(Iterable<Properties> multiProperties, String clientName){
		super(ListTool.createArrayList(multiProperties));
		this.clientPrefix = "client."+clientName+".";
	}

	
	public String url(){
		return getRequiredString(clientPrefix+"url");
	}

	public String user(String def){
		return getString(clientPrefix+"user", def);
	}

	public String password(String def){
		return getString(clientPrefix+"password", def);
	}
	
	public Integer minPoolSize(Integer def){
		return getInteger(clientPrefix+"minPoolSize", def);
	}
	
	public Integer maxPoolSize(Integer def){
		return getInteger(clientPrefix+"maxPoolSize", def);
	}
	
	public Boolean logging(Boolean def){
		return getBoolean(clientPrefix+"logging", def);
	}
	
	public Boolean appEngine(){
		return getBoolean(clientPrefix+"appEngine", false);
	}

}
