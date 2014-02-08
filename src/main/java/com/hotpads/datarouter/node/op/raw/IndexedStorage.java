package com.hotpads.datarouter.node.op.raw;

import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends IndexedStorageReader<PK,D>, IndexedStorageWriter<PK,D>
{
	public interface IndexedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends IndexedStorageReaderNode<PK,D>, IndexedStorageWriterNode<PK,D>
	{
	}

	public interface PhysicalIndexedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalIndexedStorageReaderNode<PK,D>, PhysicalIndexedStorageWriterNode<PK,D>
	{
	}
}
