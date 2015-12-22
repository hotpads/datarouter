package com.hotpads.datarouter.storage.lazy.base;

import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class BaseLazyReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,S extends NodeOps<PK,D>>
implements LazyReader<PK,D,S>{

	private S backingStorage;

	public BaseLazyReader(S backingStorage){
		this.backingStorage = backingStorage;
	}

	@Override
	public S getBackingStorage(){
		return backingStorage;
	}

}
