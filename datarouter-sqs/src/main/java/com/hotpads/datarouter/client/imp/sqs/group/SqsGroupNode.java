package com.hotpads.datarouter.client.imp.sqs.group;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.sqs.SqsClient;
import com.hotpads.datarouter.client.imp.sqs.SqsOpFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.op.scan.queue.PeekUntilEmptyQueueStorageScanner;
import com.hotpads.datarouter.op.scan.queue.PollUntilEmptyQueueStorageIterable;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class SqsGroupNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> 
implements PhysicalGroupQueueStorageNode<PK,D>{

	protected final DatarouterContext datarouterContext;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;
	protected final Lazy<String> queueUrl;

	public SqsGroupNode(DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
		super(params);
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
	
	public Lazy<String> getQueueUrl(){
		return queueUrl;
	}
	
	protected SqsClient getSqsClient(){
		return (SqsClient) datarouterContext.getClientPool().getClient(getClientId().getName());
	}
	
	public AmazonSQSClient getAmazonSqsClient(){
		return getSqsClient().getAmazonSqsClient();
	}
	
	//Writer
	
	@Override
	public void put(D databean, Config config){
		sqsOpFactory.makeGroupPutMultiOp(Collections.singleton(databean), config).call();
	}
	
	@Override
	public void putMulti(Collection<D> databeans, Config config){
		sqsOpFactory.makeGroupPutMultiOp(databeans, config).call();
	}
	
	@Override
	public void ack(QueueMessageKey key, Config config){
		sqsOpFactory.makeAckOp(key, config).call();
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		sqsOpFactory.makeAckMultiOp(keys, config).call();
	}
	
	//Reader
	
	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		return sqsOpFactory.makeGroupPeekMultiOp(config).call();
	}
	
	@Override
	public Iterable<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		return new ScannerIterable<>(new PeekUntilEmptyQueueStorageScanner<>(this, config));
	}
	
	//Reader + Writer
	
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
