package com.hotpads.datarouter.node.op.combo.writer;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends SortedMapStorageWriter<PK,D>, IndexedMapStorageWriter<PK,D>{

	public interface IndexedSortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedSortedMapStorageWriter<PK,D>, SortedMapStorageWriterNode<PK,D>, IndexedMapStorageWriterNode<PK,D>{
	}

	public interface PhysicalIndexedSortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedSortedMapStorageWriterNode<PK,D>,
			PhysicalIndexedStorageWriterNode<PK,D>,
			PhysicalSortedMapStorageWriterNode<PK,D>{
	}

}
