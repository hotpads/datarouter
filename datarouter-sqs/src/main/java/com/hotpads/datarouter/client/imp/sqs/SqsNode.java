package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
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
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> 
implements QueueStorage<PK,D>{
	
	//do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256*1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256*1024;
	
	private final DatarouterContext datarouterContext;
	private final SqsEncoder sqsEncoder;
	private final SqsOpFactory<PK,D,F> sqsOpFactory;
	private final Lazy<String> queueUrl;

	public SqsNode(SqsEncoder sqsEncoder, DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
		super(params);
		this.sqsEncoder = sqsEncoder;
		this.datarouterContext = datarouterContext;
		this.queueUrl = new Lazy<String>(){
			
			@Override
			protected String load(){
				return getOrCreateQueueUrl();
			}
		};
		this.sqsOpFactory = new SqsOpFactory<>(this);
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
	
	public AmazonSQSClient getAmazonSqsClient(){
		return getSqsClient().getAmazonSqsClient();
	}
	
	public Lazy<String> getQueueUrl(){
		return queueUrl;
	}
	
	public SqsEncoder getSqsEncoder(){
		return sqsEncoder;
	}
	
	//Writer
	
	@Override
	public void put(D databean, Config config){
		sqsOpFactory.makePutOp(databean, config).call();
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		sqsOpFactory.makePutMultiOp(databeans, config).call();
	}
	
	@Override
	public void ack(QueueMessageKey key, Config config){
		sqsOpFactory.makeAckOp(key, config).call();
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		sqsOpFactory.makeAckMultiOp(keys, config).call();
	}
	
	// Reader
	
	@Override
	public QueueMessage<PK,D> peek(Config config){
		config = Config.nullSafe(config).setLimit(1);
		return DrCollectionTool.getFirst(sqsOpFactory.makePeekMultiOp(config).call());
	}
	
	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		return sqsOpFactory.makePeekMultiOp(config).call();
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
