package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;

public interface TableStorageWriteOps<D extends Databean> 
extends SortedStorageWriteOps<D>{

	void updateField(String field, Object newValue, Collection<Key<D>> keys);
	void updateFields(Map<String,Object> newValueByFieldName, Collection<Key<D>> keys);
	
//	void updateFieldWhere(String field, Object newValue, RestrictionSet restrictionSet);
//	void updateFieldsWhere(Map<String,Object> newValueByFieldName, RestrictionSet restrictionSet);
	
//	void deleteWhere(RestrictionSet restrictionSet);
}
