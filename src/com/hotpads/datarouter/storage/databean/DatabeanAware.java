package com.hotpads.datarouter.storage.databean;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface DatabeanAware<PK extends PrimaryKey<PK>,D extends Databean<PK>> {

	String getDatabeanName();
	Class<D> getDatabeanClass();
	String getPhysicalNodeName();
}
