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
package io.datarouter.client.gcp.pubsub;

import io.datarouter.bytes.Codec;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubSettingsRoot;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubGroupNode;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.NodeParams;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcpPubsubNodeFactory{

	@Inject
	private EnvironmentName environmentName;
	@Inject
	private ServiceName serviceName;
	@Inject
	private GcpPubsubClientType clientType;
	@Inject
	private GcpPubsubClientManager clientManager;
	@Inject
	private DatarouterGcpPubsubSettingsRoot settingRoot;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	GcpPubsubNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new GcpPubsubNode<>(
				environmentName,
				serviceName,
				params,
				clientType,
				clientManager,
				settingRoot,
				params.getClientId());
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	GcpPubsubGroupNode<PK,D,F> createGroupNode(NodeParams<PK,D,F> params){
		return new GcpPubsubGroupNode<>(
				environmentName,
				serviceName,
				params,
				clientType,
				clientManager,
				settingRoot,
				params.getClientId());
	}

	public <T> GcpPubsubBlobNode<T> createBlobNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			Codec<T,byte[]> codec){
		return new GcpPubsubBlobNode<>(
				params,
				codec,
				clientType,
				clientManager,
				settingRoot,
				environmentName,
				serviceName);
	}

}
