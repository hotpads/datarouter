package com.hotpads.datarouter.client.imp.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;

public class SqsClientFactory implements ClientFactory{

	private final String clientName;
	private final BaseSqsClientType clientType;
	private final SqsOptions sqsOptions;

	public SqsClientFactory(String clientName, BaseSqsClientType clientType, SqsOptions sqsOptions){
		this.clientName = clientName;
		this.clientType = clientType;
		this.sqsOptions = sqsOptions;
	}

	@Override
	public Client call() throws Exception{
		AWSCredentials credentials = new BasicAWSCredentials(sqsOptions.getAccessKey(), sqsOptions.getSecretKey());
		AmazonSQSClient amazonSqsClient = new AmazonSQSAsyncClient(credentials);
		return new SqsClient(clientName, clientType, amazonSqsClient, sqsOptions);
	}

}
