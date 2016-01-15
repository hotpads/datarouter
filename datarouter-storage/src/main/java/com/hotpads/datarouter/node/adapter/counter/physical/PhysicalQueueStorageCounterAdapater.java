package com.hotpads.datarouter.node.adapter.counter.physical;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.QueueStorageWriterCounterAdapter;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import com.hotpads.datarouter.node.op.raw.read.QueueStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;

public class PhysicalQueueStorageCounterAdapater<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalQueueStorageNode<PK,D>>
extends QueueStorageWriterCounterAdapter<PK,D,N>
implements PhysicalQueueStorageNode<PK,D>, PhysicalAdapterMixin<PK,D,N>{

	public PhysicalQueueStorageCounterAdapater(N backingNode){
		super(backingNode);
	}

	@Override
	public QueueMessage<PK,D> peek(Config config){
		counter.count(QueueStorageReader.OP_peek);
		return backingNode.peek(config);
	}

	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		counter.count(QueueStorageReader.OP_peekMulti);
		List<QueueMessage<PK,D>> messages = backingNode.peekMulti(config);
		counter.count(QueueStorageReader.OP_peekMulti + " messages", messages.size());
		return messages;
	}

	@Override
	public Iterable<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		counter.count(QueueStorageReader.OP_peekUntilEmpty);
		return backingNode.peekUntilEmpty(config);
	}

	@Override
	public D poll(Config config){
		counter.count(QueueStorage.OP_poll);
		return backingNode.poll(config);
	}

	@Override
	public List<D> pollMulti(Config config){
		counter.count(QueueStorage.OP_pollMulti);
		List<D> databeans = backingNode.pollMulti(config);
		counter.count(QueueStorage.OP_pollMulti + " databeans", databeans.size());
		return databeans;
	}

	@Override
	public Iterable<D> pollUntilEmpty(Config config){
		counter.count(QueueStorage.OP_pollUntilEmpty);
		return backingNode.pollUntilEmpty(config);
	}

}
