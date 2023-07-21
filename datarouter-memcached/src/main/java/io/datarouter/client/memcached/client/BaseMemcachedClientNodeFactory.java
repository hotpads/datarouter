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

import io.datarouter.client.memcached.codec.MemcachedBlobCodec;
import io.datarouter.client.memcached.codec.MemcachedTallyCodec;
import io.datarouter.client.memcached.node.MemcachedBlobNode;
import io.datarouter.client.memcached.node.MemcachedTallyNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.ReservedBlobPaths;
import io.datarouter.storage.node.DatabeanNodePrefix;
import io.datarouter.storage.node.DatabeanToBlobCodec;
import io.datarouter.storage.node.DatabeanToBlobNode;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Singleton;

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
	public PhysicalBlobStorageNode createBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		var codec = new MemcachedBlobCodec(nodeParams.getPath());
		var node = new MemcachedBlobNode(
				nodeParams,
				memcachedClientType,
				codec,
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
				DatabeanToBlobCodec.CODEC_VERSION,
				serviceName.get(),
				"1",//placeholder for client-scoped version
				nodeParams,
				fieldInfo)
				.makeShortenedSubpath();//Store shortened path because of memcached's limited key length.
		var blobParams = new NodeParamsBuilder<>(nodeParams)
				.withPath(path)
				.build();
		var blobNode = new MemcachedBlobNode(
				castParams(blobParams),
				memcachedClientType,
				new MemcachedBlobCodec(path),
				memcachedClientManager.getLazyClient(nodeParams.getClientId()));
		var databeanCodec = new DatabeanToBlobCodec<PK,D,F>(
				memcachedClientType.getName(),
				fieldInfo.getSampleFielder(),
				fieldInfo.getDatabeanSupplier(),
				fieldInfo.getFieldByPrefixedName(),
				path,
				CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH,
				CommonFieldSizes.MEMCACHED_MAX_VALUE_LENGTH);
		var node = new DatabeanToBlobNode<>(
				nodeParams,
				memcachedClientType,
				blobNode,
				databeanCodec);
		return nodeAdapters.wrapDatabeanMapNode(node);
	}

	@SuppressWarnings("unchecked")
	private NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> castParams(NodeParams<?,?,?> params){
		return (NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>)params;
	}

	/*---------------- TallyClientNodeFactory ------------------*/

	@Override
	public PhysicalTallyStorageNode createTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> nodeParams){
		var fieldInfo = new PhysicalDatabeanFieldInfo<>(nodeParams);
		Subpath path = new DatabeanNodePrefix(
				ReservedBlobPaths.TALLY,
				MemcachedTallyCodec.CODEC_VERSION,
				serviceName.get(),
				"1",//placeholder for client-scoped version
				nodeParams,
				fieldInfo)
				.makeShortenedSubpath();
		var tallyCodec = new MemcachedTallyCodec(
				memcachedClientType.getName(),
				path,
				CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH);
		var node = new MemcachedTallyNode(
				nodeParams,
				memcachedClientType,
				tallyCodec,
				memcachedClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapTallyNode(node);
	}

}
