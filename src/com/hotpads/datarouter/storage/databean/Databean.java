package com.hotpads.datarouter.storage.databean;

import java.io.Serializable;

import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface Databean
extends Serializable, /*FieldSet,*/ Comparable<Databean>{

	String getDatabeanName();
//	Class<? extends PrimaryKey<? extends Databean>> getKeyClass();
	
	@SuppressWarnings("unchecked")  //can't figure out how to keep checked
	PrimaryKey getKey();  
	
}
