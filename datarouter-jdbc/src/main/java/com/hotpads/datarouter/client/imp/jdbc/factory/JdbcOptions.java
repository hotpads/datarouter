package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.Properties;

import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class JdbcOptions extends TypedProperties{
	
	private static final String SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";
	
	protected String clientPrefix;

	public JdbcOptions(Iterable<Properties> multiProperties, String clientName){
		super(DrListTool.createArrayList(multiProperties));
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

	public boolean schemaUpdateEnabled(){
		return getBoolean(SCHEMA_UPDATE_ENABLE, false);
	}

}
