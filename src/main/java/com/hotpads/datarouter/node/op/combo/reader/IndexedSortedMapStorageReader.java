package com.hotpads.datarouter.node.op.combo.reader;

import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.IndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.PhysicalIndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageReader<PK,D>, SortedStorageReader<PK,D>, IndexedStorageReader<PK,D>,
		SortedMapStorageReader<PK,D>,
		IndexedMapStorageReader<PK,D>
{	
	public interface IndexedSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends MapStorageReaderNode<PK,D>, SortedStorageReaderNode<PK,D>, IndexedStorageReaderNode<PK,D>,
			SortedMapStorageReaderNode<PK,D>,
			IndexedMapStorageReaderNode<PK,D>
	{
	}
	public interface PhysicalIndexedSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalMapStorageReaderNode<PK,D>, PhysicalSortedStorageReaderNode<PK,D>, PhysicalIndexedStorageReaderNode<PK,D>,
			PhysicalSortedMapStorageReaderNode<PK,D>,
			PhysicalIndexedMapStorageReaderNode<PK,D>
	{
	}
}