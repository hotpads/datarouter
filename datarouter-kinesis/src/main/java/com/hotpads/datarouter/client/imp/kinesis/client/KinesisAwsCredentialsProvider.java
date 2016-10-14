package com.hotpads.datarouter.client.imp.kinesis.client;

import java.util.concurrent.TimeUnit;

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
	private static final int ASSUME_ROLE_SESSION_DURATION_SECONDS = (int)TimeUnit.MINUTES.toSeconds(60);//aws max
	private static final int ASSUME_ROLE_SESSION_REFRESH_THRESHOLD_MS = (int)TimeUnit.MINUTES.toMillis(1) ;

	private final AWSCredentials basicCredentials;
	private final String arnRole;

	private AWSCredentials credentials;

	private long arnRoleSessionExpirationTimestamp;

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

	private AWSSessionCredentials getTempSessionCredentialsForRoleArn(AWSCredentials awsCredentials, String arn){
		AWSSecurityTokenServiceClient tokenServiceClient = new AWSSecurityTokenServiceClient(awsCredentials);
		AssumeRoleRequest arRequest = new AssumeRoleRequest().withRoleArn(arn).withRoleSessionName("arSession")
				.withDurationSeconds(ASSUME_ROLE_SESSION_DURATION_SECONDS);
		AssumeRoleResult arResult = tokenServiceClient.assumeRole(arRequest);
		Credentials tempCredentials = arResult.getCredentials();
		this.arnRoleSessionExpirationTimestamp = tempCredentials.getExpiration().getTime();
		return new BasicSessionCredentials(tempCredentials.getAccessKeyId(), tempCredentials.getSecretAccessKey(),
				tempCredentials.getSessionToken());
	}

	@Override
	public AWSCredentials getCredentials(){
		if(shouldRefreshCredentials()){
			refresh();
		}
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

	public boolean shouldRefreshCredentials(){
		return arnRole != null && arnRoleSessionExpirationTimestamp - System
				.currentTimeMillis() < ASSUME_ROLE_SESSION_REFRESH_THRESHOLD_MS;
	}
}
