package com.hotpads.datarouter.node.op.combo.reader;

import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageReader<PK,D>, IndexedStorageReader<PK,D>
{
	public interface IndexedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends MapStorageReaderNode<PK,D>, IndexedStorageReaderNode<PK,D>
	{
	}

	public interface PhysicalIndexedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalMapStorageReaderNode<PK,D>, PhysicalIndexedStorageReaderNode<PK,D>
	{
	}
}
