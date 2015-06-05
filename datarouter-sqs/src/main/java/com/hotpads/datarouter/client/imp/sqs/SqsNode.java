package com.hotpads.datarouter.client.imp.sqs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
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
import com.hotpads.datarouter.op.scan.queue.PeekUntilEmptyQueueStorageScanner;
import com.hotpads.datarouter.op.scan.queue.PollUntilEmptyQueueStorageIterable;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> 
implements QueueStorage<PK,D>{
	
	//do not change, this is a limit from SQS
	private static final int MAX_MESSAGES_PER_BATCH = 10;
	private static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256*1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256*1024;
	
	private final DatarouterContext datarouterContext;
	private final SqsEncoder sqsEncoder;

	private final Lazy<String> queueUrl;

	public SqsNode(SqsEncoder sqsEncoder, DatarouterContext datarouterContext, 
			NodeParams<PK,D,F> params){
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
	public Client getClient(){
		return getSqsClient();
	}
	
	private String getOrCreateQueueUrl(){
		String queueName = getSqsClient().getSqsOptions().getNamespace() + "-" + getTableName();
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
		return getAmazonSqsClient().createQueue(createQueueRequest).getQueueUrl();
	}
	
	private SqsClient getSqsClient(){
		return (SqsClient) datarouterContext.getClientPool().getClient(getClientName());
	}
	
	private AmazonSQSClient getAmazonSqsClient(){
		return getSqsClient().getAmazonSqsClient();
	}
	
	//Writer
	
	@Override
	public void put(D databean, Config config){
		config = Config.nullSafe(config);
		String encodedDatabean = sqsEncoder.encode(databean);
		if(StringByteTool.getUtf8Bytes(encodedDatabean).length > MAX_BYTES_PER_MESSAGE){
			throw new SqsDataTooLargeException(databean);
		}
		SendMessageRequest request = new SendMessageRequest(queueUrl.get(), encodedDatabean);
		getAmazonSqsClient().sendMessage(request);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		config = Config.nullSafe(config);
		List<SendMessageBatchRequestEntry> entries = new ArrayList<>(MAX_MESSAGES_PER_BATCH);
		List<D> rejectedDatabeans = new ArrayList<>();
		int currentPayloadSize = 0;
		for(D databean : databeans){
			String encodedDatabean = sqsEncoder.encode(databean);
			int encodedDatabeanSize = StringByteTool.getUtf8Bytes(encodedDatabean).length;
			if(encodedDatabeanSize > MAX_BYTES_PER_MESSAGE){
				rejectedDatabeans.add(databean);
				continue;
			}
			if(currentPayloadSize + encodedDatabeanSize > MAX_BYTES_PER_PAYLOAD 
					|| entries.size() >= MAX_MESSAGES_PER_BATCH){
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
	}
	
	private void putBatch(List<SendMessageBatchRequestEntry> entries){
		SendMessageBatchRequest request = new SendMessageBatchRequest(queueUrl.get(), entries);
		getAmazonSqsClient().sendMessageBatch(request);
	}
	
	@Override
	public void ack(QueueMessageKey key, Config config){
		config = Config.nullSafe(config);
		String handle = StringByteTool.fromUtf8Bytes(key.getHandle());
		DeleteMessageRequest deleteRequest = new DeleteMessageRequest(queueUrl.get(), handle);
		getAmazonSqsClient().deleteMessage(deleteRequest);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		config = Config.nullSafe(config);
		if(keys.size() == 0){
			return;
		}
		List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<>(keys.size());
		for(QueueMessageKey key : keys){
			deleteEntries.add(new DeleteMessageBatchRequestEntry(UUID.randomUUID().toString(), new String(key
					.getHandle())));
		}
		DeleteMessageBatchRequest deleteRequest = new DeleteMessageBatchRequest(queueUrl.get(), deleteEntries);
		getAmazonSqsClient().deleteMessageBatch(deleteRequest);
	}
	
	// Reader
	
	@Override
	public QueueMessage<PK,D> peek(Config config){
		config = Config.nullSafe(config);
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl.get());
		Long timeoutMs = config.getTimeoutMs();
		if(timeoutMs == null){
			timeoutMs = 0L;
		}
		long timeWaitedMs = 0;
		do{
			long waitTimeMs = Math.min(timeoutMs - timeWaitedMs, MAX_TIMEOUT_SECONDS * 1000);
			timeWaitedMs += waitTimeMs;
			request.setWaitTimeSeconds((int) (waitTimeMs / 1000));
			ReceiveMessageResult result = getAmazonSqsClient().receiveMessage(request);
			if(result.getMessages().size() != 0){
				Message message = result.getMessages().get(0);
				D databean = sqsEncoder.decode(message.getBody(), getDatabeanType());
				return new QueueMessage<>(message.getReceiptHandle().getBytes(), databean);
			}
		}while(timeWaitedMs < timeoutMs);
		return null;
	}
	
	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		config = Config.nullSafe(config);
		Integer limit = config.getLimitOrUse(MAX_MESSAGES_PER_BATCH);
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl.get()).withMaxNumberOfMessages(limit);
		if(config.getTimeoutMs() != null){
			request.setWaitTimeSeconds((int)Math.max(config.getTimeoutMs() / 1000, MAX_TIMEOUT_SECONDS));
		}
		ReceiveMessageResult result = getAmazonSqsClient().receiveMessage(request);
		if(result.getMessages().size() == 0){
			return Collections.emptyList();
		}
		List<QueueMessage<PK,D>> results = new ArrayList<>(result.getMessages().size());
		for(Message message : result.getMessages()){
			D databean = sqsEncoder.decode(message.getBody(), getDatabeanType());
			results.add(new QueueMessage<>(message.getReceiptHandle().getBytes(), databean));
		}
		return results;
	}
	
	@Override
	public Iterable<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		return new ScannerIterable<>(new PeekUntilEmptyQueueStorageScanner<>(this, config));
	}
	
	// Reader + Writer
	
	@Override
	public D poll(Config config){
		QueueMessage<PK,D> message = peek(config);
		if(message == null){
			return null;
		}
		ack(message.getKey(), config);
		return message.getDatabean();
	}
	
	@Override
	public List<D> pollMulti(Config config){
		List<QueueMessage<PK,D>> results = peekMulti(config);
		ackMulti(QueueMessage.getKeys(results), config);
		return QueueMessage.getDatabeans(results);
	}
	
	@Override
	public Iterable<D> pollUntilEmpty(Config config){
		return new PollUntilEmptyQueueStorageIterable<>(this, config);
	}

}
