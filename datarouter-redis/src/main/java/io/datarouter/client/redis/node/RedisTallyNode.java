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
package io.datarouter.client.redis.node;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.client.RedisRequestConfig;
import io.datarouter.client.redis.codec.RedisTallyCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.lettuce.core.KeyValue;

public class RedisTallyNode
extends BasePhysicalNode<TallyKey,Tally,TallyFielder>
implements PhysicalTallyStorageNode{

	private final RedisTallyCodec codec;
	private final Supplier<DatarouterRedisClient> lazyClient;

	public RedisTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> params,
			ClientType<?,?> clientType,
			RedisTallyCodec codec,
			Supplier<DatarouterRedisClient> lazyClient){
		super(params, clientType);
		this.codec = codec;
		this.lazyClient = lazyClient;
	}

	@Override
	public Long incrementAndGetCount(String id, int delta, Config config){
		byte[] encodedId = codec.encodeId(id);
		long count = lazyClient.get().incrby(
				encodedId,
				delta,
				RedisRequestConfig.forWrite(getName(), config));
		config.findTtl()
				.ifPresent(ttl -> lazyClient.get().pexpire(
						encodedId,
						ttl,
						RedisRequestConfig.forWrite(getName(), config)));
		return count;
	}

	@Override
	public Optional<Long> findTallyCount(String id, Config config){
		return Optional.of(id)
				.map(codec::encodeId)
				.flatMap(encodedId -> lazyClient.get().find(
						encodedId,
						RedisRequestConfig.forRead(getName(), config)))
				.map(codec::decodeValue);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> ids, Config config){
		return Scanner.of(ids)
				.map(codec::encodeId)
				.listTo(encodedIds -> lazyClient.get().mget(
						encodedIds,
						RedisRequestConfig.forRead(getName(), config)))
				.include(KeyValue::hasValue)
				.toMap(codec::decodeId, codec::decodeValue);
	}

	@Override
	public void deleteTally(String id, Config config){
		Optional.of(id)
				.map(codec::encodeId)
				.ifPresent(encodedKey -> lazyClient.get().del(
						encodedKey,
						RedisRequestConfig.forWrite(getName(), config)));
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
