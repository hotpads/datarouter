package com.hotpads.datarouter.client.imp.sqs.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.util.core.iterable.BatchingIterable;

public class SqsAckMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<QueueMessageKey> keys;

	public SqsAckMultiOp(Collection<QueueMessageKey> keys, Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.keys = keys;
	}

	@Override
	protected Void run(){
		for(List<QueueMessageKey> batch : new BatchingIterable<>(keys, BaseSqsNode.MAX_MESSAGES_PER_BATCH)){
			List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<>(batch.size());
			for(QueueMessageKey key : batch){
				deleteEntries.add(new DeleteMessageBatchRequestEntry(UUID.randomUUID().toString(), new String(key
						.getHandle())));
			}
			DeleteMessageBatchRequest deleteRequest = new DeleteMessageBatchRequest(queueUrl, deleteEntries);
			amazonSqsClient.deleteMessageBatch(deleteRequest);
		}
		return null;
	}

}
