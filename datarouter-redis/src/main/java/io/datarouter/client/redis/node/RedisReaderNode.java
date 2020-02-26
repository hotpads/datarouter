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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.databean.RedisDatabeanKey;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.collection.CollectionTool;

public class RedisReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>{

	private final Integer databeanVersion;
	private final RedisClientManager redisClientManager;
	private final ClientId clientId;

	public RedisReaderNode(NodeParams<PK,D,F> params, RedisClientType redisClientType,
			RedisClientManager redisClientManager, ClientId clientId){
		super(params, redisClientType);
		this.redisClientManager = redisClientManager;
		this.clientId = clientId;
		this.databeanVersion = Objects.requireNonNull(params.getSchemaVersion());
	}

	@Override
	public boolean exists(PK key, Config config){
		try{
			startTraceSpan("redis exists");
			return redisClientManager.getJedis(clientId).exists(buildRedisKey(key));
		}finally{
			finishTraceSpan();
		}
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		try{
			startTraceSpan("redis get");
			String json = redisClientManager.getJedis(clientId).get(buildRedisKey(key));
			if(json == null){
				return null;
			}
			return JsonDatabeanTool.databeanFromJson(getFieldInfo().getDatabeanSupplier(), getFieldInfo()
					.getSampleFielder(), json);
		}finally{
			finishTraceSpan();
		}
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return Collections.emptyList();
		}
		List<String> jsons = new ArrayList<>();
		try{
			startTraceSpan("redis get multi");
			jsons = redisClientManager.getJedis(clientId).mget(buildRedisKeys(keys).toArray(new String[keys.size()]));
		}finally{
			finishTraceSpan();
		}
		return jsons.stream()
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
		return DatabeanTool.getKeys(getMulti(keys, config));
	}

	public Long getTallyCount(RedisDatabeanKey key){
		if(key == null){
			return null;
		}
		try{
			startTraceSpan("redis getTallyCount");
			String tallyCount = redisClientManager.getJedis(clientId).get(buildRedisKey(key));
			if(tallyCount == null){
				return null;
			}
			return Long.valueOf(tallyCount.trim());
		}finally{
			finishTraceSpan();
		}
	}

	protected String buildRedisKey(PrimaryKey<?> pk){
		return new RedisNodeKey(getName(), databeanVersion, pk).getVersionedKeyString();
	}

	protected List<String> buildRedisKeys(Collection<? extends PrimaryKey<?>> pks){
		return RedisNodeKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

	protected void startTraceSpan(String opName){
		TracerTool.startSpan(TracerThreadLocal.get(), getName() + " " + opName);
	}

	protected void finishTraceSpan(){
		TracerTool.finishSpan(TracerThreadLocal.get());
	}

}
