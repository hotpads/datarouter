package com.hotpads.datarouter.client.imp.kinesis.op;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import com.hotpads.datarouter.client.imp.kinesis.node.BaseKinesisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberConfig;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public class KinesisOpFactory<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final BaseKinesisNode<PK,D,F> kinesisNode;

	public KinesisOpFactory(BaseKinesisNode<PK,D,F> kinesisNode){
		this.kinesisNode = kinesisNode;
	}

	public KinesisOp<PK,D,F,BlockingQueue<StreamRecord<PK,D>>> makeStreamSubscriberOp(
			DatarouterStreamSubscriberConfig streamSubscriberConfig,
			Config config){
		switch(streamSubscriberConfig.getDrInitialPositionInStream()){
		case AT_TIMESTAMP:
			return new KinesisStreamAtTimestampOp<>(streamSubscriberConfig, config, kinesisNode);
		case LATEST:
			return new KinesisStreamLatestOp<>(streamSubscriberConfig, config, kinesisNode);
		case OLDEST:
			return new KinesisStreamOldestOp<>(streamSubscriberConfig, config, kinesisNode);
		default:
			throw new IllegalArgumentException();
		}
	}

	public KinesisOp<PK,D,F,Void> makePutMultiOp(Collection<D> databeans, Config config){
		return new KinesisPutMultiOp<>(databeans, config, kinesisNode);
	}

	public KinesisOp<PK,D,F,Void> makePutOp(D databean, Config config){
		return new KinesisPutOp<>(databean, config, kinesisNode);
	}

}
