package com.hotpads.datarouter.storage.databean;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseVersionedDatabean<PK extends PrimaryKey<PK>,D extends VersionedDatabean<PK,D>>
extends BaseDatabean<PK,D> implements VersionedDatabean<PK,D>{

	private Long version;

	@Override
	public long getVersion(){
		return version == null ? 0 : version;
	}

	@Override
	public void incrementVersion(){
		if(version == null){
			version = 0L;
		}
		version++;
	}

}
