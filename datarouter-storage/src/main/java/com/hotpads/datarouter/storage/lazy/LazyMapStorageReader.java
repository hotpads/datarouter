package com.hotpads.datarouter.storage.lazy;

import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.BaseLazyReader;
import com.hotpads.datarouter.storage.lazy.mixin.LazyMapStorageReaderMixin;

public class LazyMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,S extends MapStorageReader<PK,D>>
extends BaseLazyReader<PK,D,S>
implements LazyMapStorageReaderMixin<PK,D,S>{

	public LazyMapStorageReader(S mapStorage){
		super(mapStorage);
	}

}
