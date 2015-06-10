package com.hotpads.datarouter.client.imp.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;

public class SqsClient extends BaseClient{

	private final String name;
	private final SqsClientType clientType;
	private final AmazonSQSClient amazonSqsClient;
	private final SqsOptions sqsOptions;

	public SqsClient(String name, SqsClientType clientType, AmazonSQSClient amazonSqsClient, SqsOptions sqsOptions){
		this.name = name;
		this.clientType = clientType;
		this.amazonSqsClient = amazonSqsClient;
		this.sqsOptions = sqsOptions;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public ClientType getType(){
		return clientType;
	}

	@Override
	public void shutdown(){
		amazonSqsClient.shutdown();
	}
	
	public AmazonSQSClient getAmazonSqsClient(){
		return amazonSqsClient;
	}
	
	public SqsOptions getSqsOptions(){
		return sqsOptions;
	}
}
