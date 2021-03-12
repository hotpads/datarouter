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
package io.datarouter.client.redis.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.datarouter.client.redis.RedisDatabeanCodec;
import io.datarouter.client.redis.RedisTallyCodec;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.client.RedisNodeOps;
import io.datarouter.client.redis.client.RedisOps;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public class RedisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{

	private final ExecutorService executor;
	private final RedisDatabeanCodec<PK,D,F> codec;
	private final RedisTallyCodec tallyCodec;
	private final RedisClientManager redisClientManager;
	private final ClientId clientId;

	public RedisNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			RedisClientManager redisClientManager,
			ExecutorService executor){
		super(params, clientType);
		this.executor = executor;
		int version = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new RedisDatabeanCodec<>(version, getFieldInfo());
		this.tallyCodec = new RedisTallyCodec(version, getFieldInfo());
		this.redisClientManager = redisClientManager;
		this.clientId = params.getClientId();
	}

	/*------------------------------- reader --------------------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return nodeOps().nodeExists(key);
	}

	@Override
	public D get(PK key, Config config){
		return nodeOps().nodeGet(key);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return nodeOps().nodeGetMulti(keys);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		if(keys.isEmpty()){
			return List.of();
		}
		return scanMulti(keys, config)
				.map(Databean::getKey)
				.list();
	}

	/*-------------------------------- writer -------------------------------*/

	@Override
	public void put(D databean, Config config){
		nodeOps().clientPut(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		nodeOps().nodePutMulti(databeans, config);
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(PK key, Config config){
		nodeOps().nodeDelete(key);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		nodeOps().nodeDeleteMulti(keys);
	}

	/*-------------------------------- tally --------------------------------*/

	@Override
	public Optional<Long> findTallyCount(String stringKey, Config config){
		return nodeOps().nodeFindTallyCount(stringKey);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> stringKeys, Config config){
		return nodeOps().getMultiTallyCount(stringKeys);
	}

	@Override
	public Long incrementAndGetCount(String stringKey, int delta, Config config){
		return nodeOps().nodeIncrementAndGetCount(stringKey, delta, config);
	}

	@Override
	public void deleteTally(String stringKey, Config config){
		nodeOps().nodeDeleteTally(stringKey);
	}

	/*-------------------------------- helper -------------------------------*/

	private RedisClusterAsyncCommands<byte[],byte[]> client(){
		return redisClientManager.getClient(clientId);
	}

	private RedisOps ops(){
		return new RedisOps(client());
	}

	private RedisNodeOps<PK,D,F> nodeOps(){
		return new RedisNodeOps<>(ops(), client(), codec, tallyCodec, executor);
	}

}
