package com.hotpads.datarouter.node.op.raw;

import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends SortedStorageReader<PK,D>, SortedStorageWriter<PK,D>
{
	public interface SortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedStorage<PK,D>,
			SortedStorageReaderNode<PK,D>, SortedStorageWriterNode<PK,D>
	{
	}
	
	public interface PhysicalSortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends SortedStorageNode<PK,D>,
			PhysicalSortedStorageReaderNode<PK,D>, PhysicalSortedStorageWriterNode<PK,D>
	{
	}
}
