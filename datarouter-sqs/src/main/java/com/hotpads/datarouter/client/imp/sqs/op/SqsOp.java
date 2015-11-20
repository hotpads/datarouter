package com.hotpads.datarouter.client.imp.sqs.op;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
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
	protected final Supplier<D> databeanSupplier;
	protected final F fielder;
	protected final StringDatabeanCodec codec;

	public SqsOp(Config config, BaseSqsNode<PK,D,F> sqsNode){
		this.config = Config.nullSafe(config);
		this.amazonSqsClient = sqsNode.getAmazonSqsClient();
		this.queueUrl = sqsNode.getQueueUrl().get();
		this.databeanSupplier = sqsNode.getFieldInfo().getDatabeanSupplier();
		this.fielder = sqsNode.getFieldInfo().getSampleFielder();
		this.codec = fielder.getStringDatabeanCodec();
	}

	@Override
	public V call(){
		//count
		return run();
	}

	protected abstract V run();

}
