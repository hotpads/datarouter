package com.hotpads.datarouter.client.imp.kinesis.single.op;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberConfig;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class KinesisStreamOldestOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseKinesisStreamSubscribeOp<PK,D,F>{

	private static final String SUBSCRIBER_NAME = "streamOldestSubscriber";
	private static final InitialPositionInStream INITIAL_POSITION_IN_STREAM = InitialPositionInStream.TRIM_HORIZON;

	public KinesisStreamOldestOp(DatarouterStreamSubscriberConfig streamConfig, Config config,
			BaseKinesisNode<PK,D,F> kinesisNode){
		super(config, kinesisNode, SUBSCRIBER_NAME, INITIAL_POSITION_IN_STREAM, streamConfig, streamConfig
				.getBlockingQueueSize());
		if(streamConfig.getSubscriberAppName()!= null){
			withExplicitApplicationName(streamConfig.getSubscriberAppName());
		}
	}

}
