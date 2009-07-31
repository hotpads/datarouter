package com.hotpads.datarouter.client;

import com.hotpads.datarouter.client.imp.hashmap.HashMapClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientFactory;
import com.hotpads.datarouter.storage.StorageType;

public enum ClientType {

	hashMap(StorageType.map),
	memcached(StorageType.map),
	ehcache(StorageType.map),
	
	treeMap(StorageType.sortedMap),
	bdb(StorageType.sortedMap),
	bdbJe(StorageType.sortedMap),
	
	jdbc(StorageType.column),
	hibernate(StorageType.column),
	
	simpleDb(StorageType.attribute),
	lucene(StorageType.attribute),
	hotpadsIndex(StorageType.attribute),
	;
	
	StorageType storageType;
	
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
		if(hashMap.equals(this)){ return new HashMapClientFactory(); }
		if(hibernate.equals(this)){ return new HibernateClientFactory(); }
		return null;
		
	}
}
