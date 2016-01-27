package com.hotpads.datarouter.op.scan.queue;

import java.util.Iterator;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;

public class PollUntilEmptyQueueStorageIterator<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
implements Iterator<D>{
	
	private final Iterator<QueueMessage<PK,D>> queueMessageIterator;
	private final QueueStorage<PK,D> queueStorage;
	
	public PollUntilEmptyQueueStorageIterator(QueueStorage<PK,D> queueStorage, Config config){
		this.queueStorage = queueStorage;
		this.queueMessageIterator = queueStorage.peekUntilEmpty(config).iterator();
	}

	@Override
	public boolean hasNext(){
		return queueMessageIterator.hasNext();
	}

	@Override
	public D next(){
		if(!hasNext()){
			return null;
		}
		QueueMessage<PK,D> message = queueMessageIterator.next();
		queueStorage.ack(message.getKey(), null);
		return message.getDatabean();
	}

	@Override
	public void remove(){
		queueMessageIterator.remove();
	}

}
