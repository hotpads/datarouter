package com.hotpads.datarouter.node.op.raw.write;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * Methods for writing to storage mechanisms that keep databeans sorted by PrimaryKey.  Similar to java's TreeMap.
 *
 * See SortedStorageReader for possible implementations.
 */
public interface SortedStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	/*************** sub-interfaces ***********************/

	public interface SortedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, SortedStorageWriter<PK,D>{
	}


	public interface PhysicalSortedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, SortedStorageWriterNode<PK,D>{
	}

}
