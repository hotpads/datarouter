package com.hotpads.datarouter.client.imp.sqs.op;

import java.util.concurrent.Callable;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.imp.sqs.SqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.StringDatabeanEncoder;
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
	protected final Class<D> databeanType;
	protected final F fielder;
	protected final StringDatabeanEncoder encoder;
	
	public SqsOp(Config config, SqsNode<PK,D,F> sqsNode){
		this.config = Config.nullSafe(config);
		this.amazonSqsClient = sqsNode.getAmazonSqsClient();
		this.queueUrl = sqsNode.getQueueUrl().get();
		this.databeanType = sqsNode.getDatabeanType();
		this.fielder = sqsNode.getFieldInfo().getSampleFielder();
		this.encoder = fielder.getStringDatabeanEncoder();
	}

	@Override
	public V call(){
		//count
		return run();
	}
	
	protected abstract V run();
	
}
