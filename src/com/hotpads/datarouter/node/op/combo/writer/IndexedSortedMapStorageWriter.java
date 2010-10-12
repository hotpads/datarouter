package com.hotpads.datarouter.node.op.combo.writer;

import com.hotpads.datarouter.node.op.combo.writer.SortedMapStorageWriter.PhysicalSortedMapStorageWriterNode;
import com.hotpads.datarouter.node.op.combo.writer.SortedMapStorageWriter.SortedMapStorageWriterNode;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageWriter<PK,D>, SortedStorageWriter<PK,D>, IndexedStorageWriter<PK,D>
{	
	public interface IndexedSortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends MapStorageWriterNode<PK,D>, SortedStorageWriter<PK,D>, IndexedStorageWriter<PK,D>,
			SortedMapStorageWriterNode<PK,D>
	{
	}
	public interface PhysicalIndexedSortedMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalMapStorageWriterNode<PK,D>, PhysicalSortedStorageWriterNode<PK,D>, PhysicalIndexedStorageWriterNode<PK,D>,
			PhysicalSortedMapStorageWriterNode<PK,D>
	{
	}
}
