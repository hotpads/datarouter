package com.hotpads.datarouter.node.op.combo;

import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.node.op.combo.writer.SortedMapStorageWriter;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorage<PK,D>, SortedStorage<PK,D>, 
		SortedMapStorageReader<PK,D>, SortedMapStorageWriter<PK,D>
{
	public interface SortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedMapStorage<PK,D>, 
			MapStorageNode<PK,D>, SortedStorageNode<PK,D>, 
			SortedMapStorageReaderNode<PK,D>, SortedMapStorageWriterNode<PK,D>
	{
	}
	public interface PhysicalSortedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedMapStorageNode<PK,D>,
			PhysicalMapStorageNode<PK,D>, PhysicalSortedStorageNode<PK,D>, 
			PhysicalSortedMapStorageReaderNode<PK,D>, PhysicalSortedMapStorageWriterNode<PK,D>
	{
	}
}
