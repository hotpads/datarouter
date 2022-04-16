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
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.codec.RedisTallyCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.lettuce.core.KeyValue;

public class RedisTallyNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{

	private final RedisTallyCodec codec;
	private final Supplier<DatarouterRedisClient> lazyClient;

	public RedisTallyNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			RedisTallyCodec codec,
			Supplier<DatarouterRedisClient> lazyClient){
		super(params, clientType);
		this.codec = codec;
		this.lazyClient = lazyClient;
	}

	@Override
	public Long incrementAndGetCount(String id, int delta, Config config){
		byte[] idBytes = codec.encodeId(id);
		long count = lazyClient.get().incrby(idBytes, delta);
		config.findTtl()
				.map(Duration::toMillis)
				.ifPresent(ttlMs -> lazyClient.get().pexpire(idBytes, ttlMs));
		return count;
	}

	@Override
	public Optional<Long> findTallyCount(String id, Config config){
		return Optional.of(id)
				.map(codec::encodeId)
				.flatMap(lazyClient.get()::find)
				.map(codec::decodeValue);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> ids, Config config){
		return Scanner.of(ids)
				.map(codec::encodeId)
				.listTo(lazyClient.get()::mget)
				.include(KeyValue::hasValue)
				.toMap(codec::decodeId, codec::decodeValue);
	}

	@Override
	public void deleteTally(String id, Config config){
		Optional.of(id)
				.map(codec::encodeId)
				.ifPresent(lazyClient.get()::del);
	}

}
