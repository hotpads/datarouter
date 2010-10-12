package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface IndexedStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends NodeOps<PK,D>
{
	void delete(Lookup<PK> lookup, Config config);
	
	void deleteUnique(UniqueKey<PK> uniqueKey, Config config);
	void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);
	
	

	public interface IndexedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends Node<PK,D>, IndexedStorageWriter<PK,D>
	{
	}

	public interface PhysicalIndexedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalNode<PK,D>, IndexedStorageWriterNode<PK,D>
	{
	}
}
