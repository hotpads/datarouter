package com.hotpads.datarouter.storage.lazy;

import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.BaseLazyReader;
import com.hotpads.datarouter.storage.lazy.mixin.LazyIndexedStorageReaderMixin;
import com.hotpads.datarouter.storage.lazy.mixin.LazyMapStorageReaderMixin;

public class LazyIndexedMapStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseLazyReader<PK,D,IndexedMapStorageReader<PK,D>>
implements LazyMapStorageReaderMixin<PK,D,IndexedMapStorageReader<PK,D>>,
		LazyIndexedStorageReaderMixin<PK,D,IndexedMapStorageReader<PK,D>>{

	public LazyIndexedMapStorageReader(IndexedMapStorageReader<PK,D> storage){
		super(storage);
	}

}
