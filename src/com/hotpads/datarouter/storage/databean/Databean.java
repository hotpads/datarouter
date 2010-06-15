package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Databean<PK extends PrimaryKey<PK>>
extends FieldSet{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	PK getKey();  
	
	List<BaseField<?>> getKeyFields();
	List<BaseField<?>> getNonKeyFields();
	
}
