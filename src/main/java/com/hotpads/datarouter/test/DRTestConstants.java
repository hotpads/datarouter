package com.hotpads.datarouter.test;

import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.util.core.ListTool;

public class DRTestConstants{

	public static final String
		CONFIG_PATH = "/hotpads/config/datarouter-test.properties",
		DATABASE_drTest0 = "drTest0",
		CLIENT_drTestMemory = RouterOptions.CLIENT_NAME_memory,
		CLIENT_drTestHibernate0 = "drTestHibernate0",
		CLIENT_drTestHBase = "drTestHBase",
		CLIENT_drTestMemcached = "drTestMemcached";

	public static List<ClientType> ALL_CLIENT_TYPES = ListTool.create();
	public static List<Object[]> CLIENT_TYPE_OBJECT_ARRAYS = ListTool.create();
	static{
		ALL_CLIENT_TYPES.add(ClientType.memory);
		ALL_CLIENT_TYPES.add(ClientType.hibernate);
		ALL_CLIENT_TYPES.add(ClientType.hbase);
		ALL_CLIENT_TYPES.add(ClientType.memcached);
		for(ClientType clientType : ALL_CLIENT_TYPES){
			CLIENT_TYPE_OBJECT_ARRAYS.add(new Object[]{clientType});
		}
	}
}