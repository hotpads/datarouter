package com.hotpads.datarouter.client.imp.kinesis.single.op;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.client.imp.kinesis.op.KinesisOp;
import com.hotpads.datarouter.client.imp.kinesis.single.op.kcl.KinesisSubscriber;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberAccessorSetter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public abstract class BaseKinesisStreamSubscribeOp<
PK extends PrimaryKey<PK>,
D extends Databean<PK,D>,
F extends DatabeanFielder<PK,D>>
extends KinesisOp<PK,D,F,BlockingQueue<StreamRecord<PK,D>>>{

	private String applicationName;
	private String subscriberName;
	private InitialPositionInStream initialPositionInStream;
	private DatarouterStreamSubscriberAccessorSetter streamSubscriberAccessorSetter;
	private Date timestamp;
	private Integer blockingQueueSize;
	private Integer maxRecordsPerRequest;
	private Boolean replayData;//if true deletes the dynamodb table

	public BaseKinesisStreamSubscribeOp(Config config, BaseKinesisNode<PK,D,F> kinesisNode, String subscriberName,
			InitialPositionInStream initialPositionInStream,
			DatarouterStreamSubscriberAccessorSetter subscriberAccessorSetter, Integer blockingQueueSize,
			Integer maxRecordsPerRequest, boolean replayData){
		super(config, kinesisNode);
		this.subscriberName = subscriberName;
		this.initialPositionInStream = initialPositionInStream;
		this.applicationName = kclNamespace + "-" + subscriberName + "-" + streamName;//used for dynamoDb table name
		this.streamSubscriberAccessorSetter = subscriberAccessorSetter;
		this.blockingQueueSize = blockingQueueSize;
		this.maxRecordsPerRequest = maxRecordsPerRequest;
		this.replayData = replayData;
	}

	public BaseKinesisStreamSubscribeOp<PK,D,F> withExplicitApplicationName(String applicationName){
		this.applicationName = applicationName;
		return this;
	}

	public BaseKinesisStreamSubscribeOp<PK,D,F> withTimestampAtInitialPositionInStreamAtTimestamp(Date timestamp){
		this.timestamp = timestamp;
		this.initialPositionInStream = InitialPositionInStream.AT_TIMESTAMP;
		return this;
	}

	@Override
	protected final BlockingQueue<StreamRecord<PK,D>> run(){
		String workerId = applicationName + UUID.randomUUID().toString();
		KinesisSubscriber<PK,D,F> kinesisSubscriber = new KinesisSubscriber<>(streamName, regionName,
				initialPositionInStream, timestamp, blockingQueueSize, maxRecordsPerRequest, applicationName, workerId,
				replayData,	amazonKinesisClient, awsCredentialsProvider, codec, fielder, databeanSupplier);
		if(streamSubscriberAccessorSetter != null){
			streamSubscriberAccessorSetter.setDatarouterStreamSubscriberAccessor(kinesisSubscriber);
		}
		kinesisSubscriber.subscribe();
		return kinesisSubscriber.getBlockingQueue();
	}

}
