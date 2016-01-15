package com.hotpads.datarouter.op.scan.queue;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.QueueStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

public class PeekUntilEmptyQueueStorageScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseBatchBackedScanner<QueueMessage<PK,D>,QueueMessage<PK,D>>{
	
	private final QueueStorageReader<PK,D> queueStorageReader;
	private final Config config;
	
	public PeekUntilEmptyQueueStorageScanner(QueueStorageReader<PK,D> queueStorageReader, Config config){
		this.queueStorageReader = queueStorageReader;
		this.config = config;
		this.currentBatchIndex = -1;
	}

	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		currentBatch = queueStorageReader.peekMulti(config);
		noMoreBatches = currentBatch.size() == 0;
	}

	@Override
	protected void setCurrentFromResult(QueueMessage<PK,D> result){
		current = result;
	}

}
