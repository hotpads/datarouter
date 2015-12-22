package com.hotpads.datarouter.node.op.combo;

import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.node.op.combo.writer.IndexedSortedMapStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends IndexedMapStorage<PK,D>,
		SortedMapStorage<PK,D>,
		IndexedSortedMapStorageReader<PK,D>,
		IndexedSortedMapStorageWriter<PK,D>{

	public interface IndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedSortedMapStorage<PK,D>,
			IndexedMapStorageNode<PK,D>,
			SortedMapStorageNode<PK,D>,
			IndexedSortedMapStorageReaderNode<PK,D>,
			IndexedSortedMapStorageWriterNode<PK,D>{
	}
	public interface PhysicalIndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedSortedMapStorageNode<PK,D>,
			PhysicalIndexedMapStorageNode<PK,D>,
			PhysicalSortedMapStorageNode<PK,D>,
			PhysicalIndexedSortedMapStorageReaderNode<PK,D>,
			PhysicalIndexedSortedMapStorageWriterNode<PK,D>{
	}
}
