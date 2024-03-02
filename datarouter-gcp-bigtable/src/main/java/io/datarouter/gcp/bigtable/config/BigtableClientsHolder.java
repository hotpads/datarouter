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
package io.datarouter.gcp.bigtable.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;

import io.datarouter.storage.client.ClientId;
import jakarta.inject.Singleton;

@Singleton
public class BigtableClientsHolder{

	private final Map<ClientId,BigtableTableAdminClient> tableAdminDataClientInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,BigtableInstanceAdminClient> instanceAdminDataClientInstances =
			new ConcurrentHashMap<>();
	private final Map<ClientId,BigtableDataClient> dataClientInstances = new ConcurrentHashMap<>();

	public void register(
			ClientId clientId,
			BigtableTableAdminClient adminAdminClient,
			BigtableInstanceAdminClient adminClient,
			BigtableDataClient dataClient){
		tableAdminDataClientInstances.put(clientId, adminAdminClient);
		instanceAdminDataClientInstances.put(clientId, adminClient);
		dataClientInstances.put(clientId, dataClient);
	}

	public BigtableTableAdminClient getTableAdminClient(ClientId clientId){
		return tableAdminDataClientInstances.get(clientId);
	}

	public BigtableInstanceAdminClient getInstanceAdminClient(ClientId clientId){
		return instanceAdminDataClientInstances.get(clientId);
	}

	public BigtableDataClient getDataClient(ClientId clientId){
		return dataClientInstances.get(clientId);
	}

	public void closeClient(ClientId clientId){
		tableAdminDataClientInstances.get(clientId).close();
		instanceAdminDataClientInstances.get(clientId).close();
		dataClientInstances.get(clientId).close();
	}

}
