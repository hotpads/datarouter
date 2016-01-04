package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * Methods for deleting from simple key/value storage systems, supporting similar methods to a HashMap.
 * 
 * See MapStorageReader for implementation notes.
 */
public interface MapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends StorageWriter<PK,D>{
	
	public static final String
		OP_put = "put",
		OP_putMulti = "putMulti",
		OP_delete = "delete",
		OP_deleteMulti = "deleteMulti",
		OP_deleteAll = "deleteAll"
		;
	
	
	void delete(PK key, Config config);
	void deleteMulti(Collection<PK> keys, Config config);
	void deleteAll(Config config);

	
	/*************** sub-interfaces ***********************/
	
	public interface MapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends Node<PK,D>, MapStorageWriter<PK,D>{
	}

	
	public interface PhysicalMapStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, MapStorageWriterNode<PK,D>{
	}
}
