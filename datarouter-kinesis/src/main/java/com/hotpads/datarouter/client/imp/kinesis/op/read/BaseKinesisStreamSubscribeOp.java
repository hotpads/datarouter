package com.hotpads.datarouter.client.imp.kinesis.op.read;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.hotpads.datarouter.client.imp.kinesis.node.BaseKinesisNode;
import com.hotpads.datarouter.client.imp.kinesis.op.KinesisOp;
import com.hotpads.datarouter.client.imp.kinesis.op.read.kcl.KinesisSubscriber;
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

	/* used only if subscribing for the first time to the stream or if replayData = true, or if using an
	 * explicitKclApplicationName that was not used before. */
	private final InitialPositionInStream initialPositionInStream;

	/* accessing the 'unsubscribe()' method */
	private final DatarouterStreamSubscriberAccessorSetter streamSubscriberAccessorSetter;

	/* used for internal dynamoDb table name that keeps track of the position of each shard iterator. */
	private final String kclApplicationName;

	/* used with InitialPositionInStream.AT_TIMESTAMP */
	private final Date timestamp;

	/* the kcl worker puts stream records in this blocking queue. */
	private final Integer blockingQueueSize;

	/* each shard processor of the kcl worker can read up to 2MB of records/second or up to 10000 records/request. */
	private final Integer maxRecordsPerRequest;

	/* one read capacity unit = 1 strongly consistent read per second, or 2/second for items up to 4KB */
	private final Integer initialLeaseTableReadCapacity;

	/* one write capacity unit = 1 write per second, for items up to 1KB in size */
	private final Integer initialLeaseTableWriteCapacity;

	/* if true we're issuing a delete request for the dynamoDb table before subscribing to the stream */
	private final Boolean replayData;

	public BaseKinesisStreamSubscribeOp(Config config, BaseKinesisNode<PK,D,F> kinesisNode, String subscriberName,
			InitialPositionInStream initialPositionInStream,
			DatarouterStreamSubscriberAccessorSetter subscriberAccessorSetter, Integer blockingQueueSize,
			Integer maxRecordsPerRequest, Boolean replayData, String explicitKclApplicationName,
			Integer initialLeaseTableReadCapacity, Integer initialLeaseTableWriteCapacity){
		this(config, kinesisNode, subscriberName, initialPositionInStream, subscriberAccessorSetter, blockingQueueSize,
				maxRecordsPerRequest, replayData, explicitKclApplicationName, null, initialLeaseTableReadCapacity,
				initialLeaseTableWriteCapacity);
	}

	public BaseKinesisStreamSubscribeOp(Config config, BaseKinesisNode<PK,D,F> kinesisNode, String subscriberName,
			InitialPositionInStream initialPositionInStream,
			DatarouterStreamSubscriberAccessorSetter subscriberAccessorSetter, Integer blockingQueueSize,
			Integer maxRecordsPerRequest, boolean replayData, String explicitKclApplicationName, Date timestamp,
			Integer initialLeaseTableReadCapacity, Integer initialLeaseTableWriteCapacity){
		super(config, kinesisNode);
		this.initialPositionInStream = initialPositionInStream;
		this.timestamp = timestamp;
		this.kclApplicationName = explicitKclApplicationName != null ? explicitKclApplicationName
				:(kclNamespace + "-" + subscriberName + "-" + streamName);
		this.streamSubscriberAccessorSetter = subscriberAccessorSetter;
		this.blockingQueueSize = blockingQueueSize;
		this.maxRecordsPerRequest = maxRecordsPerRequest;
		this.initialLeaseTableReadCapacity = initialLeaseTableReadCapacity;
		this.initialLeaseTableWriteCapacity = initialLeaseTableWriteCapacity;
		this.replayData = replayData;
	}

	@Override
	protected final BlockingQueue<StreamRecord<PK,D>> run(){
		String workerId = kclApplicationName + UUID.randomUUID().toString();
		KinesisSubscriber<PK,D,F> kinesisSubscriber = new KinesisSubscriber<>(streamName, regionName,
				initialPositionInStream, timestamp, blockingQueueSize, maxRecordsPerRequest, kclApplicationName,
				workerId, replayData, initialLeaseTableReadCapacity, initialLeaseTableWriteCapacity,
				amazonKinesisClient, awsCredentialsProvider, codec, fielder, databeanSupplier,
				streamsSubscribersTracker);
		if(streamSubscriberAccessorSetter != null){
			streamSubscriberAccessorSetter.setDatarouterStreamSubscriberAccessor(kinesisSubscriber);
		}
		kinesisSubscriber.subscribe();
		return kinesisSubscriber.getBlockingQueue();
	}

}
