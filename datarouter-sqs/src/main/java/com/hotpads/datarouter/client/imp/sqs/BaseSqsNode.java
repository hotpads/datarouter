package com.hotpads.datarouter.client.imp.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.Lazy;

public abstract class BaseSqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F> {
	
	//do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256*1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256*1024;

	protected final DatarouterContext datarouterContext;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;
	protected final Lazy<String> queueUrl;

	public BaseSqsNode(DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
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

}
