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
package io.datarouter.client.hbase.balancer;

import io.datarouter.client.hbase.HBaseClientType;
import io.datarouter.client.hbase.balancer.imp.ConsistentHashBalancer;
import io.datarouter.client.hbase.balancer.imp.NoOpBalancer;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultHBaseBalancerFactory implements HBaseBalancerFactory{

	@Inject
	private DatarouterClients clients;

	@Override
	public BaseHBaseRegionBalancer getBalancerForTable(ClientId clientId, String tableName){
		if(!isHBase(clientId)){
			return new NoOpBalancer(tableName);
		}
		return new ConsistentHashBalancer(tableName);
	}

	private boolean isHBase(ClientId clientId){
		return HBaseClientType.class == clients.getClientTypeInstance(clientId).getClass();
	}

}
