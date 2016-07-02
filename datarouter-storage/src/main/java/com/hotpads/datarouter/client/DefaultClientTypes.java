package com.hotpads.datarouter.client;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class DefaultClientTypes{

	@Inject
	private DatarouterInjector injector;

	public static final String
			CLIENT_TYPE_bigtable = "bigtable",
			CLIENT_TYPE_hbase = "hbase",
			CLIENT_TYPE_hibernate = "hibernate",
			CLIENT_TYPE_http = "http",
			CLIENT_TYPE_jdbc = "jdbc",
			CLIENT_TYPE_memcached = "memcached",
			CLIENT_TYPE_memory = "memory",
			CLIENT_TYPE_sqs = "sqs";

	public static final String
			CLIENT_CLASS_bigtable = "com.hotpads.datarouter.client.bigtable.BigTableClientType",
			CLIENT_CLASS_hbase = "com.hotpads.datarouter.client.imp.hbase.HBaseClientType",
			CLIENT_CLASS_hibernate = "com.hotpads.datarouter.client.imp.hibernate.HibernateClientType",
			CLIENT_CLASS_http = "com.hotpads.datarouter.client.imp.http.HttpClientType",
			CLIENT_CLASS_jdbc = "com.hotpads.datarouter.client.imp.jdbc.JdbcClientType",
			CLIENT_CLASS_memcached = "com.hotpads.datarouter.client.imp.memcached.MemcachedClientType",
			CLIENT_CLASS_memory = "com.hotpads.datarouter.client.imp.memory.MemoryClientType",
			CLIENT_CLASS_sqs = "com.hotpads.datarouter.client.imp.sqs.SqsClientType";

	//TODO these should eventually be mapped in an external config file, like datarouter-xyz.properties
	public static final Map<String,String> CLASS_BY_NAME = new TreeMap<>();
	static{
		CLASS_BY_NAME.put(CLIENT_TYPE_bigtable, CLIENT_CLASS_bigtable);
		CLASS_BY_NAME.put(CLIENT_TYPE_hbase, CLIENT_CLASS_hbase);
		CLASS_BY_NAME.put(CLIENT_TYPE_hibernate, CLIENT_CLASS_hibernate);
		CLASS_BY_NAME.put(CLIENT_TYPE_http, CLIENT_CLASS_http);
		CLASS_BY_NAME.put(CLIENT_TYPE_jdbc, CLIENT_CLASS_jdbc);
		CLASS_BY_NAME.put(CLIENT_TYPE_memcached, CLIENT_CLASS_memcached);
		CLASS_BY_NAME.put(CLIENT_TYPE_memory, CLIENT_CLASS_memory);
		CLASS_BY_NAME.put(CLIENT_TYPE_sqs, CLIENT_CLASS_sqs);
	}

	public ClientType create(String name){
		return (ClientType)injector.getInstance(ClassTool.forName(CLASS_BY_NAME.get(name)));
	}

}
