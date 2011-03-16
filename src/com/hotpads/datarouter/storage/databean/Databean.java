package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Databean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends FieldSet<D>{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	String getKeyFieldName();
	PK getKey();  
	
	boolean isFieldAware();
	
	List<Field<?>> getKeyFields();
	List<Field<?>> getNonKeyFields();
	
}
