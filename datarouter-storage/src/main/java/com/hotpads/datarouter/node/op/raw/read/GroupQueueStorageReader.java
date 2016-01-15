package com.hotpads.datarouter.node.op.raw.read;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;

public interface GroupQueueStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends NodeOps<PK,D>{

	public static final String
			OP_peek = "peek",
			OP_peekMulti = "peekMulti",
			OP_peekUntilEmpty = "peekUntilEmpty"
			;

	GroupQueueMessage<PK,D> peek(Config config);
	List<GroupQueueMessage<PK,D>> peekMulti(Config config);
	Iterable<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config);

}
