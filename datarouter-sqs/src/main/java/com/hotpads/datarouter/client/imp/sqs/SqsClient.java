package com.hotpads.datarouter.client.imp.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;

public class SqsClient extends BaseClient{

	private String name;
	private SqsClientType clientType;
	private AmazonSQSClient amazonSqsClient;

	public SqsClient(String name, SqsClientType clientType, AmazonSQSClient amazonSqsClient){
		this.name = name;
		this.clientType = clientType;
		this.amazonSqsClient = amazonSqsClient;
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
	
}
