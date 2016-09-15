package com.hotpads.datarouter.client.imp.kinesis.op.read.kcl;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberAccessor;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public class KinesisSubscriber<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements DatarouterStreamSubscriberAccessor{

	private static final Logger logger = LoggerFactory.getLogger(KinesisSubscriber.class);

	private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 1;

	private final AmazonKinesisClient kinesisClient;
	private final KinesisClientLibConfiguration kinesisClientLibConfiguration;
	private final BlockingQueue<StreamRecord<PK, D>> blockingQueue;
	private final IRecordProcessorFactory recordProcessorFactory;

	private Worker kinesisWorker;
	private Thread kinesisWorkerThread;

	public KinesisSubscriber(String streamName, String regionName, InitialPositionInStream initialPositionInStream,
			Date timestamp, Integer blockingQueueSize, Integer maxRecordsPerRequest, String applicationName,
			String workerId, Boolean replayData, Integer initialLeaseTableReadCapacity,
			Integer initialLeaseTableWriteCapacity, AmazonKinesisClient kinesisClient,
			AWSCredentialsProvider credentialsProvider, StringDatabeanCodec codec, F fielder,
			Supplier<D> databeanSupplier){
		this.kinesisClient = kinesisClient;
		this.blockingQueue = blockingQueueSize != null ? new ArrayBlockingQueue<>(blockingQueueSize)
				: new ArrayBlockingQueue<>(DEFAULT_BLOCKING_QUEUE_SIZE);
		this.kinesisClientLibConfiguration = new KinesisClientLibConfiguration(applicationName, streamName,
				credentialsProvider, workerId)
				.withRegionName(regionName)
				.withMetricsLevel(MetricsLevel.SUMMARY);

		if(maxRecordsPerRequest!= null && maxRecordsPerRequest > 0){
			kinesisClientLibConfiguration.withMaxRecords(maxRecordsPerRequest);
		}
		if(initialLeaseTableReadCapacity!= null && initialLeaseTableReadCapacity > 0){
			kinesisClientLibConfiguration.withInitialLeaseTableReadCapacity(initialLeaseTableReadCapacity);
		}
		if(initialLeaseTableWriteCapacity!= null && initialLeaseTableWriteCapacity > 0){
			kinesisClientLibConfiguration.withInitialLeaseTableWriteCapacity(initialLeaseTableWriteCapacity);
		}
		if(timestamp != null){
			kinesisClientLibConfiguration.withTimestampAtInitialPositionInStream(timestamp);
		} else {
			kinesisClientLibConfiguration.withInitialPositionInStream(initialPositionInStream);
		}

		this.recordProcessorFactory = () -> new KclApplicationRecordProcessor<>(blockingQueue, codec, fielder,
				databeanSupplier);

		if(replayData != null && replayData){
			deleteOldDynamoDbTable(applicationName);
		}
	}

	private void deleteOldDynamoDbTable(String dynamoDbTableName){
		AmazonDynamoDBClient dynamoDbclient = new AmazonDynamoDBClient(kinesisClientLibConfiguration
				.getDynamoDBCredentialsProvider())
				.withRegion(Regions.fromName(kinesisClientLibConfiguration.getRegionName()));
		DynamoDB dynamoDb = new DynamoDB(dynamoDbclient);
		Table table = dynamoDb.getTable(dynamoDbTableName);
        try {
            logger.warn("Issuing DeleteTable request for " + dynamoDbTableName);
            table.delete();

            logger.warn("Waiting for " + dynamoDbTableName + " to be deleted...this may take a while...");

            table.waitForDelete();
        } catch (Exception e) {
            logger.error("DeleteTable request failed for " + dynamoDbTableName);
        }
	}

	public void subscribe(){
		logger.warn("subscribing to " + kinesisClientLibConfiguration.getStreamName() + " in "
				+ kinesisClientLibConfiguration.getRegionName() + " with app name: " + kinesisClientLibConfiguration
						.getApplicationName());
		kinesisWorker = new Worker.Builder()
				 .recordProcessorFactory(recordProcessorFactory)
				 .config(kinesisClientLibConfiguration.withInitialLeaseTableReadCapacity(250))
				 .kinesisClient(kinesisClient)
				 .build();
		kinesisWorkerThread = new Thread(() -> kinesisWorker.run(), "kinesis client worker");

		kinesisWorkerThread.setDaemon(true);
		kinesisWorkerThread.start();
	}

	@Override
	public void unsubscribe(){
		if(kinesisWorker!=null){
			kinesisWorker.shutdown();
		}
		try{
			kinesisWorkerThread.join();
		}catch(InterruptedException e){
			logger.error("", e);
		}
	}

	public BlockingQueue<StreamRecord<PK,D>> getBlockingQueue(){
		return blockingQueue;
	}

}
