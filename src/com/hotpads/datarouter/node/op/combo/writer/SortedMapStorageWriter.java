package com.hotpads.datarouter.node.op.combo.writer;

import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageWriter<PK,D>, SortedStorageWriter<PK,D>
{
	public interface SortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends MapStorageWriterNode<PK,D>, SortedStorageWriterNode<PK,D>
	{
	}

	public interface PhysicalSortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalMapStorageWriterNode<PK,D>, PhysicalSortedStorageWriterNode<PK,D>
	{
	}
}
