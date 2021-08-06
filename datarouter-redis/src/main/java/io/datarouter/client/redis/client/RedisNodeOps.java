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
package io.datarouter.client.redis.client;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisDatabeanCodec;
import io.datarouter.client.redis.RedisTallyCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.tuple.Twin;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public class RedisNodeOps<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(RedisNodeOps.class);

	private final RedisOps ops;
	private final RedisClusterAsyncCommands<byte[],byte[]> client;
	private final RedisDatabeanCodec<PK,D,F> databeanCodec;
	private final RedisTallyCodec tallyCodec;
	private final ExecutorService executor;

	public RedisNodeOps(
			RedisOps ops,
			RedisClusterAsyncCommands<byte[],byte[]> client,
			RedisDatabeanCodec<PK,D,F> databeanCodec,
			RedisTallyCodec tallyCodec,
			ExecutorService executor){
		this.ops = ops;
		this.client = client;
		this.databeanCodec = databeanCodec;
		this.tallyCodec = tallyCodec;
		this.executor = executor;
	}

	/*------------------------------- reader --------------------------------*/

	public boolean nodeExists(PK key){
		return ops.exists(databeanCodec.encodeKey(key));
	}

	public D nodeGet(PK key){
		return ops.find(databeanCodec.encodeKey(key))
				.map(databeanCodec::decode)
				.orElse(null);
	}

	public List<D> nodeGetMulti(Collection<PK> keys){
		if(keys.isEmpty()){
			return List.of();
		}
		return ops.mget(databeanCodec.encodeKeys(keys))
				.include(KeyValue::hasValue)
				.map(KeyValue::getValue)
				.map(databeanCodec::decode)
				.list();
	}

	/*-------------------------------- writer -------------------------------*/

	public void clientPut(D databean, Config config){
		databeanCodec.encodeIfValid(databean).ifPresent(kvBytes -> {
			config.findTtl()
					.map(Duration::toMillis)
					.ifPresentOrElse(
							ttlMs -> ops.psetex(kvBytes, ttlMs),
							() -> ops.set(kvBytes));
		});
	}

	public void nodePutMulti(Collection<D> databeans, Config config){
		if(databeans.isEmpty()){
			return;
		}
		List<Twin<byte[]>> kvs = Scanner.of(databeans)
				.map(databeanCodec::encodeIfValid)
				.concat(OptionalScanner::of)
				.list();
		if(config.findTtl().isPresent()){
			Scanner.of(kvs)
					.parallel(new ParallelScannerContext(executor, 16, true))
					.forEach(kv -> ops.psetex(kv, getTtlMs(config)));
		}else{
			ops.mset(kvs);
		}
	}

	public void nodeDeleteMulti(Collection<PK> keys){
		if(keys.isEmpty()){
			return;
		}
		ops.del(databeanCodec.encodeKeys(keys));
	}

	public void nodeDelete(PK key){
		ops.del(databeanCodec.encodeKey(key));
	}

	/*-------------------------------- tally --------------------------------*/

	public Optional<Long> nodeFindTallyCount(String stringKey){
		byte[] tallyKeyBytes = tallyCodec.encodeKey(new TallyKey(stringKey));
		Optional<byte[]> byteTally = ops.find(tallyKeyBytes);
		return tallyCodec.decodeTallyValue(byteTally);
	}

	public Map<String,Long> getMultiTallyCount(Collection<String> stringKeys){
		return Scanner.of(stringKeys)
				.map(TallyKey::new)
				.map(tallyCodec::encodeKey)
				.listTo(ops::mget)
				.toMap(entry -> tallyCodec.decodeKey(entry.getKey()).getId(),
						entry -> tallyCodec.decodeTallyValue(entry).orElse(0L));
	}

	public Long nodeIncrementAndGetCount(String stringKey, int delta, Config config){
		byte[] tallyKeyBytes = tallyCodec.encodeKey(new TallyKey(stringKey));
		RedisFuture<Long> increment = client.incrby(tallyKeyBytes, delta);
		Long expiration = getTtlMs(config);
		RedisFuture<Boolean> expire = null;
		if(expiration != null){
			expire = client.pexpire(tallyKeyBytes, expiration);
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

	public void nodeDeleteTally(String stringKey){
		ops.del(tallyCodec.encodeKey(new TallyKey(stringKey)));
	}

	/*-------------------------------- helper -------------------------------*/

	private Long getTtlMs(Config config){
		return config.findTtl()
				.map(Duration::toMillis)
				.orElse(Long.MAX_VALUE);
	}

}
