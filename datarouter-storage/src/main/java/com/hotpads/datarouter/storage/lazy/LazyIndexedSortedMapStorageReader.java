package com.hotpads.datarouter.storage.lazy;

import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.lazy.base.BaseLazyReader;
import com.hotpads.datarouter.storage.lazy.mixin.LazyIndexedStorageReaderMixin;
import com.hotpads.datarouter.storage.lazy.mixin.LazyMapStorageReaderMixin;
import com.hotpads.datarouter.storage.lazy.mixin.LazySortedStorageReaderMixin;

public class LazyIndexedSortedMapStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>>
extends BaseLazyReader<PK,D,IndexedSortedMapStorageReader<PK,D>>
implements LazySortedStorageReaderMixin<PK,D,IndexedSortedMapStorageReader<PK,D>>,
		LazyIndexedStorageReaderMixin<PK,D,IndexedSortedMapStorageReader<PK,D>>,
		LazyMapStorageReaderMixin<PK,D,IndexedSortedMapStorageReader<PK,D>>{

	public LazyIndexedSortedMapStorageReader(IndexedSortedMapStorageReader<PK,D> storage){
		super(storage);
	}

}
