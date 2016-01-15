package com.hotpads.datarouter.op.scan.queue;

import java.util.Iterator;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PollUntilEmptyQueueStorageIterable<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
implements Iterable<D>{

	private final QueueStorage<PK,D> queueStorage;
	private final Config config;

	public PollUntilEmptyQueueStorageIterable(QueueStorage<PK,D> queueStorage, Config config){
		this.queueStorage = queueStorage;
		this.config = config;
	}

	@Override
	public Iterator<D> iterator(){
		return new PollUntilEmptyQueueStorageIterator<>(queueStorage, config);
	}

}
