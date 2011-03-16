package com.hotpads.datarouter.node.op.combo.writer;

import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.IndexedStorageWriterNode;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageWriter<PK,D>, IndexedStorageWriter<PK,D>
{
	public interface IndexedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends MapStorageWriterNode<PK,D>, IndexedStorageWriterNode<PK,D>
	{
	}

	public interface PhysicalIndexedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalMapStorageWriterNode<PK,D>, PhysicalIndexedStorageWriterNode<PK,D>
	{
	}
}
