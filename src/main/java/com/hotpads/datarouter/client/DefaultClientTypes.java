package com.hotpads.datarouter.client;

import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.hotpads.util.core.java.ReflectionTool;

public class DefaultClientTypes{

	public static final Map<String,String> CLASS_BY_NAME = new TreeMap<>();
	static{
		CLASS_BY_NAME.put("hbase", "com.hotpads.datarouter.client.imp.hbase.HBaseClientType");
		CLASS_BY_NAME.put("hibernate", "com.hotpads.datarouter.client.imp.hibernate.HibernateClientType");
		CLASS_BY_NAME.put("http", "com.hotpads.datarouter.client.imp.http.HttpClientType");
		CLASS_BY_NAME.put("jdbc", "com.hotpads.datarouter.client.imp.jdbc.JdbcClientType");
		CLASS_BY_NAME.put("memcached", "com.hotpads.datarouter.client.imp.memcached.MemcachedClientType");
		CLASS_BY_NAME.put("memory", "com.hotpads.datarouter.client.imp.memory.MemoryClientType");
	}
	
	public static ClientType create(String name){
		return ReflectionTool.create(CLASS_BY_NAME.get(name));
	}
	
	
	/************************ tests *********************************/
	
	public static class DefaultClientTypeTests{
		@Test
		public void testFragileStrings(){
			for(String className : CLASS_BY_NAME.keySet()){
				create(className);
			}
		}
	}
}
