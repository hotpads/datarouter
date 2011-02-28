package com.hotpads.datarouter.client;

import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.StorageType;
import com.hotpads.util.core.StringTool;

public enum ClientType {

	memory(StorageType.sortedMap),
	memcached(StorageType.map),
	ehcache(StorageType.map),
	
	treeMap(StorageType.sortedMap),
	
	bdb(StorageType.indexed),
	bdbJe(StorageType.indexed),
	
	hbase(StorageType.sortedMap),
	cassandra(StorageType.sortedMap),
	hypertable(StorageType.sortedMap),
	
	jdbc(StorageType.column),
	hibernate(StorageType.column),
	
	simpleDb(StorageType.attribute),
	lucene(StorageType.attribute),
	hotpadsIndex(StorageType.attribute),
	;
	
	protected StorageType storageType;
	
	ClientType(StorageType storageType){
		this.storageType = storageType;
	}
	
	public static ClientType fromString(String input){
		for(ClientType t : values()){
			if(StringTool.equalsCaseInsensitive(t.toString(), input)){
				return t;
			}
		}
		return null;
	}
	
	public ClientFactory createClientFactory(
			DataRouter router, String clientName, 
			String configFileLocation, ExecutorService executorService){
		if(hbase==this){ return new HBaseSimpleClientFactory(router, clientName, 
				configFileLocation, executorService); }
		if(hibernate==this){ return new HibernateSimpleClientFactory(router, clientName, 
				configFileLocation, executorService); }
		return null;
		
	}
}
