package com.hotpads.datarouter.storage.databean;

import java.io.Serializable;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Databean<PK extends PrimaryKey<PK>>
extends Comparable<Databean<PK>>,Serializable{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	PK getKey();  
	
}
