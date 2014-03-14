package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface IndexedStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	public static final String
		OP_count = "count",
		OP_lookupUnique = "lookupUnique",
		OP_lookupMultiUnique = "lookupMultiUnique",
		OP_lookup = "lookup",
		OP_lookupMulti = "lookupMulti";
	
	Long count(Lookup<PK> lookup, Config config);
	
	D lookupUnique(UniqueKey<PK> uniqueKey, Config config);
	List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);
	
	List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config);
	//TODO rename lookupMulti
	List<D> lookup(Collection<? extends Lookup<PK>> lookup, Config config);
	
	

	public interface IndexedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, IndexedStorageReader<PK,D>
	{
	}
	
	public interface PhysicalIndexedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, IndexedStorageReaderNode<PK,D>
	{
	}
}
