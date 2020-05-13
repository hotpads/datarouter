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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.tally.TallyKey;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends RedisReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>,
		PhysicalTallyStorageNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(RedisNode.class);

	//redis can handle a max keys size of 32 megabytes
	private static final int MAX_REDIS_KEY_SIZE = 1024 * 64;

	public RedisNode(
			NodeParams<PK,D,F> params,
			RedisClientType redisClientType,
			RedisClientManager redisClientManager,
			ClientId clientId){
		super(params, redisClientType, redisClientManager, clientId);
	}

	@Override
	public void put(D databean, Config config){
		if(databean == null){
			return;
		}
		String key = buildRedisKey(databean.getKey());
		if(key.length() > MAX_REDIS_KEY_SIZE){
			String jsonKey = JsonDatabeanTool.fieldsToJson(databean.getKey().getFields()).toString();
			logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + jsonKey);
			return;
		}
		Long ttl = null;
		if(config != null && config.getTtl() != null){
			ttl = getTtlMs(config);
		}
		String jsonBean = JsonDatabeanTool.databeanToJsonString(databean, getFieldInfo().getSampleFielder());
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			if(ttl == null){
				client.set(key, jsonBean);
			}else{
				// XX - Only set they key if it already exists
				// PX - Milliseconds
				client.set(key, jsonBean, new SetParams().xx().px(ttl));
			}
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans == null || databeans.size() == 0){
			return;
		}
		if(config != null && config.getTtl() != null){
			// redis cannot handle both batch-puts and setting ttl
			databeans.forEach(databean -> put(databean, config));
			return;
		}
		List<String> keysAndDatabeans = new ArrayList<>();
		// redis mset(key1, value1, key2, value2, key3, value3, ...)
		for(D databean : databeans){
			String key = buildRedisKey(databean.getKey());
			if(key.length() > MAX_REDIS_KEY_SIZE){
				logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + key);
				continue;
			}
			String jsonBean = JsonDatabeanTool.databeanToJsonString(databean, getFieldInfo().getSampleFielder());
			keysAndDatabeans.add(key);
			keysAndDatabeans.add(jsonBean);
		}
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			client.mset(keysAndDatabeans.toArray(new String[keysAndDatabeans.size()]));
		}
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(PK key, Config config){
		if(key == null){
			return;
		}
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			client.del(buildRedisKey(key));
		}
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return;
		}
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			client.del(buildRedisKeys(keys).toArray(new String[keys.size()]));
		}
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		if(key == null){
			return null;
		}
		String tallyKey = buildRedisKey(new TallyKey(key));
		Long expiration = getTtlMs(config);
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			if(expiration == null){
				return client.incrBy(tallyKey, delta).longValue();
			}
			Long response = client.incrBy(tallyKey, delta);
			client.pexpire(tallyKey, expiration);
			return response.longValue();
		}
	}

	@Override
	public void deleteTally(String key, Config config){
		if(key == null){
			return;
		}
		try(Jedis client = redisClientManager.getJedis(clientId).getResource()){
			client.del(buildRedisKey(new TallyKey(key)));
		}
	}

	private Long getTtlMs(Config config){
		if(config == null){
			return null;
		}
		return config.getTtl() == null ? Long.MAX_VALUE : config.getTtl().toMillis();
	}

}
