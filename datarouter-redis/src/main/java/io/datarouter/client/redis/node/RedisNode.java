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

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.tally.TallyKey;
import io.lettuce.core.RedisFuture;

public class RedisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends RedisReaderNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(RedisNode.class);

	//redis can handle a max keys size of 32 megabytes
	private static final int MAX_REDIS_KEY_SIZE = 1024 * 64;

	public RedisNode(
			NodeParams<PK,D,F> params,
			RedisClientType redisClientType,
			RedisClientManager redisClientManager,
			ClientId clientId,
			ExecutorService executor){
		super(params, redisClientType, redisClientManager, clientId, executor);
	}

	@Override
	public void put(D databean, Config config){
		if(databean == null){
			return;
		}
		byte[] key = codec.encode(databean.getKey());
		if(key.length > MAX_REDIS_KEY_SIZE){
			logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + key);
			return;
		}
		Long ttl = null;
		if(config != null && config.getTtl() != null){
			ttl = getTtlMs(config);
		}
		byte[] databeanBytes = DatabeanTool.getBytes(databean, getFieldInfo().getSampleFielder());
		if(ttl == null){
			try{
				client().set(key, databeanBytes).get();
			}catch(InterruptedException | ExecutionException e){
				logger.error("", e);
			}
		}else{
			try{
				client().psetex(key, ttl, databeanBytes).get();
			}catch(InterruptedException | ExecutionException e){
				logger.error("", e);
			}
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans == null || databeans.isEmpty()){
			return;
		}
		Map<byte[],byte[]> keysAndDatabeans = new HashMap<>();
		for(D databean : databeans){
			byte[] key = codec.encode(databean.getKey());
			if(key.length > MAX_REDIS_KEY_SIZE){
				logger.error("redis object too big for redis! " + databean.getDatabeanName() + ", key: " + key);
				continue;
			}
			byte[] databeanBytes = DatabeanTool.getBytes(databean, getFieldInfo().getSampleFielder());
			keysAndDatabeans.put(key, databeanBytes);
		}
		Long ttl = null;
		if(config != null && config.getTtl() != null){
			ttl = getTtlMs(config);
		}
		if(ttl == null){
			try{
				client().mset(keysAndDatabeans).get();
			}catch(InterruptedException | ExecutionException e){
				logger.error("", e);
			}
		}else{
			long ttlMs = ttl;
			Scanner.of(keysAndDatabeans.entrySet())
					.parallel(new ParallelScannerContext(executor, 16, true))
					.forEach(entry -> {
						try{
							client().psetex(entry.getKey(), ttlMs, entry.getValue()).get();
						}catch(InterruptedException | ExecutionException e){
							logger.error("", e);
						}
					});
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
		try{
			client().del(codec.encode(key)).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return;
		}
		try{
			client().del(encodeKeys(keys)).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		if(key == null){
			return null;
		}
		byte[] tallyKey = codec.encode(new TallyKey(key));
		RedisFuture<Long> increment = client().incrby(tallyKey, delta);
		Long expiration = getTtlMs(config);
		RedisFuture<Boolean> expire = null;
		if(expiration != null){
			expire = client().pexpire(tallyKey, expiration);
		}
		try{
			long count = increment.get();
			expire.get();
			return count;
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		return null;
	}

	@Override
	public void deleteTally(String key, Config config){
		if(key == null){
			return;
		}
		try{
			client().del(codec.encode(new TallyKey(key))).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	private Long getTtlMs(Config config){
		if(config == null){
			return null;
		}
		return Optional.ofNullable(config.getTtl())
				.map(Duration::toMillis)
				.orElse(Long.MAX_VALUE);
	}

}
