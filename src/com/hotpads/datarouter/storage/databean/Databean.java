package com.hotpads.datarouter.storage.databean;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Databean<PK extends PrimaryKey<PK>>
extends FieldSet{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	PK getKey();  
	
	List<Field<?>> getKeyFields();
	List<Field<?>> getNonKeyFields();
	
	<D extends Databean<PK>> Map<PK,D> getByKey(Iterable<D> databeans);
}
