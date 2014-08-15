package com.hotpads.datarouter.storage.entity;

import java.util.Collection;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public interface Entity<EK extends EntityKey<EK>>{

	void setKey(EK ek);
	EK getKey();
	
	<PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefixUnchecked(String subEntityTableName, 
			Collection<? extends Databean<?,?>> databeans);
	
	int getNumDatabeans();
}
