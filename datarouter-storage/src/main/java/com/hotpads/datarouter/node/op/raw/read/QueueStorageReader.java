package com.hotpads.datarouter.node.op.raw.read;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;

/**
 * Methods for reading from a message queue where each message contains a single Databean.
 */
public interface QueueStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{
	
	public static final String
			OP_peek = "peek",
			OP_peekMulti = "peekMulti",
			OP_peekUntilEmpty = "peekUntilEmpty"
			;
	
	
	QueueMessage<PK,D> peek(Config config);
	List<QueueMessage<PK,D>> peekMulti(Config config);
	Iterable<QueueMessage<PK,D>> peekUntilEmpty(Config config);
	
}
