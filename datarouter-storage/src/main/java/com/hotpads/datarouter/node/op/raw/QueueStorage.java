package com.hotpads.datarouter.node.op.raw;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.QueueStorageReader;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A wrapper class including a variety of poll() methods. Poll lets you consume a message from a queue in one call,
 * rather than having to peek() and ack().  Of course, it can be dangerous to ack the message before successfully
 * processing it, but sometimes that is ok.
 */
public interface QueueStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends QueueStorageWriter<PK,D>, QueueStorageReader<PK,D>{
	
	public static final String
			OP_poll = "poll",
			OP_pollMulti = "pollMulti",
			OP_pollUntilEmpty = "pollUntilEmpty"
			;
	
	
	D poll(Config config);
	List<D> pollMulti(Config config);
	Iterable<D> pollUntilEmpty(Config config);
	

	/*************** sub-interfaces ***********************/
	
	public interface PhysicalQueueStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends QueueStorage<PK,D>, PhysicalQueueStorageWriterNode<PK,D>{
	}
}
