package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface TableStorageReadOps<D extends Databean,PK extends PrimaryKey<D>> 
extends SortedStorageReadOps<D,PK>{
	
//	Key<D> getKeysWhere(RestrictionSet restrictionSet);
//	D getWhere(RestrictionSet restrictionSet);

	Object getField(String fieldName, PK key);
	List<Object> getFields(List<String> fieldNames, PK key);

	List<Object> getField(String fieldName, Collection<? extends PK> keys);
	List<List<Object>> getFields(List<String> fieldNames, Collection<? extends PK> keys);

//	List<Object> getFieldWhere(String fieldName, RestrictionSet restrictionSet);
//	List<List<Object>> getFieldsWhere(List<String> fieldNames, RestrictionSet restrictionSet);
	
}
