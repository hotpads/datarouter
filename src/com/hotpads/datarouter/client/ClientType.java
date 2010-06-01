package com.hotpads.datarouter.client;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientFactory;
import com.hotpads.datarouter.storage.StorageType;

public enum ClientType {

	memory(StorageType.sortedMap),
	memcached(StorageType.map),
	ehcache(StorageType.map),
	
	treeMap(StorageType.sortedMap),
	
	bdb(StorageType.indexed),
	bdbJe(StorageType.indexed),
	
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
			if(t.toString().equals(input)){
				return t;
			}
		}
		return null;
	}
	
	public ClientFactory getClientFactory(){
		if(hibernate.equals(this)){ return new HibernateClientFactory(); }
		return null;
		
	}
}
