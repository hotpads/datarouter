/**
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
package io.datarouter.aws.s3.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.SerializableAwsCredentialsProviderProvider;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class S3ClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(S3ClientManager.class);

	@Inject
	private S3Options options;

	private final Map<ClientId,DatarouterS3Client> clientByClientId = new ConcurrentHashMap<>();

	public DatarouterS3Client getClient(ClientId clientId){
		initClient(clientId);
		return clientByClientId.get(clientId);
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		if(clientByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already exists");
		}
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		DatarouterS3Client client = create(clientId);
		clientByClientId.put(clientId, client);
		timer.add("create");
		logger.warn("{}", timer);
	}

	private DatarouterS3Client create(ClientId clientId){
		SerializableAwsCredentialsProviderProvider<?> awsCredentialsProvider = options.makeCredentialsProvider(
				clientId.getName());
		return new GenericDatarouterS3Client(awsCredentialsProvider);
	}

	@Override
	public void shutdown(ClientId clientId){
	}

}
