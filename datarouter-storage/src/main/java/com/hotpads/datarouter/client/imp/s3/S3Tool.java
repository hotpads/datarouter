package com.hotpads.datarouter.client.imp.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Tool{

	private static final String ACCOUNT_NUMBER = "5134-2313-4236";
	private static final String ACCESS_KEY = "1YXWW9ZSHCRNJQ8K5FR2";
	private static final String SECRET_KEY = "bZFE3p0amBaR3BrEXiTLip/jtvBhYjajXtvm0FYD";

	public static AmazonS3 getS3(){
		return new AmazonS3Client(
				new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
	}
}
