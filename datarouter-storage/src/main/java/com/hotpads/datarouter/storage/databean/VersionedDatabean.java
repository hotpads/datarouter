package com.hotpads.datarouter.storage.databean;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface VersionedDatabean<PK extends PrimaryKey<PK>,D extends VersionedDatabean<PK,D>> extends Databean<PK,D>{

	long getVersion();
	void incrementVersion();

}
