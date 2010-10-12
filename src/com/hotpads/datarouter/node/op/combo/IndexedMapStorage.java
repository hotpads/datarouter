package com.hotpads.datarouter.node.op.combo;

import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader;
import com.hotpads.datarouter.node.op.combo.writer.IndexedMapStorageWriter;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedMapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorage<PK,D>, IndexedStorage<PK,D>, 
		IndexedMapStorageReader<PK,D>, IndexedMapStorageWriter<PK,D>
{
	public interface IndexedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends IndexedMapStorage<PK,D>,
			MapStorageNode<PK,D>, IndexedStorageNode<PK,D>,
			IndexedMapStorageReaderNode<PK,D>, IndexedMapStorageWriterNode<PK,D>
	{
	}
	public interface PhysicalIndexedMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends IndexedMapStorageNode<PK,D>,
			PhysicalMapStorageNode<PK,D>, PhysicalIndexedStorageNode<PK,D>,
			PhysicalIndexedMapStorageReaderNode<PK,D>, PhysicalIndexedMapStorageWriterNode<PK,D>
	{
	}
}
