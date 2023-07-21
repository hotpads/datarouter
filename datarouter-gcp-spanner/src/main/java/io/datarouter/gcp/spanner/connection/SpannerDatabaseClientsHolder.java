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
package io.datarouter.gcp.spanner.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;

import io.datarouter.storage.client.ClientId;
import jakarta.inject.Singleton;

@Singleton
public class SpannerDatabaseClientsHolder{

	private final Map<ClientId,DatabaseAdminClient> databaseAdminClientInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,DatabaseClient> databaseClientsInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,DatabaseId> databaseIdInstances = new ConcurrentHashMap<>();

	public void register(
			ClientId clientId,
			DatabaseAdminClient databaseAdminClient,
			DatabaseClient databaseClient,
			DatabaseId databaseId){
		if(databaseClientsInstances.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a client");
		}
		databaseAdminClientInstances.put(clientId, databaseAdminClient);
		databaseClientsInstances.put(clientId, databaseClient);
		databaseIdInstances.putIfAbsent(clientId, databaseId);
	}

	public DatabaseAdminClient getDatabaseAdminClient(ClientId clientId){
		return databaseAdminClientInstances.get(clientId);
	}

	public DatabaseClient getDatabaseClient(ClientId clientId){
		return databaseClientsInstances.get(clientId);
	}

	public void close(ClientId clientId){
		databaseAdminClientInstances.remove(clientId);
		databaseClientsInstances.remove(clientId);
		databaseIdInstances.remove(clientId);
	}

	public DatabaseId getDatabaseId(ClientId clientId){
		return databaseIdInstances.get(clientId);
	}

	public Database getDatabase(ClientId clientId){
		DatabaseAdminClient databaseAdminClient = getDatabaseAdminClient(clientId);
		DatabaseId databaseId = getDatabaseId(clientId);
		return databaseAdminClient.getDatabase(
				databaseId.getInstanceId().getInstance(),
				databaseId.getDatabase());
	}

}
