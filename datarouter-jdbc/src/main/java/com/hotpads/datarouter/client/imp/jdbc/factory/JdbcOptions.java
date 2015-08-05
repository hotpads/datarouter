package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.Properties;

import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class JdbcOptions extends TypedProperties{

	protected String clientPrefix;
	private final String clientGenericPrefix;

	public JdbcOptions(Iterable<Properties> multiProperties, String clientName){
		super(DrListTool.createArrayList(multiProperties));
		this.clientPrefix = "client."+clientName+".";
		this.clientGenericPrefix = "client.generic.";
	}


	public String url(){
		return getRequiredString(clientPrefix+"url");
	}

	public String getGenericUrl(){
		return getRequiredString(clientGenericPrefix+"url");
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
