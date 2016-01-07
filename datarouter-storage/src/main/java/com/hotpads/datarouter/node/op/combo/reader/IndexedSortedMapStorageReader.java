package com.hotpads.datarouter.node.op.combo.reader;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends SortedMapStorageReader<PK,D>, IndexedMapStorageReader<PK,D>{

	public interface IndexedSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedMapStorageReaderNode<PK,D>, IndexedMapStorageReaderNode<PK,D>, IndexedSortedMapStorageReader<PK,D>{
	}

	public interface PhysicalIndexedSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedSortedMapStorageReaderNode<PK,D>,
			PhysicalSortedMapStorageReaderNode<PK,D>,
			PhysicalIndexedMapStorageReaderNode<PK,D>{
	}
}
