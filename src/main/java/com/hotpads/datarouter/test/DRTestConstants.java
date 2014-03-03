package com.hotpads.datarouter.test;

import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;
import com.hotpads.util.core.ListTool;

public class DRTestConstants{

	public static final String
		CONFIG_PATH = "/hotpads/config/datarouter-test.properties",
		DATABASE_drTest0 = "drTest0",
		CLIENT_drTestMemory = "memory0",
		CLIENT_drTestJdbc0 = "drTestJdbc0",
		CLIENT_drTestHibernate0 = "drTestHibernate0",
		CLIENT_drTestHBase = "drTestHBase",
		CLIENT_drTestMemcached = "drTestMemcached";

	public static List<ClientType> ALL_CLIENT_TYPES = ListTool.create();
	public static List<Object[]> CLIENT_TYPE_OBJECT_ARRAYS = ListTool.create();
	static{
		ALL_CLIENT_TYPES.add(MemoryClientType.INSTANCE);
		ALL_CLIENT_TYPES.add(JdbcClientType.INSTANCE);
		ALL_CLIENT_TYPES.add(HibernateClientType.INSTANCE);
		ALL_CLIENT_TYPES.add(HBaseClientType.INSTANCE);
		ALL_CLIENT_TYPES.add(MemcachedClientType.INSTANCE);
		for(ClientType clientType : ALL_CLIENT_TYPES){
			CLIENT_TYPE_OBJECT_ARRAYS.add(new Object[]{clientType});
		}
	}
}
