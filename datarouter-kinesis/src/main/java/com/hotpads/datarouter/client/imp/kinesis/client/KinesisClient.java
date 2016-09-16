package com.hotpads.datarouter.client.imp.kinesis.client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;

public class KinesisClient extends BaseClient{

	private final KinesisClientType clientType;
	private final AmazonKinesisClient kinesisClient;
	private final AWSCredentialsProvider awsCredentialsProvider;
	private final KinesisOptions kinesisOptions;
	private final KinesisStreamsSubscribersTracker streamsSubscribersTracker;

	public KinesisClient(String name, KinesisClientType clientType, AmazonKinesisClient kinesisClient,
			AWSCredentialsProvider awsCredentialsProvider, KinesisOptions kinesisOptions,
			ClientAvailabilitySettings clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
		this.clientType = clientType;
		this.kinesisClient = kinesisClient;
		this.kinesisOptions = kinesisOptions;
		this.awsCredentialsProvider = awsCredentialsProvider;
		this.streamsSubscribersTracker = new KinesisStreamsSubscribersTracker();
	}

	@Override
	public ClientType getType(){
		return clientType;
	}

	@Override
	public void shutdown(){
		streamsSubscribersTracker.unsubscribeAll();
		kinesisClient.shutdown();
	}

	public AmazonKinesisClient getAmazonKinesisClient(){
		return kinesisClient;
	}

	public AWSCredentialsProvider getAwsCredentialsProvider(){
		return awsCredentialsProvider;
	}

	public KinesisOptions getKinesisOptions(){
		return kinesisOptions;
	}

	public KinesisStreamsSubscribersTracker getKinesisStreamsSubscribersTracker(){
		return streamsSubscribersTracker;
	}

}
