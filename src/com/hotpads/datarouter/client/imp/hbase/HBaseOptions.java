package com.hotpads.datarouter.client.imp.hbase;

import java.util.Properties;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.properties.TypedProperties;

public class HBaseOptions extends TypedProperties{
	
	protected String clientPrefix;

	public HBaseOptions(Properties properties, String clientName){
		super(ListTool.wrap(properties));
		this.clientPrefix = "client."+clientName+".";
	}
	
	public boolean checkTables(){
		return getBoolean(clientPrefix+"checkTables", false);
	}

	public boolean createTables(){
		return getBoolean(clientPrefix+"createTables", false);
	}
	
	public Integer getMinPoolSize(int def){
		return getInteger(clientPrefix+"minPoolSize", def);
	}
	
}
