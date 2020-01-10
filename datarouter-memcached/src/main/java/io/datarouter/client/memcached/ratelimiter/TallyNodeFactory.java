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
package io.datarouter.client.memcached.ratelimiter;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.memcached.BaseMemcachedClientNodeFactory;
import io.datarouter.client.memcached.node.MemcachedNode;
import io.datarouter.client.memcached.tally.Tally;
import io.datarouter.client.memcached.tally.Tally.TallyFielder;
import io.datarouter.client.memcached.tally.TallyKey;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;

@Singleton
public class TallyNodeFactory{

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterInjector injector;

	public MemcachedNode<TallyKey,Tally,TallyFielder> create(ClientId clientId, int version){
		return create(clientId, version, null);
	}

	public MemcachedNode<TallyKey,Tally,TallyFielder> create(ClientId clientId, int version, String tableName){
		@SuppressWarnings("unchecked")
		ClientType<? extends BaseMemcachedClientNodeFactory,?> clientType =
				(ClientType<? extends BaseMemcachedClientNodeFactory,?>)datarouterClients.getClientTypeInstance(
				clientId);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + clientId.getName());
		NodeParams<TallyKey,Tally,TallyFielder> params = new NodeParamsBuilder<>(Tally::new, TallyFielder::new)
				.withClientId(clientId)
				.withSchemaVersion(version)
				.withTableName(tableName)
				.build();
		return injector.getInstance(clientType.getClientNodeFactoryClass()).createNodeWithoutAdapters(params);
	}

}
