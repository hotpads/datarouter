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
package io.datarouter.client.memcached.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedOps;
import io.datarouter.client.memcached.codec.MemcachedDatabeanCodec;
import io.datarouter.client.memcached.codec.MemcachedTallyCodec;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.tuple.Pair;

public class MemcachedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedNode.class);

	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

	private final Integer schemaVersion;
	private final MemcachedOps ops;
	private final ClientId clientId;
	private final MemcachedDatabeanCodec<PK,D,F> codec;
	private final MemcachedTallyCodec tallyCodec;

	public MemcachedNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			MemcachedClientManager memcachedClientManager){
		super(params, clientType);
		this.ops = new MemcachedOps(memcachedClientManager);
		this.clientId = params.getClientId();
		this.schemaVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new MemcachedDatabeanCodec<>(
				getName(),
				schemaVersion,
				getFieldInfo().getSampleFielder(),
				getFieldInfo().getDatabeanSupplier(),
				getFieldInfo().getFieldByPrefixedName());
		this.tallyCodec = new MemcachedTallyCodec(
				getName(),
				schemaVersion);
	}

	/*--------------- MapStorage write ---------------*/

	@Override
	public void put(D databean, Config config){
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		Scanner.of(databeans)
				.map(codec::encodeKeyValueIfValid)
				.concat(OptionalScanner::of)
				.forEach(keyAndValue -> setInternal(keyAndValue.getLeft(), keyAndValue.getRight(), config));
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(List.of(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.map(codec::encodeKey)
				.forEach(memcachedStringKey -> deleteInternal(memcachedStringKey, config));
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	/*--------------- TallyStorage write ---------------*/

	@Override
	public Long incrementAndGetCount(String tallyStringKey, int delta, Config config){
		String memcachedStringKey = tallyCodec.encodeKey(new TallyKey(tallyStringKey));
		int expirationSeconds = MemcachedExpirationTool.getExpirationSeconds(config);
		try{
			return ops.increment(clientId, memcachedStringKey, delta, expirationSeconds);
		}catch(RuntimeException exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedStringKey, exception);
				return null;
			}
			throw exception;
		}
	}

	@Override
	public void deleteTally(String tallyStringKey, Config config){
		String memcachedStringKey = tallyCodec.encodeKey(new TallyKey(tallyStringKey));
		deleteInternal(memcachedStringKey, config);
	}

	/*--------------- MapStorageReader ---------------*/

	@Override
	public boolean exists(PK key, Config config){
		return scanMultiInternal(List.of(key), config)
				.hasAny();
	}

	@Override
	public D get(PK key, Config config){
		return scanMultiInternal(List.of(key), config)
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return scanMultiInternal(keys, config)
				.map(Databean::getKey)
				.list();
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return scanMultiInternal(keys, config)
				.list();
	}

	private Scanner<D> scanMultiInternal(Collection<PK> keys, Config config){
		return Scanner.of(keys)
				.map(codec::encodeKey)
				.listTo(memcachedStringKeys -> ops.fetch(
						clientId,
						getName(),
						memcachedStringKeys,
						config.getTimeout().toMillis(),
						config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)))
				.map(codec::decodeResultValue);
	}

	/*--------------- TallyStorageReader ---------------*/

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		return Optional.ofNullable(getMultiTallyCount(List.of(key), config).get(key));
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> tallyStringKeys, Config config){
		if(tallyStringKeys.isEmpty()){
			return Map.of();
		}
		return Scanner.of(tallyStringKeys)
				.map(TallyKey::new)
				.map(tallyCodec::encodeKey)
				.listTo(memcachedStringKeys -> ops.fetch(
						clientId,
						getName(),
						memcachedStringKeys,
						config.getTimeout().toMillis(),
						config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)))
				.map(tallyCodec::decodeResult)
				.toMap(Pair::getLeft, Pair::getRight);
	}


	/*--------------- private ---------------*/

	private void setInternal(String memcachedStringKey, byte[] bytes, Config config){
		int expirationSeconds = MemcachedExpirationTool.getExpirationSeconds(config);
		try{
			ops.set(clientId, getName(), memcachedStringKey, expirationSeconds, bytes);
		}catch(Exception exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedStringKey, exception);
			}else{
				throw exception;
			}
		}
	}

	private void deleteInternal(String memcachedStringKey, Config config){
		try{
			ops.delete(clientId, getName(), memcachedStringKey, config.getTimeout());
		}catch(Exception exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedStringKey, exception);
			}else{
				throw exception;
			}
		}
	}

}
