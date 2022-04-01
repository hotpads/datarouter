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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.codec.RedisDatabeanCodec;
import io.datarouter.client.redis.util.RedisConfigTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.tuple.Twin;
import io.lettuce.core.KeyValue;

public class RedisDatabeanNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>{

	private final Supplier<DatarouterRedisClient> lazyClient;
	private final ExecutorService executor;
	private final RedisDatabeanCodec<PK,D,F> codec;

	public RedisDatabeanNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			Supplier<DatarouterRedisClient> lazyClient,
			ExecutorService executor){
		super(params, clientType);
		this.lazyClient = lazyClient;
		this.executor = executor;
		int version = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new RedisDatabeanCodec<>(version, getFieldInfo());
	}

	/*------------------------------- reader --------------------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return lazyClient.get().exists(codec.encodeKey(key));
	}

	@Override
	public D get(PK key, Config config){
		return lazyClient.get().find(codec.encodeKey(key))
				.map(codec::decode)
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(keys.isEmpty()){
			return List.of();
		}
		return lazyClient.get().mget(codec.encodeKeys(keys))
				.include(KeyValue::hasValue)
				.map(KeyValue::getValue)
				.map(codec::decode)
				.list();
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
		codec.encodeIfValid(databean).ifPresent(kvBytes -> {
			config.findTtl()
					.map(Duration::toMillis)
					.ifPresentOrElse(
							ttlMs -> lazyClient.get().psetex(kvBytes, ttlMs),
							() -> lazyClient.get().set(kvBytes));
		});
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(databeans.isEmpty()){
			return;
		}
		List<Twin<byte[]>> kvs = Scanner.of(databeans)
				.map(codec::encodeIfValid)
				.concat(OptionalScanner::of)
				.list();
		if(config.findTtl().isPresent()){
			Scanner.of(kvs)
					.parallel(new ParallelScannerContext(executor, 16, true))
					.forEach(kv -> lazyClient.get().psetex(kv, RedisConfigTool.getTtlMs(config)));
		}else{
			lazyClient.get().mset(kvs);
		}
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(PK key, Config config){
		lazyClient.get().del(codec.encodeKey(key));
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys.isEmpty()){
			return;
		}
		lazyClient.get().del(codec.encodeKeys(keys));
	}

}
