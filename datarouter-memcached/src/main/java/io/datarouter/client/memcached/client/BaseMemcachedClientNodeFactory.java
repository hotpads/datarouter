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
package io.datarouter.client.memcached.client;

import javax.inject.Singleton;

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.client.memcached.ReservedBlobPaths;
import io.datarouter.client.memcached.codec.MemcachedBlobCodec;
import io.datarouter.client.memcached.codec.MemcachedDatabeanCodec;
import io.datarouter.client.memcached.codec.MemcachedTallyCodec;
import io.datarouter.client.memcached.node.MemcachedBlobNode;
import io.datarouter.client.memcached.node.MemcachedDatabeanNode;
import io.datarouter.client.memcached.node.MemcachedTallyNode;
import io.datarouter.client.memcached.util.DatabeanNodePrefix;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;
import io.datarouter.web.config.service.ServiceName;

@Singleton
public class BaseMemcachedClientNodeFactory
implements BlobClientNodeFactory, DatabeanClientNodeFactory, TallyClientNodeFactory{

	private final ClientType<?,?> memcachedClientType;
	private final ServiceName serviceName;
	private final BaseMemcachedClientManager memcachedClientManager;
	private final NodeAdapters nodeAdapters;

	public BaseMemcachedClientNodeFactory(
			ClientType<?,?> memcachedClientType,
			ServiceName serviceName,
			BaseMemcachedClientManager memcachedClientManager,
			NodeAdapters nodeAdapters){
		this.memcachedClientType = memcachedClientType;
		this.serviceName = serviceName;
		this.memcachedClientManager = memcachedClientManager;
		this.nodeAdapters = nodeAdapters;
	}

	/*---------------- BlobClientNodeFactory ------------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		var node = new MemcachedBlobNode(
				nodeParams,
				memcachedClientType,
				new MemcachedBlobCodec(nodeParams.getPath()),
				memcachedClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapBlobNode(node);
	}

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
		var fieldInfo = new PhysicalDatabeanFieldInfo<>(nodeParams);
		Subpath path = new DatabeanNodePrefix(
				ReservedBlobPaths.DATABEAN,
				MemcachedDatabeanNode.CODEC_VERSION,
				serviceName.get(),
				"1",//placeholder for client-scoped version
				nodeParams,
				fieldInfo)
				.makeShortenedSubpath();
		var blobParams = new NodeParamsBuilder<>(nodeParams)
				.withPath(path)
				.build();
		var blobNode = new MemcachedBlobNode(
				toPathbeanParams(blobParams),
				memcachedClientType,
				new MemcachedBlobCodec(path),
				memcachedClientManager.getLazyClient(nodeParams.getClientId()));
		var databeanCodec = new MemcachedDatabeanCodec<PK,D,F>(
				memcachedClientType.getName(),
				fieldInfo.getSampleFielder(),
				fieldInfo.getDatabeanSupplier(),
				fieldInfo.getFieldByPrefixedName(),
				CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH,
				ByteUnitType.MiB.toBytesInt(2),//this is a bit fuzzy due to compression
				path.toString().length());
		var node = new MemcachedDatabeanNode<>(
				nodeParams,
				memcachedClientType,
				blobNode,
				databeanCodec);
		return nodeAdapters.wrapDatabeanMapNode(node);
	}

	@SuppressWarnings("unchecked")
	private NodeParams<PathbeanKey,Pathbean,PathbeanFielder> toPathbeanParams(NodeParams<?,?,?> params){
		return (NodeParams<PathbeanKey,Pathbean,PathbeanFielder>)params;
	}

	/*---------------- TallyClientNodeFactory ------------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> nodeParams){
		var fieldInfo = new PhysicalDatabeanFieldInfo<>(nodeParams);
		Subpath path = new DatabeanNodePrefix(
				ReservedBlobPaths.TALLY,
				MemcachedTallyNode.CODEC_VERSION,
				serviceName.get(),
				"1",//placeholder for client-scoped version
				nodeParams,
				fieldInfo)
				.makeShortenedSubpath();
		var tallyCodec = new MemcachedTallyCodec(
				memcachedClientType.getName(),
				path,
				CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH);
		var node = new MemcachedTallyNode<>(
				nodeParams,
				memcachedClientType,
				tallyCodec,
				memcachedClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapTallyNode(node);
	}

}
