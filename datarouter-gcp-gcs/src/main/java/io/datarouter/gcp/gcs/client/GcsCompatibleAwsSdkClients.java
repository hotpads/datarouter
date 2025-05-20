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
package io.datarouter.gcp.gcs.client;

import java.io.IOException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.SignerFactory;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;


public class GcsCompatibleAwsSdkClients{
	public static AmazonS3 getS3Client(Credentials credentials){
		GcpSessionCredentials sessionCredentials = new GcpSessionCredentials(credentials);
		SignerFactory.registerSigner("AwsCompatibleSigner", AwsCompatibleSigner.class);
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setSignerOverride("AwsCompatibleSigner");
		clientConfig.setUseGzip(true);
		clientConfig.setMaxConnections(200);
		clientConfig.setMaxErrorRetry(1);
		return AmazonS3ClientBuilder.standard()
				.withClientConfiguration(clientConfig)
				.withEndpointConfiguration(new EndpointConfiguration("https://storage.googleapis.com", "auto"))
				.withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
				.build();
	}

	public static class AwsCompatibleSigner extends AWS4Signer{
		@Override
		public void sign(SignableRequest<?> request, AWSCredentials credentials){
			request.addHeader("Authorization", "Bearer " + credentials.getAWSSecretKey());
			request.addHeader("x-goog-project-id", credentials.getAWSAccessKeyId());
		}
	}

	public static class GcpSessionCredentials implements AWSCredentials{

		private final GoogleCredentials credentials;

		public GcpSessionCredentials(Credentials credentials){
			this.credentials = ((GoogleCredentials) credentials).createScoped("https://www.googleapis"
					+ ".com/auth/cloud-platform");
		}

		/**
		 * Does not return AWS access key id, rather this is used to return the project id for gcp.
		 * @return Returns the gcp project id for the given credentials
		 */
		@Override
		public String getAWSAccessKeyId(){
			return ((ServiceAccountCredentials)credentials).getProjectId();
		}

		/**
		 * Used to get a valid access token for gcp. Refreshes the credentials if expired.
		 * @return a valid oauth access token
		 */
		@Override
		public String getAWSSecretKey(){
			try{
				this.credentials.refreshIfExpired();
			}catch(IOException exception){
				return exception.getMessage();
			}
			return credentials.getAccessToken().getTokenValue();
		}
	}
}
