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
package io.datarouter.virtualnode.caching;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import io.datarouter.storage.util.DatarouterCounters;

public class MapCachingMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends BaseMapCachingNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D,F>{

	private static final Config DEFAULT_CACHING_NODE_CONFIG = new Config().setTimeout(Duration.ofMillis(100));

	protected final boolean cacheReads;

	public MapCachingMapStorageReaderNode(N cacheNode, N backingNode, boolean cacheReads){
		super(cacheNode, backingNode);
		this.cacheReads = cacheReads;
	}

	public static Config getEffectiveCachingNodeConfig(Config givenConfig){
		if(givenConfig == null){
			return DEFAULT_CACHING_NODE_CONFIG;
		}
		// avoid changing user object
		givenConfig = givenConfig.clone();
		// ignore exception on the cache and try the backing node
		givenConfig.setIgnoreException(true);
		// fine print:
		// for memcached, timeout <= 0 implies no wait per net.spy.memcached.internal.GetFuture
		//   and java.util.concurrent.CountDownLatch.await(long, java.util.concurrent.TimeUnit)
		// for redis, this setting is not used since the timeout is fixed at the time of client node construction
		if(givenConfig.getTimeout() != null
				&& givenConfig.getTimeout().toMillis() <= DEFAULT_CACHING_NODE_CONFIG.getTimeout().toMillis()){
			return givenConfig;
		}
		givenConfig.setTimeout(DEFAULT_CACHING_NODE_CONFIG.getTimeout());
		return givenConfig;
	}

	/*---------------------------MapStorageReader ---------------------------*/

	@Override
	public boolean exists(PK key, Config config){
		if(!useCache(config)){
			return backingNode.exists(key, config);
		}
		try{
			updateLastAttemptedContact();
			if(cachingNode.exists(key, getEffectiveCachingNodeConfig(config))){
				countHits();
				return true;
			}
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.exists(key, config);
		}
		countMisses();
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config){
		if(!useCache(config)){
			return backingNode.get(key, config);
		}
		Config effectiveCachingNodeConfig = getEffectiveCachingNodeConfig(config);
		D cachedObject;
		try{
			updateLastAttemptedContact();
			cachedObject = cachingNode.get(key, effectiveCachingNodeConfig);
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.get(key, config);
		}
		if(cachedObject != null){
			countHits();
			return cachedObject;
		}
		D realObject = backingNode.get(key, config);
		if(realObject != null){
			countMisses();
			if(cacheReads){
				try{
					updateLastAttemptedContact();
					cachingNode.put(realObject, effectiveCachingNodeConfig);
					updateLastContact();
				}catch(Exception e){
					countExceptions();
				}
			}
		}
		return realObject;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return Collections.emptyList();
		}
		if(!useCache(config)){
			return backingNode.getMulti(keys, config);
		}
		List<D> resultBuilder = new ArrayList<>();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getMulti(keys, getEffectiveCachingNodeConfig(config)));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getMulti(keys, config);
		}
		countHits();
		Set<PK> cachedKeys = resultBuilder.stream()
				.map(Databean::getKey)
				.collect(Collectors.toSet());
		Set<PK> uncachedKeys = keys.stream()
				.filter(Predicate.not(cachedKeys::contains))
				.collect(Collectors.toSet());
		if(uncachedKeys.isEmpty()){
			return resultBuilder;
		}
		List<D> fromBackingNode = getAndCacheDatabeans(uncachedKeys, config);
		resultBuilder.addAll(fromBackingNode);
		return resultBuilder;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return Collections.emptyList();
		}
		if(!useCache(config)){
			return backingNode.getKeys(keys, config);
		}
		List<PK> resultBuilder = new ArrayList<>();
		try{
			updateLastAttemptedContact();
			resultBuilder.addAll(cachingNode.getKeys(keys, getEffectiveCachingNodeConfig(config)));
			updateLastContact();
		}catch(Exception e){
			countExceptions();
			return backingNode.getKeys(keys, config);
		}
		countHits();
		Set<PK> cachedKeys = new HashSet<>(resultBuilder);
		Set<PK> uncachedKeys = keys.stream()
				.filter(Predicate.not(cachedKeys::contains))
				.collect(Collectors.toSet());
		if(uncachedKeys.isEmpty()){
			return resultBuilder;
		}
		List<D> fromBackingNode = getAndCacheDatabeans(uncachedKeys, config);
		Scanner.of(fromBackingNode).map(Databean::getKey).forEach(resultBuilder::add);
		return resultBuilder;
	}

	private List<D> getAndCacheDatabeans(Collection<PK> uncachedKeys, Config config){
		List<D> fromBackingNode = backingNode.getMulti(uncachedKeys, config);
		countMisses();
		if(cacheReads){
			try{
				updateLastAttemptedContact();
				cachingNode.putMulti(fromBackingNode, getEffectiveCachingNodeConfig(config));
				updateLastContact();
			}catch(Exception e){
				countExceptions();
			}
		}
		return fromBackingNode;
	}

	/*------------------------------- counters ------------------------------*/

	private void countHits(){
		DatarouterCounters.incOp(null, getName() + " hit");
	}

	private void countMisses(){
		DatarouterCounters.incOp(null, getName() + " miss");
	}

	private void countExceptions(){
		DatarouterCounters.incOp(null, getName() + " exception");
	}

}
