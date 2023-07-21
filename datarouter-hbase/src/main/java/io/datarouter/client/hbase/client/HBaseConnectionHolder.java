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
package io.datarouter.client.hbase.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.hbase.client.Connection;

import io.datarouter.storage.client.ClientId;
import jakarta.inject.Singleton;

@Singleton
public class HBaseConnectionHolder{

	private final Map<ClientId,Connection> connectionsByClientId = new ConcurrentHashMap<>();

	public void register(ClientId clientId, Connection connection){
		if(connectionsByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a connection");
		}
		connectionsByClientId.put(clientId, connection);
	}

	public Connection getConnection(ClientId clientId){
		return connectionsByClientId.get(clientId);
	}

}
