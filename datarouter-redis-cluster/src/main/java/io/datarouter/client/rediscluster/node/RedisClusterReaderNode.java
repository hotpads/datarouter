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
package io.datarouter.client.rediscluster.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.client.rediscluster.RedisClusterClientType;
import io.datarouter.client.rediscluster.client.RedisClusterClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.TallyStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.storage.util.EncodedPrimaryKeyPercentCodec;
import io.datarouter.util.collection.CollectionTool;
import redis.clients.jedis.JedisCluster;

public class RedisClusterReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, TallyStorageReader<PK,D>{

	private final Integer databeanVersion;
	protected final JedisCluster client;

	public RedisClusterReaderNode(
			NodeParams<PK,D,F> params,
			RedisClusterClientType redisClientType,
			RedisClusterClientManager redisClientManager,
			ClientId clientId){
		super(params, redisClientType);
		client = redisClientManager.getJedis(clientId);
		this.databeanVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
	}

	@Override
	public boolean exists(PK key, Config config){
		return client.exists(buildRedisKey(key));
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		String json = client.get(buildRedisKey(key));
		if(json == null){
			return null;
		}
		return JsonDatabeanTool.databeanFromJson(getFieldInfo().getDatabeanSupplier(), getFieldInfo()
				.getSampleFielder(), json);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		return client.mget(buildRedisKeys(keys).toArray(new String[keys.size()])).stream()
				.filter(Objects::nonNull)
				.map(bean -> JsonDatabeanTool.databeanFromJson(getFieldInfo().getDatabeanSupplier(), getFieldInfo()
						.getSampleFielder(), bean))
				.collect(Collectors.toList());
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		return Scanner.of(getMulti(keys, config)).map(Databean::getKey).list();
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		if(key == null){
			return null;
		}
		return Optional.ofNullable(client.get(buildRedisKey(new TallyKey(key))))
				.map(String::trim)
				.map(Long::valueOf);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		return keys.stream()
				.collect(Collectors.toMap(Function.identity(), key -> findTallyCount(key).orElse(0L)));
	}

	protected String buildRedisKey(PrimaryKey<?> pk){
		return new EncodedPrimaryKeyPercentCodec(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildRedisKeys(Collection<? extends PrimaryKey<?>> pks){
		return EncodedPrimaryKeyPercentCodec.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

}
