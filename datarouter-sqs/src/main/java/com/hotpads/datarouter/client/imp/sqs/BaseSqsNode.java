package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.util.core.concurrent.Lazy;

public abstract class BaseSqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements QueueStorageWriter<PK,D>{

	//do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256*1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256*1024;

	private final Datarouter datarouter;
	private final Lazy<String> queueUrl;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;

	public BaseSqsNode(Datarouter datarouter, NodeParams<PK,D,F> params){
		super(params);
		this.datarouter = datarouter;
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

	private SqsClient getSqsClient(){
		return (SqsClient) datarouter.getClientPool().getClient(getClientId().getName());
	}

	public AmazonSQSClient getAmazonSqsClient(){
		return getSqsClient().getAmazonSqsClient();
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		sqsOpFactory.makeAckOp(key, config).call();
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		sqsOpFactory.makeAckMultiOp(keys, config).call();
	}
}
