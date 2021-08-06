/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.aws.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

public class AwsSdkV2ToV1CredentialsProvider implements AWSCredentialsProvider{

	private final AwsCredentialsProvider awsCredentialsProvider;

	public AwsSdkV2ToV1CredentialsProvider(AwsCredentialsProvider awsCredentialsProvider){
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	@Override
	public void refresh(){
	}

	@Override
	public AWSCredentials getCredentials(){
		AwsCredentials credentials = awsCredentialsProvider.resolveCredentials();
		if(credentials instanceof AwsSessionCredentials){
			return new BasicSessionCredentials(
					credentials.accessKeyId(),
					credentials.secretAccessKey(),
					((AwsSessionCredentials)credentials)
					.sessionToken());
		}
		return new BasicAWSCredentials(credentials.accessKeyId(), credentials.secretAccessKey());
	}

}
