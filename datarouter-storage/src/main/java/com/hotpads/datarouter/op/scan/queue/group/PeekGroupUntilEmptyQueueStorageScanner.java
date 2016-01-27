package com.hotpads.datarouter.op.scan.queue.group;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.GroupQueueStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

public class PeekGroupUntilEmptyQueueStorageScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseBatchBackedScanner<GroupQueueMessage<PK,D>,GroupQueueMessage<PK,D>>{
	
	private final GroupQueueStorageReader<PK,D> queueStorageReader;
	private final Config config;
	
	public PeekGroupUntilEmptyQueueStorageScanner(GroupQueueStorageReader<PK,D> queueStorageReader, Config config){
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
	protected void setCurrentFromResult(GroupQueueMessage<PK,D> result){
		current = result;
	}

}
