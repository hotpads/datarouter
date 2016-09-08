package com.hotpads.datarouter.client.imp.kinesis.single.op;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.client.imp.kinesis.op.KinesisOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public abstract class BaseKinesisStreamSubscribeOperation<
PK extends PrimaryKey<PK>,
D extends Databean<PK,D>,
F extends DatabeanFielder<PK,D>>
extends KinesisOp<PK,D,F,BlockingQueue<StreamRecord<PK,D>>>{

	private String applicationName;

	public BaseKinesisStreamSubscribeOperation(Config config, BaseKinesisNode<PK,D,F> kinesisNode){
		super(config, kinesisNode);
		this.applicationName = kclNamespace + "-" + getSubscriberName() + "-" + streamName;
	}

	public abstract  String getSubscriberName();

	public BaseKinesisStreamSubscribeOperation<PK,D,F> withExplicitApplicationName(String applicationName){
		this.applicationName = applicationName;
		return this;
	}

	public String getApplicationName(){
		return applicationName;
	}

	public static class StreamSubscriber<PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>, F extends DatabeanFielder<PK,D>>{
		private Worker kinesisWorker;
		private Thread kinesisWorkerThread;
		private BlockingQueue<StreamRecord<PK, D>> hose;
		private AmazonKinesisClient kinesisClient;
		private KinesisClientLibConfiguration kinesisClientLibConfiguration;
		private IRecordProcessorFactory recordProcessorFactory;
		public StreamSubscriber(String streamName, String applicationName, String regionName,
				InitialPositionInStream initialPositionInStream, AmazonKinesisClient kinesisClient,
				AWSCredentialsProvider credentialsProvider, StringDatabeanCodec codec, F fielder,
				Supplier<D> databeanSupplier ){
			this.kinesisClient = kinesisClient;
			this.hose = new ArrayBlockingQueue<>(100000);
			String workerId = applicationName + UUID.randomUUID().toString();//maybe
	        this.kinesisClientLibConfiguration =
					new KinesisClientLibConfiguration(applicationName, streamName, credentialsProvider, workerId);
	        kinesisClientLibConfiguration.withInitialPositionInStream(initialPositionInStream);
	        kinesisClientLibConfiguration.withRegionName(regionName);

	        this.recordProcessorFactory = new IRecordProcessorFactory(){

				@Override
				public IRecordProcessor createProcessor(){
					return new AmazonKinesisApplicationSampleRecordProcessor<>(hose, codec, fielder, databeanSupplier);
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

		public void unsubscribe(){
			if(kinesisWorker!=null){
				kinesisWorker.shutdown();
				System.out.println("SHUTTING DOWN KINESIS WORKER");
			}
			try{
				kinesisWorkerThread.join();
				System.out.println("JOINED");
			}catch(InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public BlockingQueue<StreamRecord<PK,D>> getHose(){
			return hose;
		}
	}

	//TODO move these classes to their own file
	public static class AmazonKinesisApplicationSampleRecordProcessor<PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>, F extends DatabeanFielder<PK,D>> implements IRecordProcessor {
		private static final Logger logger = LoggerFactory.getLogger(
				KinesisStreamOldest.AmazonKinesisApplicationSampleRecordProcessor.class);

    	private BlockingQueue<StreamRecord<PK, D>> hose;
    	private StringDatabeanCodec codec;
		private F fielder;
		private Supplier<D> databeanSupplier;

        private String kinesisShardId;

        // Backoff and retry settings
        private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
        private static final int NUM_RETRIES = 3;

        // Checkpoint about once a minute
        private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
        private long nextCheckpointTimeInMillis;

        public AmazonKinesisApplicationSampleRecordProcessor(BlockingQueue<StreamRecord<PK,D>> hose,
				StringDatabeanCodec codec, F fielder, Supplier<D> databeanSupplier){
			this.hose = hose;
			this.codec = codec;
			this.fielder = fielder;
			this.databeanSupplier = databeanSupplier;
		}

        /**
         * Process records performing retries as needed. Skip "poison pill" records.
         *
         * @param records Data records to be processed.
         */
        private void processRecordsWithRetries(List<Record> records) {
            System.out.println(">>"+records.size()+"<<");
        	for (Record record : records) {
                boolean processedSuccessfully = false;
                for (int i = 0; i < NUM_RETRIES; i++) {
                    try {
                        processSingleRecord(record);

                        processedSuccessfully = true;
                        break;
                    } catch (Throwable t) {
                        logger.warn("Caught throwable while processing record " + record, t);
                    }

                    // backoff if we encounter an exception.
                    try {
                        Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                    } catch (InterruptedException e) {
                        logger.debug("Interrupted sleep", e);
                    }
                }

                if (!processedSuccessfully) {
                    logger.error("Couldn't process record " + record + ". Skipping the record.");
                }
            }
        }

        /**
         * Process a single record.
         *
         * @param record The record to be processed.
         * @throws InterruptedException
         */
        private void processSingleRecord(Record record) throws InterruptedException {
        	logger.trace(record.getPartitionKey() + " " + record.getSequenceNumber());
        	ByteBuffer data = record.getData();
			String jsonBody = new String(data.array(), Charset.forName("UTF-8"));
			StringBuilder jsonBuilder = new StringBuilder();
			jsonBuilder.append("{");
			jsonBuilder.append("\"SequenceNumber\":\"" + record.getSequenceNumber() + "\",");
			jsonBuilder.append("\"RecordData\":"+jsonBody);
			jsonBuilder.append("}");
			D databean = codec.fromString(jsonBuilder.toString(), fielder, databeanSupplier);
			hose.put(new StreamRecord<>(record.getSequenceNumber(), record.getApproximateArrivalTimestamp(), databean));
        }

        /** Checkpoint with retries.
         * @param checkpointer
         */
        private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
            logger.info("Checkpointing shard " + kinesisShardId);
            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    checkpointer.checkpoint();
                    break;
                } catch (ShutdownException se) {
                    // Ignore checkpoint if the processor instance has been shutdown (fail over).
                    logger.info("Caught shutdown exception, skipping checkpoint.", se);
                    break;
                } catch (ThrottlingException e) {
                    // Backoff and re-attempt checkpoint upon transient failures
                    if (i >= (NUM_RETRIES - 1)) {
                        logger.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                        break;
                    } else {
                        logger.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                                + NUM_RETRIES, e);
                    }
                } catch (InvalidStateException e) {
                    // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                    logger.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                    break;
                }
                try {
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    logger.debug("Interrupted sleep", e);
                }
            }
        }

		@Override
		public void initialize(InitializationInput initializationInput){
			 this.kinesisShardId = initializationInput.getShardId();
		}

		@Override
		public void processRecords(ProcessRecordsInput processRecordsInput){
			processRecordsWithRetries(processRecordsInput.getRecords());
			if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
                checkpoint(processRecordsInput.getCheckpointer());
                nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
            }
		}

		@Override
		public void shutdown(ShutdownInput shutdownInput){
			logger.info("Shutting down record processor for shard: " + kinesisShardId);
			// Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
			if(shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE){
				checkpoint(shutdownInput.getCheckpointer());
			}
		}
    }
}
