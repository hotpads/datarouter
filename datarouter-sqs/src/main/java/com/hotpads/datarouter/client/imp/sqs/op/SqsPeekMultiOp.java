package com.hotpads.datarouter.client.imp.sqs.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.hotpads.datarouter.client.imp.sqs.SqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;

public class SqsPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,List<QueueMessage<PK,D>>>{


	public SqsPeekMultiOp(Config config, SqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
	}

	@Override
	protected List<QueueMessage<PK,D>> run(){
		Integer limit = config.getLimitOrUse(SqsNode.MAX_MESSAGES_PER_BATCH);
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(limit);
		if(config.getTimeoutMs() != null){
			request.setWaitTimeSeconds((int)Math.min(config.getTimeoutMs() / 1000, SqsNode.MAX_TIMEOUT_SECONDS));
		}
		ReceiveMessageResult result = amazonSqsClient.receiveMessage(request);
		if(result.getMessages().size() == 0){
			return Collections.emptyList();
		}
		List<QueueMessage<PK,D>> results = new ArrayList<>(result.getMessages().size());
		for(Message message : result.getMessages()){
			D databean = sqsEncoder.decode(message.getBody(), databeanType);
			results.add(new QueueMessage<>(message.getReceiptHandle().getBytes(), databean));
		}
		return results;
	}

}
