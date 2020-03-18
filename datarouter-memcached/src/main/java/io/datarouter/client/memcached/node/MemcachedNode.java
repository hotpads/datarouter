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

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.client.DatarouterMemcachedKey;
import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.tally.TallyKey;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.iterable.IterableTool;

public class MemcachedNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends MemcachedReaderNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>, TallyStorage{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedNode.class);

	protected static final int MEGABYTE = 1024 * 1024;

	private final MemcachedClientManager memcachedClientManager;
	private final ClientId clientId;

	public MemcachedNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			MemcachedClientManager memcachedClientManager,
			ClientId clientId){
		super(params, clientType, memcachedClientManager, clientId);
		this.memcachedClientManager = memcachedClientManager;
		this.clientId = clientId;
	}

	@Override
	public void put(D databean, Config config){
		if(databean == null){
			return;
		}
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(CollectionTool.isEmpty(databeans)){
			return;
		}
		for(D databean : databeans){
			//TODO put only the nonKeyFields in the byte[] and figure out the keyFields from the key string
			//  could be big savings for small or key-only databeans
			byte[] bytes = DatabeanTool.getBytes(databean, getFieldInfo().getSampleFielder());
			if(bytes.length > 2 * MEGABYTE){
				//memcached max size is 1mb for a compressed object, so don't PUT things that won't compress well
				logger.error("object too big for memcached length={} key={}", bytes.length, databean.getKey());
				return;
			}
			String memcachedKey = buildMemcachedKey(databean.getKey());
			int expiration = getExpiration(config);
			try{
				clientSet(memcachedKey, expiration, bytes);
			}catch(RuntimeException exception){
				if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
					logger.error("memcached error on " + memcachedKey, exception);
				}else{
					throw exception;
				}
			}
		}
	}

	@Override
	public void delete(PK key, Config config){
		if(key == null){
			return;
		}
		deleteByKey(key, config);
	}

	@Override
	public void deleteTally(String key, Config config){
		deleteByKey(new TallyKey(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		if(keys == null){
			return;
		}
		keys.forEach(key -> delete(key, config));
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		if(key == null){
			return Optional.empty();
		}
		return Optional.ofNullable(getMultiTallyCount(List.of(key), config).get(key));
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		if(CollectionTool.isEmpty(keys)){ // TODO Move into an adapter
			return Collections.emptyMap();
		}
		List<TallyKey> tallyKeys = IterableTool.map(keys, TallyKey::new);
		Map<String,Object> bytesByStringKey = fetchBytesByStringKey(tallyKeys, config);
		if(bytesByStringKey == null){ // an ignored error occurred
			return Collections.emptyMap();
		}

		Map<String,Long> results = new HashMap<>();
		for(Entry<String,Object> entry : bytesByStringKey.entrySet()){
			String string = (String)entry.getValue();
			DatarouterMemcachedKey memcachedKey = DatarouterMemcachedKey.parse(entry.getKey(), TallyKey.class);
			results.put(((TallyKey)memcachedKey.primaryKey).getId(), Long.parseLong(string));
		}
		return results;
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		if(key == null){
			return null;
		}
		String memcachedKey = buildMemcachedKey(new TallyKey(key));
		try{
			return clientIncr(memcachedKey, delta, getExpiration(config));
		}catch(RuntimeException exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedKey, exception);
				return null;
			}
			throw exception;
		}
	}

	private void deleteByKey(PrimaryKey<?> pk, Config config){
		String memcacheKey = buildMemcachedKey(pk);
		try{
			clientDelete(memcacheKey, config.getTimeout());
		}catch(Exception exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcacheKey, exception);
			}else{
				throw exception;
			}
		}
	}

	private void clientSet(String memcachedKey, int expiration, byte[] bytes){
		try(var $ = startTraceSpan("set")){
			TracerTool.appendToSpanInfo("bytes", bytes.length);
			memcachedClientManager.getSpyMemcachedClient(clientId).set(memcachedKey, expiration, bytes);
		}
	}

	private long clientIncr(String memcacheKey, int delta, int expiration){
		try(var $ = startTraceSpan("incr")){
			// this cannot be async and use the client wide operationTimeout, with default of 2.5s
			return memcachedClientManager.getSpyMemcachedClient(clientId).incr(memcacheKey, delta, delta, expiration);
		}
	}

	private void clientDelete(String memcacheKey, Duration timeout){
		try(var $ = startTraceSpan("delete")){
			long start = System.currentTimeMillis();
			try{
				memcachedClientManager.getSpyMemcachedClient(clientId)
						.delete(memcacheKey)
						.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			}catch(TimeoutException e){
				TracerTool.appendToSpanInfo("memcached timeout");
				String details = "timeout after " + (System.currentTimeMillis() - start) + "ms";
				throw new RuntimeException(details, e);
			}catch(ExecutionException | InterruptedException e){
				TracerTool.appendToSpanInfo("memcached exception");
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * The exp value is passed along to memcached exactly as given, and will be processed per the memcached protocol
	 * specification:
	 *
	 * The actual value sent may either be Unix time (number of seconds since January 1, 1970, as a 32-bit value), or a
	 * number of seconds starting from current time. In the latter case, this number of seconds may not exceed
	 * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client is larger than that, the server will
	 * consider it to be real Unix time value rather than an offset from current time.
	 */
	private static int getExpiration(Config config){
		if(config == null){
			return 0; // Infinite time
		}
		Long timeoutSeconds = config.getTtl() == null
				? Long.MAX_VALUE
				: config.getTtl().toSeconds();
		Integer expiration = timeoutSeconds > Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: timeoutSeconds.intValue();
		return expiration;
	}

}
