package com.hotpads.datarouter.client.imp.kinesis;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.ByteUnitTool;

public abstract class BaseKinesisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements StorageWriter<PK,D>{

	//do not change, this is a limit from Kinesis
	public static final int MAX_BYTES_PER_RECORD = (int)(1*ByteUnitTool.MiB);

	private final Datarouter datarouter;
	private final String streamName;
	private final String regionName;
	protected final KinesisOpFactory<PK,D,F> kinesisOpFactory;

	public BaseKinesisNode(Datarouter datarouter, NodeParams<PK,D,F> params){
		super(params);
		this.datarouter = datarouter;
		this.streamName = params.getStreamName();
		this.regionName = params.getRegionName();
		this.kinesisOpFactory = new KinesisOpFactory<>(this);
	}

//	private String getOrMakeStreamName(NodeParams<PK,D,F> params){
//		return params.getStreamName();
//		if(getKinesisClient().getKinesisOptions().getStreamName()!=null){
//			return getKinesisClient().getKinesisOptions().getStreamName();
//		}
//		String prefix = getKinesisClient().getKinesisOptions().getNamespace();
//		if(prefix != null && !prefix.isEmpty()){
//			prefix+="-";
//		}
//		String streamName = prefix + getTableName();
//		return streamName;
//	}

	@Override
	public Client getClient(){
		return getKinesisClient();
	}

	public String getStreamName(){
		return streamName;
	}

	public String getRegionName(){
		return regionName;
	}

	private KclZillowReadOnlyLitLzgClient getKinesisClient(){
		return (KclZillowReadOnlyLitLzgClient) datarouter.getClientPool().getClient(getClientId().getName());
	}

	public AmazonKinesisClient getAmazonKinesisClient(){
		return getKinesisClient().getAmazonKinesisClient();
	}

	public AWSCredentialsProvider getAwsCredentialsProvider(){
		return getKinesisClient().getAwsCredentialsProvider();
	};
}
