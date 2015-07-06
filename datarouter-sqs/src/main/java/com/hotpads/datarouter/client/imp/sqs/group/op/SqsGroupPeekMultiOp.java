package com.hotpads.datarouter.client.imp.sqs.group.op;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.hotpads.datarouter.client.imp.sqs.SqsNode;
import com.hotpads.datarouter.client.imp.sqs.group.SqsGroupNode;
import com.hotpads.datarouter.client.imp.sqs.op.SqsOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsGroupPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends SqsOp<PK,D,F,GroupQueueMessage<PK,D>>{

	public SqsGroupPeekMultiOp(Config config, SqsGroupNode<PK,D,F> sqsNode){
		super(config, sqsNode);
	}

	@Override
	protected GroupQueueMessage<PK,D> run(){
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
		request.setMaxNumberOfMessages(1);
		Long timeoutMs = config.getTimeoutMs();
		if(timeoutMs == null){
			timeoutMs = 0L;
		}
		long timeWaitedMs = 0;
		do{
			long waitTimeMs = Math.min(timeoutMs - timeWaitedMs, SqsNode.MAX_TIMEOUT_SECONDS * 1000);
			timeWaitedMs += waitTimeMs;
			request.setWaitTimeSeconds((int) (waitTimeMs / 1000));
			try{
				ReceiveMessageResult result = amazonSqsClient.receiveMessage(request);
				if(result.getMessages().size() != 0){
					Message message = result.getMessages().get(0);
					List<D> databeans = codec.fromStringMulti(message.getBody(), fielder, databeanType);
					byte[] receiptHandle = StringByteTool.getUtf8Bytes(message.getReceiptHandle());
					return new GroupQueueMessage<>(receiptHandle, databeans);
				}
			}catch(AbortedException e){
				Thread.currentThread().interrupt();
			}
		}while(timeWaitedMs < timeoutMs && !Thread.currentThread().isInterrupted());
		return new GroupQueueMessage<>(new byte[0], new ArrayList<D>());
	}

}
