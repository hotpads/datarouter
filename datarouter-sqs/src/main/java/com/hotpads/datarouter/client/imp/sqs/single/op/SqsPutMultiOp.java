package com.hotpads.datarouter.client.imp.sqs.single.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.op.SqsOp;
import com.hotpads.datarouter.client.imp.sqs.single.SqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<D> databeans;

	public SqsPutMultiOp(Collection<D> databeans, Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.databeans = databeans;
	}

	@Override
	protected Void run(){
		List<SendMessageBatchRequestEntry> entries = new ArrayList<>(SqsNode.MAX_MESSAGES_PER_BATCH);
		List<D> rejectedDatabeans = new ArrayList<>();
		int currentPayloadSize = 0;
		for(D databean : databeans){
			String encodedDatabean = codec.toString(databean, fielder);
			int encodedDatabeanSize = StringByteTool.getUtf8Bytes(encodedDatabean).length;
			if(encodedDatabeanSize > SqsNode.MAX_BYTES_PER_MESSAGE){
				rejectedDatabeans.add(databean);
				continue;
			}
			if(currentPayloadSize + encodedDatabeanSize > SqsNode.MAX_BYTES_PER_PAYLOAD
					|| entries.size() >= SqsNode.MAX_MESSAGES_PER_BATCH){
				putBatch(entries);
				entries = new ArrayList<>();
				currentPayloadSize = 0;
			}
			entries.add(new SendMessageBatchRequestEntry(UUID.randomUUID().toString(), encodedDatabean));
			currentPayloadSize += encodedDatabeanSize;
		}
		if(entries.size() > 0){
			putBatch(entries);
		}
		if(rejectedDatabeans.size() > 0){
			throw new SqsDataTooLargeException().withRejectedDatabeans(rejectedDatabeans);
		}
		return null;
	}

	private void putBatch(List<SendMessageBatchRequestEntry> entries){
		SendMessageBatchRequest request = new SendMessageBatchRequest(queueUrl, entries);
		amazonSqsClient.sendMessageBatch(request);
	}
}
