package com.hotpads.datarouter.client.imp.hibernate.factory;

import java.util.Properties;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class HibernateOptions extends TypedProperties{
	
	protected String clientPrefix;

	public HibernateOptions(Iterable<Properties> multiProperties, String clientName){
		super(ListTool.createArrayList(multiProperties));
		this.clientPrefix = "client."+clientName+".";
	}

	
	public String url(String def){
		return getString(clientPrefix+"url", def);
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
}
