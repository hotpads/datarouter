package com.hotpads.datarouter.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.http.DataRouterHttpClientFactory;
import com.hotpads.datarouter.client.imp.memcached.MemcachedSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memory.MemoryClientFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.StorageType;
import com.hotpads.util.core.StringTool;

public enum ClientType {

	memory(StorageType.sortedMap),
	memcached(StorageType.map),
	
	hbase(StorageType.sortedMap),
	
	jdbc(StorageType.column),
	hibernate(StorageType.column),
	
	http(StorageType.map),
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
				return new HBaseSimpleClientFactory(drContext, clientName);
//			}
		}else if(hibernate==this){ 
			return new HibernateSimpleClientFactory(drContext, clientName); 
		}else if(memcached==this){ 
			return new MemcachedSimpleClientFactory(drContext, clientName); 
		}else if(http==this){
			return new DataRouterHttpClientFactory(drContext, clientName);
		}
		
		throw new IllegalArgumentException("unsupported ClientType "+this);
		
	}
}
