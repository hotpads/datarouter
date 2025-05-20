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
package io.datarouter.gcp.spanner;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.node.SpannerBlobNode;
import io.datarouter.gcp.spanner.node.SpannerNode;
import io.datarouter.gcp.spanner.node.SpannerTallyNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerClientNodeFactory
implements BlobClientNodeFactory, DatabeanClientNodeFactory, TallyClientNodeFactory{

	@Inject
	private SpannerClientType spannerClientType;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private SpannerClientManager spannerClientManager;
	@Inject
	private SpannerFieldCodecs fieldCodecs;
	@Inject
	private NodeAdapters nodeAdapters;

	/*---------------- DatabeanClientNodeFactory ------------------*/

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createDatabeanNode(
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> nodeParams){
		var node = new SpannerNode<>(
				nodeParams,
				spannerClientType,
				managedNodesHolder,
				spannerClientManager,
				fieldCodecs);
		return nodeAdapters.wrapDatabeanIndexedNode(node);
	}

	@Override
	public PhysicalTallyStorageNode createTallyNode(NodeParams<TallyKey,Tally,TallyFielder> nodeParams){
		var node = new SpannerTallyNode(
				nodeParams,
				spannerClientType,
				spannerClientManager,
				fieldCodecs);
		return nodeAdapters.wrapTallyNode(node);
	}

	@Override
	public PhysicalBlobStorageNode createBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		var node = new SpannerBlobNode(
				nodeParams,
				spannerClientType,
				spannerClientManager,
				fieldCodecs);
		return nodeAdapters.wrapBlobNode(node);
	}

}
