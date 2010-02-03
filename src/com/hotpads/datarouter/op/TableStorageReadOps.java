package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;

public interface TableStorageReadOps<D extends Databean> 
extends SortedStorageReadOps<D>{
	
//	Key<D> getKeysWhere(RestrictionSet restrictionSet);
//	D getWhere(RestrictionSet restrictionSet);

	Object getField(String fieldName, Key<D> key);
	List<Object> getFields(List<String> fieldNames, Key<D> key);

	List<Object> getField(String fieldName, Collection<Key<D>> keys);
	List<List<Object>> getFields(List<String> fieldNames, Collection<Key<D>> keys);

//	List<Object> getFieldWhere(String fieldName, RestrictionSet restrictionSet);
//	List<List<Object>> getFieldsWhere(List<String> fieldNames, RestrictionSet restrictionSet);
	
}
