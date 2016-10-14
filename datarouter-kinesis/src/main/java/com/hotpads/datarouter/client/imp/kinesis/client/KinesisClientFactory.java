package com.hotpads.datarouter.client.imp.kinesis.client;

import com.amazonaws.services.kinesis.AmazonKinesisAsyncClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;

public class KinesisClientFactory implements ClientFactory{

	private final String clientName;
	private final KinesisClientType clientType;
	private final KinesisOptions kinesisOptions;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public KinesisClientFactory(String clientName, KinesisClientType clientType,
			KinesisOptions kinesisOptions, ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientType = clientType;
		this.kinesisOptions = kinesisOptions;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
	}

	@Override
	public Client call(){
		KinesisAwsCredentialsProvider credentialsProvider = new KinesisAwsCredentialsProvider(kinesisOptions);
		AmazonKinesisClient amazonKinesisClient = new AmazonKinesisAsyncClient(credentialsProvider);
		return new KinesisClient(clientName, clientType, amazonKinesisClient, credentialsProvider,
				kinesisOptions, clientAvailabilitySettings);
	}
}
