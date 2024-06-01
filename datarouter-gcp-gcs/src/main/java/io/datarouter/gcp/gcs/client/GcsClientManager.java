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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;

import io.datarouter.gcp.gcs.DatarouterGcsClient;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.timer.PhaseTimer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcsClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(GcsClientManager.class);

	@Inject
	private GcsClientOptions gcsClientOptions;

	private final Map<ClientId,DatarouterGcsClient> clientByClientId = new ConcurrentHashMap<>();

	public DatarouterGcsClient getClient(ClientId clientId){
		initClient(clientId);
		return clientByClientId.get(clientId);
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		if(clientByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already exists");
		}
		var timer = new PhaseTimer(clientId.getName());
		DatarouterGcsClient client = create(clientId);
		clientByClientId.put(clientId, client);
		timer.add("create");
		logger.warn("{}", timer);
	}

	private DatarouterGcsClient create(ClientId clientId){
		Credentials credentials = gcsClientOptions.credentials(clientId.getName());
		return new DatarouterGcsClient(credentials);
	}

	@Override
	public void shutdown(ClientId clientId){
	}

}
