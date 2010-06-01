package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface TableStorageWriteOps<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends SortedStorageWriteOps<D,PK>{

	void updateField(String field, Object newValue, Collection<? extends PK> keys);
	void updateFields(Map<String,Object> newValueByFieldName, Collection<? extends PK> keys);
	
//	void updateFieldWhere(String field, Object newValue, RestrictionSet restrictionSet);
//	void updateFieldsWhere(Map<String,Object> newValueByFieldName, RestrictionSet restrictionSet);
	
//	void deleteWhere(RestrictionSet restrictionSet);
}
