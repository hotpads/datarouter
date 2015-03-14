package com.hotpads.datarouter.test;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;

public class DRTestConstants{

	public static final String
		CONFIG_PATH = "/hotpads/config/datarouter-test.properties",
		DATABASE_drTest0 = "drTest0",
		CLIENT_drTestMemory = "memory0",
		CLIENT_drTestJdbc0 = "drTestJdbc0",
		CLIENT_drTestHibernate0 = "drTestHibernate0",
		CLIENT_drTestHBase = "drTestHBase",
		CLIENT_drTestMemcached = "drTestMemcached";

	public static List<ClientType> ALL_CLIENT_TYPES = new ArrayList<>();
	public static List<Object[]> CLIENT_TYPE_OBJECT_ARRAYS = new ArrayList<>();
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
