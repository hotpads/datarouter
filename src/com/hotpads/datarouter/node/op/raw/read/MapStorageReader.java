package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.mav.Mav;

public interface MapStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	
	boolean exists(PK key, Config config);
	
	D get(PK key, Config config);
	List<D> getMulti(Collection<PK> keys, Config config);
	List<D> getAll(Config config);
	
	List<PK> getKeys(final Collection<PK> keys, final Config config);
//	List<K> getAllKeys(Config config);
	
		
	public interface MapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, MapStorageReader<PK,D>
	{
	}	

	public interface PhysicalMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, MapStorageReaderNode<PK,D>
	{
	}
	
	public interface MapStorageReaderHttpNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
		Mav exists();
		Mav get();
		Mav getMulti();
		Mav getAll();
		Mav getKeys();
	}
}
