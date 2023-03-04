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
package io.datarouter.client.gcp.pubsub.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.op.scan.queue.group.PeekMultiGroupUntilEmptyQueueStorageScanner;
import io.datarouter.storage.queue.BaseQueueMessage;
import io.datarouter.storage.queue.GroupQueueMessage;

public class GcpPubsubGroupNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseGcpPubsubNode<PK,D,F>
implements PhysicalGroupQueueStorageNode<PK,D,F>{

	public GcpPubsubGroupNode(
			EnvironmentName environmentName,
			ServiceName serviceName,
			NodeParams<PK,D,F> params,
			GcpPubsubClientType clientType,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(environmentName, serviceName, params, clientType, clientManager, clientId);
	}

	@Override
	public void put(D databean, Config config){
		gcpPubSubOpFactory.makeGroupPutMultiOp(Collections.singleton(databean), config).call();
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		gcpPubSubOpFactory.makeGroupPutMultiOp(databeans, config).call();
	}

	@Override
	public GroupQueueMessage<PK, D> peek(Config config){
		Config limitedConfig = config.clone().setLimit(1);
		return peekMulti(limitedConfig).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<GroupQueueMessage<PK,D>> peekMulti(Config config){
		return gcpPubSubOpFactory.makeGroupPeekMultiOp(config).call();
	}

	@SuppressWarnings("resource")
	@Override
	public Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		return new PeekMultiGroupUntilEmptyQueueStorageScanner<>(this, config)
				.concat(Scanner::of);
	}

	/*------------ reader/writer ---------------*/

	@Override
	public List<D> pollMulti(Config config){
		List<GroupQueueMessage<PK,D>> results = peekMulti(config);
		Scanner.of(results)
				.map(BaseQueueMessage::getKey)
				.flush(keys -> ackMulti(keys, config));
		return Scanner.of(results)
				.map(GroupQueueMessage::getDatabeans)
				.concat(Scanner::of)
				.list();
	}

}
