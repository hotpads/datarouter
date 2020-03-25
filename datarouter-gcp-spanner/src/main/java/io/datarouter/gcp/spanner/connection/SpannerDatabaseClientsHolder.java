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
package io.datarouter.gcp.spanner.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Spanner;

import io.datarouter.storage.client.ClientId;

@Singleton
public class SpannerDatabaseClientsHolder{

	private final Map<ClientId,DatabaseClient> databaseClientsInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,Spanner> spannerInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,Database> databaseInstances = new ConcurrentHashMap<>();

	public void register(ClientId clientId, DatabaseClient databaseClient, Spanner spanner, Database database){
		if(databaseClientsInstances.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a client");
		}
		databaseClientsInstances.put(clientId, databaseClient);
		spannerInstances.putIfAbsent(clientId, spanner);
		databaseInstances.putIfAbsent(clientId, database);
	}

	public DatabaseClient getDatabaseClient(ClientId clientId){
		return databaseClientsInstances.get(clientId);
	}

	public void close(ClientId clientId){
		spannerInstances.get(clientId).close();
		spannerInstances.remove(clientId);
		databaseClientsInstances.remove(clientId);
		databaseInstances.remove(clientId);
	}

	public Spanner getSpanner(ClientId clientId){
		return spannerInstances.get(clientId);
	}

	public Database getDatabase(ClientId clientId){
		return databaseInstances.get(clientId);
	}

}
