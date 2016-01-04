package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter.PhysicalQueueStorageWriterNode;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;

public class QueueStorageWriterCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalQueueStorageWriterNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements QueueStorageWriter<PK,D>{

	public QueueStorageWriterCounterAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		counter.count(QueueStorageWriter.OP_ack);
		backingNode.ack(key, config);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		counter.count(QueueStorageWriter.OP_ackMulti);
		counter.count(QueueStorageWriter.OP_ackMulti + " keys", keys.size());
		backingNode.ackMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config){
		counter.count(StorageWriter.OP_put);
		backingNode.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		counter.count(StorageWriter.OP_putMulti);
		counter.count(StorageWriter.OP_putMulti + " databeans", databeans.size());
		backingNode.putMulti(databeans, config);
	}

}
