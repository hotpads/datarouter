package com.hotpads.datarouter.storage.databean;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.Key;

public interface Databean
extends Serializable, /*FieldSet,*/ Comparable<Databean>{

	String getDatabeanName();
	
	@SuppressWarnings("unchecked")
	Key getKey();  //can't figure out how to keep checked
	
//	List<Field> getDataFields();
}
