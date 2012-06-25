package com.hotpads.datarouter.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memcached.MemcachedSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memory.MemoryClientFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
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
	
	public static final boolean USE_RECONNECTING_HBASE_CLIENT = false;
	
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName, 
			List<PhysicalNode<?,?>> physicalNodes, ExecutorService executorService){
		if(memory==this){
			return new MemoryClientFactory(clientName);
		}else if(hbase==this){ 
//			if(USE_RECONNECTING_HBASE_CLIENT){
//				return new HBaseDynamicClientFactory(router, clientName, 
//						configFileLocation, executorService);
//			}else{
				return new HBaseSimpleClientFactory(drContext, clientName, executorService);
//			}
		}else if(hibernate==this){ 
			return new HibernateSimpleClientFactory(drContext, clientName, executorService); 
		}else if(memcached==this){ 
			return new MemcachedSimpleClientFactory(drContext, clientName, executorService); 
		}
		
		throw new IllegalArgumentException("unsupported ClientType "+this);
		
	}
}
