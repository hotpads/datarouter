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
package io.datarouter.aws.memcached.client.nodefactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.memcached.AwsMemcachedClientType;
import io.datarouter.aws.memcached.client.AwsMemcachedClientManager;
import io.datarouter.client.memcached.client.nodefactory.BaseMemcachedClientNodeFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class AwsMemcachedClientNodeFactory extends BaseMemcachedClientNodeFactory{

	private final AwsMemcachedNodeFactory memcachedNodeFactory;

	@Inject
	public AwsMemcachedClientNodeFactory(
			PhysicalMapStorageAvailabilityAdapterFactory factory,
			AwsMemcachedClientType clientType,
			AwsMemcachedClientManager clientManager,
			AwsMemcachedNodeFactory memcachedNodeFactory){
		super(factory, clientType, clientManager);
		this.memcachedNodeFactory = memcachedNodeFactory;
	}

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		return memcachedNodeFactory.createBlobNode(nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> nodeParams){
		return memcachedNodeFactory.createTallyNode(nodeParams);
	}

}
