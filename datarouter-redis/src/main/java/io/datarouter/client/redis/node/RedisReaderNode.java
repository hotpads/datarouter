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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.codec.BinaryDatabeanCodec;
import io.datarouter.model.serialize.codec.BinaryDatabeanCodec.BinaryDatabeanCodecBuilder;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.TallyStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class RedisReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements TallyStorageReader<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(RedisReaderNode.class);

	protected final ExecutorService executor;
	protected final BinaryDatabeanCodec codec;

	private final RedisClientManager redisClientManager;
	private final ClientId clientId;

	public RedisReaderNode(
			NodeParams<PK,D,F> params,
			RedisClientType redisClientType,
			RedisClientManager redisClientManager,
			ClientId clientId,
			ExecutorService executor){
		super(params, redisClientType);
		this.executor = executor;
		this.codec = new BinaryDatabeanCodecBuilder()
				.setDatabeanVersion(Optional.ofNullable(params.getSchemaVersion()).orElse(1))
				.setAllowNulls(false)
				.setTerminateIntermediateString(true)
				.setTerminateFinalString(true)
				.build();
		this.redisClientManager = redisClientManager;
		this.clientId = clientId;
	}

	@Override
	public boolean exists(PK key, Config config){
		try{
			return client().exists(codec.encode(key)).get() == 1;
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		return false;
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		byte[] bytes = null;
		try{
			bytes = client().get(codec.encode(key)).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		if(bytes == null){
			return null;
		}
		try{
			return codec.decode(
					getFieldInfo().getDatabeanSupplier(),
					getFieldInfo().getFieldByPrefixedName(),
					bytes);
		}catch(Exception e){
			logger.error("", e);
		}
		return null;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return List.of();
		}
		List<KeyValue<byte[],byte[]>> response = new ArrayList<>();
		try{
			response = client().mget(encodeKeys(keys)).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		return Scanner.of(response)
				.include(KeyValue::hasValue)
				.map(KeyValue::getValue)
				.exclude(Objects::isNull)
				.map(bytes -> {
					try{
						return codec.decode(
								getFieldInfo().getDatabeanSupplier(),
								getFieldInfo().getFieldByPrefixedName(),
								bytes);
					}catch(Exception e){
						logger.error("", e);
					}
					return null;
				})
				.list();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return List.of();
		}
		return Scanner.of(getMulti(keys, config))
				.map(Databean::getKey)
				.list();
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		if(key == null){
			return Optional.empty();
		}
		byte[] byteTally = null;
		try{
			byteTally = client().get(codec.encode(new TallyKey(key))).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
		if(byteTally == null || byteTally.length == 0){
			return Optional.empty();
		}
		// returned byte is ascii value of the long
		return Optional.ofNullable(byteTally)
				.map(String::new)
				.map(String::trim)
				.map(Long::valueOf);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		return Scanner.of(keys)
				.toMap(Function.identity(), key -> findTallyCount(key).orElse(0L));
	}

	protected byte[][] encodeKeys(Collection<? extends PrimaryKey<?>> pks){
		return codec.encodeMulti(pks).toArray(new byte[pks.size()][]);
	}

	protected RedisAsyncCommands<byte[],byte[]> client(){
		return redisClientManager.getClient(clientId).async();

	}

}
