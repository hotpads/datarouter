package com.hotpads.datarouter.client.imp.kinesis.client;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

public class KinesisAwsCredentialsProvider implements AWSCredentialsProvider{
	private final AWSCredentials basicCredentials;
	private final String arnRole;

	private AWSCredentials credentials;

	public KinesisAwsCredentialsProvider(KinesisOptions kinesisOptions){
		if(!kinesisOptions.isEnabled()){
			basicCredentials = null;
			arnRole = null;
			return;
		}
		this.basicCredentials = new BasicAWSCredentials(kinesisOptions.getAccessKey(), kinesisOptions
				.getSecretKey());
		this.arnRole = kinesisOptions.getArnRole();
		refresh();
	}

	private static AWSSessionCredentials getTempSessionCredentialsForRoleArn(AWSCredentials awsCredentials, String arn){
		AWSSecurityTokenServiceClient tokenServiceClient = new AWSSecurityTokenServiceClient(awsCredentials);
		AssumeRoleRequest arRequest = new AssumeRoleRequest().withRoleArn(arn).withRoleSessionName("arSession");
		AssumeRoleResult arResult = tokenServiceClient.assumeRole(arRequest);
		Credentials tempCredentials = arResult.getCredentials();
		return new BasicSessionCredentials(tempCredentials.getAccessKeyId(), tempCredentials.getSecretAccessKey(),
				tempCredentials.getSessionToken());
	}

	@Override
	public AWSCredentials getCredentials(){
		return credentials;
	}

	@Override
	public void refresh(){
		if(arnRole != null){// we're assuming an aws role
			credentials = getTempSessionCredentialsForRoleArn(basicCredentials, arnRole);
		}else{
			credentials = basicCredentials;
		}
	}
}
