package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;

/**
 * Methods to acknowledge processing of a queue message so that the messaging service can safely delete the message.
 */
public interface QueueStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends StorageWriter<PK,D>{
	
	public static final String
			OP_ack = "ack",
			OP_ackMulti = "ackMulti"
			;
	
	
	void ack(QueueMessageKey key, Config config);
	void ackMulti(Collection<QueueMessageKey> keys, Config config);
	

	/*************** sub-interfaces ***********************/
	
	public interface PhysicalQueueStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends QueueStorageWriter<PK,D>,PhysicalNode<PK,D>{
	}
}
