package com.hotpads.datarouter.client.imp.kinesis;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;

public class KclZillowReadOnlyLitLzgClientFactory implements ClientFactory{

	private final String clientName;
	private final KinesisClientType clientType;
	private final KinesisOptions kinesisOptions;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public KclZillowReadOnlyLitLzgClientFactory(String clientName, KinesisClientType clientType,
			KinesisOptions kinesisOptions, ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientType = clientType;
		this.kinesisOptions = kinesisOptions;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
	}

	@Override
	public Client call(){
		AWSCredentials basicCredentials = new BasicAWSCredentials(kinesisOptions.getAccessKey(), kinesisOptions
				.getSecretKey());
		AWSCredentials credentials = basicCredentials;
		if(kinesisOptions.getArnRole() != null){//we're assuming an aws role
			credentials = getTempSessionCredentialsForRoleArn(basicCredentials, kinesisOptions.getArnRole());
		}
		AmazonKinesisClient amazonKinesisClient = new AmazonKinesisAsyncClient(credentials);
		AWSCredentialsProvider credentialsProvider = makeAwsCredentialsProvider(credentials);
		return new KclZillowReadOnlyLitLzgClient(clientName, clientType, amazonKinesisClient, credentialsProvider,
				kinesisOptions, clientAvailabilitySettings);
	}

	private static AWSSessionCredentials getTempSessionCredentialsForRoleArn(AWSCredentials awsCredentials, String arn){
		AWSSecurityTokenServiceClient tokenServiceClient = new AWSSecurityTokenServiceClient(awsCredentials);
		AssumeRoleRequest arRequest = new AssumeRoleRequest().withRoleArn(arn).withRoleSessionName("arSession");
		AssumeRoleResult arResult = tokenServiceClient.assumeRole(arRequest);
		Credentials tempCredentials = arResult.getCredentials();
		return new BasicSessionCredentials(tempCredentials.getAccessKeyId(), tempCredentials.getSecretAccessKey(),
				tempCredentials.getSessionToken());
	}

	private static AWSCredentialsProvider makeAwsCredentialsProvider(AWSCredentials credentials){
		return new AWSCredentialsProvider(){

			@Override
			public void refresh(){
			}

			@Override
			public AWSCredentials getCredentials(){
				return credentials;
			}
		};
	}

}
