package com.hotpads.datarouter.node.op.raw.write;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends NodeOps<PK,D>
{
	void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config);
//	void deleteRange(Key<D> start, boolean startInclusive, Key<D> end, boolean endInclusive, Config config);
	
	

	public interface SortedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends Node<PK,D>, SortedStorageWriter<PK,D>
	{
	}

	public interface PhysicalSortedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalNode<PK,D>, SortedStorageWriterNode<PK,D>
	{
	}
}
