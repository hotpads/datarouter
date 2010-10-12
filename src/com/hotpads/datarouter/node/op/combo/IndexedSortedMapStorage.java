package com.hotpads.datarouter.node.op.combo;

import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.node.op.combo.writer.IndexedSortedMapStorageWriter;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

//TODO who knows (try to clean up)

public interface IndexedSortedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorage<PK,D>, SortedStorage<PK,D>, IndexedStorage<PK,D>, 
		IndexedMapStorage<PK,D>,
		SortedMapStorage<PK,D>,
		IndexedSortedMapStorageReader<PK,D>, IndexedSortedMapStorageWriter<PK,D>
{
	public interface IndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends IndexedSortedMapStorage<PK,D>,
			IndexedMapStorageNode<PK,D>,
			SortedMapStorageNode<PK,D>,
			IndexedSortedMapStorageReaderNode<PK,D>, IndexedSortedMapStorageWriterNode<PK,D>
	{
	}
	public interface PhysicalIndexedSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalNode<PK,D>, IndexedSortedMapStorageNode<PK,D>,
			PhysicalIndexedMapStorageNode<PK,D>,
			PhysicalSortedMapStorageNode<PK,D>,
			PhysicalIndexedSortedMapStorageReaderNode<PK,D>, PhysicalIndexedSortedMapStorageWriterNode<PK,D>
	{
	}
}
