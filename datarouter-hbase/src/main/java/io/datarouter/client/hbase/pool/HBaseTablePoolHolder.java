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
package io.datarouter.client.hbase.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.hadoop.hbase.client.Connection;

import io.datarouter.client.hbase.client.HBaseConnectionHolder;
import io.datarouter.client.hbase.client.HBaseOptions;
import io.datarouter.client.hbase.config.DatarouterHBaseSettingRoot;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;

@Singleton
public class HBaseTablePoolHolder{

	@Inject
	private HBaseOptions hBaseOptions;
	@Inject
	private DatarouterHBaseSettingRoot datarouterHBaseSettingRoot;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private HBaseConnectionHolder hBaseConnectionHolder;

	private final Map<ClientId,HBaseTablePool> hBasetablePoolsByClientId = new ConcurrentHashMap<>();

	public void register(ClientId clientId, Connection connection){
		hBaseConnectionHolder.register(clientId, connection);
		if(hBasetablePoolsByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered an HBaseTablePool");
		}
		ClientType<?,?> clientType = datarouterClients.getClientTypeInstance(clientId);
		HBaseTablePool hBaseTablePool = new HBaseTablePool(hBaseOptions, datarouterHBaseSettingRoot, connection,
				clientId, clientType);
		hBasetablePoolsByClientId.put(clientId, hBaseTablePool);
	}

	public HBaseTablePool getHBaseTablePool(ClientId clientId){
		return hBasetablePoolsByClientId.get(clientId);
	}

}
