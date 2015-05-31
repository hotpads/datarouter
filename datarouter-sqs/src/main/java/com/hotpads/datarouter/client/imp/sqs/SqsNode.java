package com.hotpads.datarouter.client.imp.sqs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.sqs.encode.SqsEncoder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.iterable.BatchingIterable;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> 
implements QueueStorage<PK,D>{
	
	//do not raise, this is a limit from SQS
	private static final int MAX_MESSAGES_PER_BATCH = 10;
	
	private final DatarouterContext datarouterContext;
	private final SqsEncoder sqsEncoder;
	
	private final Lazy<String> queueUrl;

	SqsNode(SqsEncoder sqsEncoder, DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
		super(params);
		this.sqsEncoder = sqsEncoder;
		this.datarouterContext = datarouterContext;
		
		this.queueUrl = new Lazy<String>(){
			
			@Override
			protected String load(){
				return getOrCreateQueueUrl();
			}
		};
	}

	@Override
	public void put(D databean, Config config){
		SendMessageRequest request = new SendMessageRequest(queueUrl.get(), sqsEncoder.encode(databean));
		getAmazonSqsClient().sendMessage(request);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		for(List<D> databeanBatch : new BatchingIterable<>(databeans, MAX_MESSAGES_PER_BATCH)){
			List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
			for(D databean : databeanBatch){
				entries.add(new SendMessageBatchRequestEntry().withMessageBody(sqsEncoder.encode(databean)));
			}
			SendMessageBatchRequest request = new SendMessageBatchRequest(queueUrl.get(), entries);
			getAmazonSqsClient().sendMessageBatch(request);
		}
	}

	@Override
	public D poll(Config config){
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl.get());
		ReceiveMessageResult result = getAmazonSqsClient().receiveMessage(request);
		if(result.getMessages().size() == 0){
			return null;
		}
		Message message = result.getMessages().get(0);
		DeleteMessageRequest deleteRequest = new DeleteMessageRequest(queueUrl.get(), message.getReceiptHandle());
		getAmazonSqsClient().deleteMessage(deleteRequest);
		return sqsEncoder.decode(message.getBody(), getDatabeanType());
	}

	@Override
	public Client getClient(){
		return getSqsClient();
	}
	
	private String getOrCreateQueueUrl(){
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(getTableName());
		return getAmazonSqsClient().createQueue(createQueueRequest).getQueueUrl();
	}
	
	private SqsClient getSqsClient(){
		return (SqsClient) datarouterContext.getClientPool().getClient(getClientName());
	}
	
	private AmazonSQSClient getAmazonSqsClient(){
		return getSqsClient().getAmazonSqsClient();
	}

}
