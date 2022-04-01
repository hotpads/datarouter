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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.client.redis.client.DatarouterRedisClient;
import io.datarouter.client.redis.codec.RedisTallyCodec;
import io.datarouter.client.redis.util.RedisConfigTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;

public class RedisTallyNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{

	private final Supplier<DatarouterRedisClient> lazyClient;
	private final RedisTallyCodec codec;

	public RedisTallyNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			Supplier<DatarouterRedisClient> lazyClient){
		super(params, clientType);
		this.lazyClient = lazyClient;
		int version = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new RedisTallyCodec(version, getFieldInfo());
	}

	@Override
	public Optional<Long> findTallyCount(String stringKey, Config config){
		byte[] tallyKeyBytes = codec.encodeKey(new TallyKey(stringKey));
		Optional<byte[]> byteTally = lazyClient.get().find(tallyKeyBytes);
		return codec.decodeTallyValue(byteTally);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> stringKeys, Config config){
		return Scanner.of(stringKeys)
				.map(TallyKey::new)
				.map(codec::encodeKey)
				.listTo(lazyClient.get()::mget)
				.toMap(entry -> codec.decodeKey(entry.getKey()).getId(),
						entry -> codec.decodeTallyValue(entry).orElse(0L));
	}

	@Override
	public Long incrementAndGetCount(String stringKey, int delta, Config config){
		byte[] keyBytes = codec.encodeKey(new TallyKey(stringKey));
		long ttlMs = RedisConfigTool.getTtlMs(config);
		return lazyClient.get().incrbyAndPexpire(keyBytes, delta, ttlMs);
	}

	@Override
	public void deleteTally(String stringKey, Config config){
		lazyClient.get().del(codec.encodeKey(new TallyKey(stringKey)));
	}

}
