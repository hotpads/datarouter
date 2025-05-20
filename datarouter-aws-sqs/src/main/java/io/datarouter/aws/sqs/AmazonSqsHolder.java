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
package io.datarouter.aws.sqs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.storage.client.ClientId;
import io.datarouter.web.config.AwsSupport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Singleton
public class AmazonSqsHolder{

	private final Map<ClientId,SqsClient> amazonSqsByClient = new ConcurrentHashMap<>();
	private final Map<ClientId,CloudWatchClient> amazonCloudWatchByClient = new ConcurrentHashMap<>();

	@Inject
	private SqsOptions sqsOptions;
	@Inject
	private AwsSupport awsSupport;

	public void registerClient(ClientId clientId){
		if(amazonSqsByClient.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered an sqs client");
		}
		var httpClient = ApacheHttpClient.builder()
				.maxConnections(200)
				.build();
		var credentials = AwsBasicCredentials.create(sqsOptions.getAccessKey(clientId.getName()),
				sqsOptions.getSecretKey(clientId.getName()));
		var awsCredentialsProvider = StaticCredentialsProvider.create(credentials);
		SqsClient amazonSqs = SqsClient.builder()
				.region(Region.of(sqsOptions.getRegion(clientId.getName())))
				.credentialsProvider(awsCredentialsProvider)
				.httpClient(httpClient)
				.build();
		awsSupport.registerConnectionManagerFromHttpClient("sqs " + clientId.getName(), httpClient);
		amazonSqsByClient.put(clientId, amazonSqs);
		CloudWatchClient cloudWatchClient = CloudWatchClient.builder()
				.region(Region.of(sqsOptions.getRegion(clientId.getName())))
				.credentialsProvider(awsCredentialsProvider)
				.build();
		amazonCloudWatchByClient.put(clientId, cloudWatchClient);
	}

	public SqsClient get(ClientId clientId){
		return amazonSqsByClient.get(clientId);
	}

	public CloudWatchClient getCloudWatch(ClientId clientId){
		return amazonCloudWatchByClient.get(clientId);
	}

}
