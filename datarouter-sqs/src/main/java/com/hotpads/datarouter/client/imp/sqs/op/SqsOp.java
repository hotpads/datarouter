package com.hotpads.datarouter.client.imp.sqs.op;

import java.util.concurrent.Callable;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.imp.sqs.SqsNode;
import com.hotpads.datarouter.client.imp.sqs.encode.SqsEncoder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class SqsOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		V>
implements Callable<V>{
	
	protected final Config config;
	protected final AmazonSQSClient amazonSqsClient;
	protected final String queueUrl;
	protected final SqsEncoder sqsEncoder;
	protected final Class<D> databeanType;
	
	public SqsOp(Config config, SqsNode<PK,D,F> sqsNode){
		this.config = Config.nullSafe(config);
		this.amazonSqsClient = sqsNode.getAmazonSqsClient();
		this.queueUrl = sqsNode.getQueueUrl().get();
		this.sqsEncoder = sqsNode.getSqsEncoder();
		this.databeanType = sqsNode.getDatabeanType();
	}

	@Override
	public V call(){
		//count
		return run();
	}
	
	protected abstract V run();
	
}
