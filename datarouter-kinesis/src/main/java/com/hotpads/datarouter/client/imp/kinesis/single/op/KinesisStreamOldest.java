package com.hotpads.datarouter.client.imp.kinesis.single.op;

import java.util.concurrent.BlockingQueue;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public class KinesisStreamOldest<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseKinesisStreamSubscribeOperation<PK,D,F>{

	public KinesisStreamOldest(Config config, BaseKinesisNode<PK,D,F> kinesisNode){
		super(config, kinesisNode);
	}

	@Override
	public String getSubscriberName(){
		return "streamOldestSubscriber";
	}

	@Override
	protected final BlockingQueue<StreamRecord<PK,D>> run(){
		StreamSubscriber<PK,D,F> streamSubscriber = new StreamSubscriber<>(streamName, getApplicationName(), regionName,
				InitialPositionInStream.TRIM_HORIZON, amazonKinesisClient, awsCredentialsProvider, codec, fielder,
				databeanSupplier);
		streamSubscriber.subscribe();
		return streamSubscriber.getHose();
	}
}
