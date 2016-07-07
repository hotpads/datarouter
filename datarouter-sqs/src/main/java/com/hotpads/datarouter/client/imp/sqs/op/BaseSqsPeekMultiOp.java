package com.hotpads.datarouter.client.imp.sqs.op;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public abstract class BaseSqsPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T>
extends SqsOp<PK,D,F,List<T>>{

	public BaseSqsPeekMultiOp(Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
	}

	@Override
	protected final List<T> run(){
		ReceiveMessageRequest request = makeRequest();
		try{
			ReceiveMessageResult result = amazonSqsClient.receiveMessage(request);
			if(DrCollectionTool.notEmpty(result.getMessages())){
				return extractDatabeans(result.getMessages());
			}
		}catch(AbortedException e){
			Thread.currentThread().interrupt();
		}
		return new ArrayList<>();
	}

	protected abstract List<T> extractDatabeans(List<Message> messages);


	private ReceiveMessageRequest makeRequest(){
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);

		//waitTime
		long configTimeoutMs = config.getVisibilityTimeoutMsOrUse(Long.MAX_VALUE);
		long waitTimeMs = Math.min(configTimeoutMs, BaseSqsNode.MAX_TIMEOUT_SECONDS * 1000);
		int waitTimeSeconds = (int)Duration.ofMillis(waitTimeMs).getSeconds();
		request.setWaitTimeSeconds(waitTimeSeconds);

		//visibility timeout
		long visibilityTimeoutMs = config.getVisibilityTimeoutMsOrUse(BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS);
		request.setVisibilityTimeout((int)Duration.ofMillis(visibilityTimeoutMs).getSeconds());

		//max messages
		request.setMaxNumberOfMessages(config.getLimitOrUse(BaseSqsNode.MAX_MESSAGES_PER_BATCH));

		return request;
	}
}
