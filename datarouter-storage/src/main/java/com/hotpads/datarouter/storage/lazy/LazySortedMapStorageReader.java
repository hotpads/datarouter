package com.hotpads.datarouter.storage.lazy;

import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.BaseLazyReader;
import com.hotpads.datarouter.storage.lazy.mixin.LazyMapStorageReaderMixin;
import com.hotpads.datarouter.storage.lazy.mixin.LazySortedStorageReaderMixin;

public class LazySortedMapStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseLazyReader<PK,D,SortedMapStorageReader<PK,D>>
implements LazySortedStorageReaderMixin<PK,D,SortedMapStorageReader<PK,D>>,
		LazyMapStorageReaderMixin<PK,D,SortedMapStorageReader<PK,D>>{

	public LazySortedMapStorageReader(SortedMapStorageReader<PK,D> storage){
		super(storage);
	}

}
