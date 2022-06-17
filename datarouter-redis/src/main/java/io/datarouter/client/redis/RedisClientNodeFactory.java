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
package io.datarouter.client.redis;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.codec.RedisBlobCodec;
import io.datarouter.client.redis.codec.RedisTallyCodec;
import io.datarouter.client.redis.node.RedisBlobNode;
import io.datarouter.client.redis.node.RedisTallyNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
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
import io.datarouter.web.config.service.ServiceName;

@Singleton
public class RedisClientNodeFactory
implements BlobClientNodeFactory, DatabeanClientNodeFactory, TallyClientNodeFactory{

	//Redis max key length is 512 MB but we'll start out shorter
	private static final int MAX_REDIS_KEY_SIZE = ByteUnitType.KiB.toBytesInt(64);
	private static final int MAX_REDIS_VALUE_SIZE = Integer.MAX_VALUE;//java array size limit

	@Inject
	private RedisClientType redisClientType;
	@Inject
	private RedisClientManager redisClientManager;
	@Inject
	private NodeAdapters nodeAdapters;
	@Inject
	private ServiceName serviceName;

	/*---------------- BlobClientNodeFactory ------------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		var codec = new RedisBlobCodec(nodeParams.getPath());
		var node = new RedisBlobNode(
				nodeParams,
				redisClientType,
				codec,
				redisClientManager.getLazyClient(nodeParams.getClientId()));
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
				.makeSubpath();
		var blobParams = new NodeParamsBuilder<>(nodeParams)
				.withPath(path)
				.build();
		var blobNode = new RedisBlobNode(
				toPathbeanParams(blobParams),
				redisClientType,
				new RedisBlobCodec(path),
				redisClientManager.getLazyClient(nodeParams.getClientId()));
		var codec = new DatabeanToBlobCodec<PK,D,F>(
				redisClientType.getName(),
				fieldInfo.getSampleFielder(),
				fieldInfo.getDatabeanSupplier(),
				fieldInfo.getFieldByPrefixedName(),
				path,
				MAX_REDIS_KEY_SIZE,
				MAX_REDIS_VALUE_SIZE);
		var node = new DatabeanToBlobNode<>(
				nodeParams,
				redisClientType,
				blobNode,
				codec);
		return nodeAdapters.wrapDatabeanMapNode(node);
	}

	@SuppressWarnings("unchecked")
	private NodeParams<PathbeanKey,Pathbean,PathbeanFielder> toPathbeanParams(NodeParams<?,?,?> params){
		return (NodeParams<PathbeanKey,Pathbean,PathbeanFielder>)params;
	}

	/*---------------- TallyClientNodeFactory ------------------*/

	@Override
	public PhysicalTallyStorageNode createTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> nodeParams){
		var fieldInfo = new PhysicalDatabeanFieldInfo<>(nodeParams);
		Subpath path = new DatabeanNodePrefix(
				ReservedBlobPaths.TALLY,
				RedisTallyCodec.TALLY_CODEC_VERSION,
				serviceName.get(),
				"1",//placeholder for client-scoped version
				nodeParams,
				fieldInfo)
				.makeSubpath();
		var codec = new RedisTallyCodec(path);
		var node = new RedisTallyNode(
				nodeParams,
				redisClientType,
				codec,
				redisClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapTallyNode(node);
	}

}
