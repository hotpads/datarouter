package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * Methods for reading from simple key/value storage systems, supporting similar methods to a HashMap.
 * 
 * See MapStorageReader for implementation notes.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 */
public interface MapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	public static final String
		OP_put = "put",
		OP_putMulti = "putMulti",
		OP_delete = "delete",
		OP_deleteMulti = "deleteMulti",
		OP_deleteAll = "deleteAll";
	
	
	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
	
	void delete(PK key, Config config);
	void deleteMulti(Collection<PK> keys, Config config);
	void deleteAll(Config config);
	
	

	public interface MapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, MapStorageWriter<PK,D>
	{
	}

	public interface PhysicalMapStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, MapStorageWriterNode<PK,D>
	{
	}
}
