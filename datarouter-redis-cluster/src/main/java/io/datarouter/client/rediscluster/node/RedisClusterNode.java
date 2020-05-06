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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.rediscluster.RedisClusterClientType;
import io.datarouter.client.rediscluster.client.RedisClusterClientManager;
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
import redis.clients.jedis.params.SetParams;

public class RedisClusterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends RedisClusterReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>,
		PhysicalTallyStorageNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(RedisClusterNode.class);

	//redis can handle a max keys size of 32 megabytes
	private static final int MAX_REDIS_KEY_SIZE = 1024 * 64;

	public RedisClusterNode(
			NodeParams<PK,D,F> params,
			RedisClusterClientType redisClientType,
			RedisClusterClientManager redisClientManager,
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
		if(ttl == null){
			client.set(key, jsonBean);
		}else{
			// XX - Only set they key if it already exists
			// PX - Milliseconds
			client.set(key, jsonBean, new SetParams().xx().px(ttl));
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans == null || databeans.isEmpty()){
			return;
		}
		// redis cannot handle batch puts
		databeans.forEach(databean -> put(databean, config));
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
		client.del(buildRedisKey(key));
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return;
		}
		keys.forEach(key -> delete(key, config));
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		if(key == null){
			return null;
		}
		String tallyKey = buildRedisKey(new TallyKey(key));
		Long expiration = getTtlMs(config);
		if(expiration == null){
			return client.incrBy(tallyKey, delta).longValue();
		}
		Long response = client.incrBy(tallyKey, delta);
		client.pexpire(tallyKey, expiration);
		return response.longValue();
	}

	@Override
	public void deleteTally(String key, Config config){
		if(key == null){
			return;
		}
		client.del(buildRedisKey(new TallyKey(key)));
	}

	private Long getTtlMs(Config config){
		if(config == null){
			return null;
		}
		return config.getTtl() == null ? Long.MAX_VALUE : config.getTtl().toMillis();
	}

}
