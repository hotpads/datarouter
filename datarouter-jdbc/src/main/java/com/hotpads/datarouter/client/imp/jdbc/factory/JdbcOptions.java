package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.util.Properties;

import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class JdbcOptions extends TypedProperties{

	private static final String CREATE_DATABASE = "createDatabases";
	private static final String SCHEMA_UPDATE_ENABLE = "schemaUpdate.enable";
	private static final String EXECUTE_CREATE_DB = ParallelSchemaUpdate.EXECUTE_PREFIX+"."+CREATE_DATABASE;
	private static final String PRINT_CREATE_DB = ParallelSchemaUpdate.PRINT_PREFIX+"."+CREATE_DATABASE;
	private static final boolean DEFAULT_BOOLEAN = false;
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
	
	public Boolean executeCreateDb(){
		return getBoolean(getRequiredString(EXECUTE_CREATE_DB), DEFAULT_BOOLEAN);
	}
	
	public boolean printCreateDb(){
		return getBoolean(getRequiredString(PRINT_CREATE_DB), DEFAULT_BOOLEAN);
	}

	public boolean schemaUpdateEnabled(){
		return getBoolean(getRequiredString(SCHEMA_UPDATE_ENABLE), DEFAULT_BOOLEAN);
	}

}
