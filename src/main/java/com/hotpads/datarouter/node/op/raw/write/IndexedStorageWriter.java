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

/**
 * Methods for writing to storage systems that provide secondary indexing.
 * 
 * This storage may be deprecated in favor of a future MultiIndexWriter.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 */
public interface IndexedStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	public static final String
		OP_indexDelete = "indexDelete",
		OP_deleteUnique = "deleteUnique",
		OP_deleteMultiUnique = "deleteMultiUnique";
	
	//TODO rename something different than MapStorageWriter.delete
	//
	// does not affect the same entry as MapStorageReader.lookup
	// Example: when a field is null affect only entry where field IS NULL (instead of accept all values for this field)
	void delete(Lookup<PK> lookup, Config config);
	
	void deleteUnique(UniqueKey<PK> uniqueKey, Config config);
	void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);
	
	

	public interface IndexedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, IndexedStorageWriter<PK,D>
	{
	}

	public interface PhysicalIndexedStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, IndexedStorageWriterNode<PK,D>
	{
	}
}
