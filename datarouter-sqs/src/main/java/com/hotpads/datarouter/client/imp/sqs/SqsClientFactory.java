package com.hotpads.datarouter.client.imp.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;

public class SqsClientFactory implements ClientFactory{

	private final String clientName;
	private final SqsClientType clientType;
	private final SqsOptions sqsOptions;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public SqsClientFactory(String clientName, SqsClientType clientType, SqsOptions sqsOptions,
			ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientType = clientType;
		this.sqsOptions = sqsOptions;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
	}

	@Override
	public Client call(){
		AWSCredentials credentials = new BasicAWSCredentials(sqsOptions.getAccessKey(), sqsOptions.getSecretKey());
		AmazonSQSClient amazonSqsClient = new AmazonSQSAsyncClient(credentials);
		return new SqsClient(clientName, clientType, amazonSqsClient, sqsOptions, clientAvailabilitySettings);
	}

}
