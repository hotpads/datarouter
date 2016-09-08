package com.hotpads.datarouter.client.imp.kinesis.op;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.client.imp.kinesis.KclZillowReadOnlyLitLzgClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class KinesisOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		V>
implements Callable<V>{

	protected final Config config;
	protected final AmazonKinesisClient amazonKinesisClient;
	protected final AWSCredentialsProvider awsCredentialsProvider;
	protected final String streamName;
	protected final String regionName;
	protected final String kclNamespace;
	protected final Supplier<D> databeanSupplier;
	protected final F fielder;
	protected final StringDatabeanCodec codec;
	protected final DatabeanFieldInfo<PK,D,F> fieldInfo;

	public KinesisOp(Config config, BaseKinesisNode<PK,D,F> kinesisNode){
		this.config = Config.nullSafe(config);
		this.amazonKinesisClient = kinesisNode.getAmazonKinesisClient();
		this.awsCredentialsProvider = kinesisNode.getAwsCredentialsProvider();
		this.streamName = kinesisNode.getStreamName();
		this.regionName = kinesisNode.getRegionName();
		this.kclNamespace = ((KclZillowReadOnlyLitLzgClient)kinesisNode.getClient()).getKinesisOptions()
				.getKclNamespace();
		this.databeanSupplier = kinesisNode.getFieldInfo().getDatabeanSupplier();
		this.fielder = kinesisNode.getFieldInfo().getSampleFielder();
		this.codec = fielder.getStringDatabeanCodec();
		this.fieldInfo = kinesisNode.getFieldInfo();
	}

	@Override
	public V call(){
		//count
		return run();
	}

	protected abstract V run();

}
