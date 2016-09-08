package com.hotpads.datarouter.client.imp.kinesis.single.op.kcl;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberAccessor;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public class KinesisSubscriber<PK extends PrimaryKey<PK>,
D extends Databean<PK,D>, F extends DatabeanFielder<PK,D>> implements DatarouterStreamSubscriberAccessor{
	private static final Logger logger = LoggerFactory.getLogger(KinesisSubscriber.class);

	private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 500;

	private Worker kinesisWorker;
	private Thread kinesisWorkerThread;
	private BlockingQueue<StreamRecord<PK, D>> blockingQueue;
	private AmazonKinesisClient kinesisClient;
	private KinesisClientLibConfiguration kinesisClientLibConfiguration;
	private IRecordProcessorFactory recordProcessorFactory;

	public KinesisSubscriber(String streamName, String regionName,
			InitialPositionInStream initialPositionInStream, Date timestamp, Integer blockingQueueSize,
			String applicationName, String workerId,
			AmazonKinesisClient kinesisClient,
			AWSCredentialsProvider credentialsProvider, StringDatabeanCodec codec, F fielder,
			Supplier<D> databeanSupplier){
		this.kinesisClient = kinesisClient;
		this.blockingQueue = blockingQueueSize != null ? new ArrayBlockingQueue<>(blockingQueueSize)
				: new ArrayBlockingQueue<>(DEFAULT_BLOCKING_QUEUE_SIZE);
		this.kinesisClientLibConfiguration = new KinesisClientLibConfiguration(applicationName, streamName,
				credentialsProvider, workerId)
				.withRegionName(regionName)
				.withInitialPositionInStream(initialPositionInStream);
		if(timestamp != null){
			kinesisClientLibConfiguration.withTimestampAtInitialPositionInStream(timestamp);
		}

        this.recordProcessorFactory = new IRecordProcessorFactory(){

			@Override
			public IRecordProcessor createProcessor(){
				return new KclApplicationRecordProcessor<>(blockingQueue, codec, fielder, databeanSupplier);
			}

		};
	}

	public void subscribe(){
		kinesisWorker = new Worker.Builder()
				 .recordProcessorFactory(recordProcessorFactory)
				 .config(kinesisClientLibConfiguration)
				 .kinesisClient(kinesisClient)
				 .build();
		kinesisWorkerThread = new Thread("kinesis client worker"){
			@Override
			public void run(){
				kinesisWorker.run();
			}
		};

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
