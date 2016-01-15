package com.hotpads.datarouter.node.op.combo.reader;

import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedMapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends MapStorageReader<PK,D>, SortedStorageReader<PK,D>{

	public interface SortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends MapStorageReaderNode<PK,D>, SortedStorageReaderNode<PK,D>, SortedMapStorageReader<PK,D>{
	}

	public interface PhysicalSortedMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalMapStorageReaderNode<PK,D>, PhysicalSortedStorageReaderNode<PK,D>, SortedMapStorageReaderNode<PK,D>{
	}
}
