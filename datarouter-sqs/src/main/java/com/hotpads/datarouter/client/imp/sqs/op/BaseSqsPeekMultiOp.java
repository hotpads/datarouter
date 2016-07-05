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
		Long timeoutMs = config.getTimeoutMs();
		if(timeoutMs == null){
			timeoutMs = 0L;
		}
		long timeWaitedMs = 0;
		do{
			long waitTimeMs = Math.min(timeoutMs - timeWaitedMs, BaseSqsNode.MAX_TIMEOUT_SECONDS * 1000);
			timeWaitedMs += waitTimeMs;
			request.setWaitTimeSeconds((int) (waitTimeMs / 1000));
			try{
				ReceiveMessageResult result = amazonSqsClient.receiveMessage(request);
				if(result.getMessages().size() != 0){
					return extractDatabeans(result.getMessages());
				}
			}catch(AbortedException e){
				Thread.currentThread().interrupt();
			}
		}while(!Thread.currentThread().isInterrupted() && timeWaitedMs < timeoutMs);
		return new ArrayList<>();
	}

	protected abstract List<T> extractDatabeans(List<Message> messages);

	private ReceiveMessageRequest makeRequest(){
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
		long visibilityTimeoutMs = config.getVisibilityTimeoutMsOrUse(BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS);
		request.setVisibilityTimeout((int)Duration.ofMillis(visibilityTimeoutMs).getSeconds());
		request.setMaxNumberOfMessages(config.getLimitOrUse(BaseSqsNode.MAX_MESSAGES_PER_BATCH));
		return request;
	}
}
