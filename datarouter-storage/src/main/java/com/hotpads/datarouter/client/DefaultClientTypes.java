package com.hotpads.datarouter.client;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class DefaultClientTypes{

	@Inject
	private DatarouterInjector injector;

	public static final String
			CLIENT_TYPE_hbase = "hbase",
			CLIENT_TYPE_hibernate = "hibernate",
			CLIENT_TYPE_http = "http",
			CLIENT_TYPE_jdbc = "jdbc",
			CLIENT_TYPE_memcached = "memcached",
			CLIENT_TYPE_memory = "memory",
			CLIENT_TYPE_sqs = "sqs",
			CLIENT_TYPE_sqsGroup = "sqsGroup"
			;

	//TODO these should eventually be mapped in an external config file, like datarouter-xyz.properties
	public static final Map<String,String> CLASS_BY_NAME = new TreeMap<>();
	static{
		CLASS_BY_NAME.put(CLIENT_TYPE_hbase, "com.hotpads.datarouter.client.imp.hbase.HBaseClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_hibernate, "com.hotpads.datarouter.client.imp.hibernate.HibernateClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_http, "com.hotpads.datarouter.client.imp.http.HttpClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_jdbc, "com.hotpads.datarouter.client.imp.jdbc.JdbcClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_memcached, "com.hotpads.datarouter.client.imp.memcached.MemcachedClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_memory, "com.hotpads.datarouter.client.imp.memory.MemoryClientType");
		CLASS_BY_NAME.put(CLIENT_TYPE_sqs, "com.hotpads.datarouter.client.imp.sqs.SqsClientType");
	}

	public static final List<String> CLIENTS_IN_CORE_MODULE = DrListTool.createArrayList(
//			CLIENT_TYPE_hbase,
//			CLIENT_TYPE_hibernate,
//			CLIENT_TYPE_http,
//			CLIENT_TYPE_jdbc,
//			CLIENT_TYPE_memcached,
			CLIENT_TYPE_memory
			);

	public ClientType create(String name){
		return (ClientType)injector.getInstance(ClassTool.forName(CLASS_BY_NAME.get(name)));
	}


	/*************** tests ************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class DefaultClientTypeTests{
		@Inject
		private DefaultClientTypes defaultClientTypes;

		@Test
		public void testFragileStrings(){
			for(String className : DefaultClientTypes.CLIENTS_IN_CORE_MODULE){
				defaultClientTypes.create(className);
			}
		}
	}
}
