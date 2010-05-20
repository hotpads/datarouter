package com.hotpads.datarouter.storage.databean;

import java.io.Serializable;

import com.hotpads.datarouter.storage.key.Key;

public interface Databean
extends Serializable, /*FieldSet,*/ Comparable<Databean>{

	String getDatabeanName();
	
	@SuppressWarnings("unchecked")  //can't figure out how to keep checked
	Key getKey();  
	
}
