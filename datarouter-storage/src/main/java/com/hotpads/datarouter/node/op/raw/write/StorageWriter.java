package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * Methods for writing to any storage system.
 */
public interface StorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{
	
	public static final String
			OP_put = "put",
			OP_putMulti = "putMulti"
			;
	
	
	void put(D databean, Config config);
	void putMulti(Collection<D> databeans, Config config);
}
