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
package io.datarouter.util.cache;

import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LruTtlCache<K,V>{

	private final Duration ttl;
	private final int maxSize;
	private final Map<K,CachedObject<V>> map;

	private Clock clock; // not final for tests

	public static class LruTtlCacheBuilder<K,V>{

		private static final Duration DEFAULT_EXPIRE_TTL = Duration.ofSeconds(30);
		private static final int DEFAULT_MAX_SIZE = 10_000;

		private Duration expireTtl = DEFAULT_EXPIRE_TTL;
		private int maxSize = DEFAULT_MAX_SIZE;
		private Clock clock = Clock.systemDefaultZone();

		public LruTtlCacheBuilder<K,V> withExpireTtl(Duration expireTtl){
			this.expireTtl = expireTtl;
			return this;
		}

		public LruTtlCacheBuilder<K,V> withMaxSize(int maxSize){
			this.maxSize = maxSize;
			return this;
		}

		LruTtlCacheBuilder<K,V> withClock(Clock clock){
			this.clock = clock;
			return this;
		}

		public LruTtlCache<K,V> build(){
			return new LruTtlCache<>(expireTtl, maxSize, clock);
		}

	}

	// only used for tests
	private LruTtlCache(Duration ttl, int maxSize, Clock clock){
		this.ttl = ttl;
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<>(maxSize, 0.75F, true);
		this.clock = clock;
	}

	public synchronized V get(K key){
		return getIfNotExpired(key);
	}

	public synchronized boolean put(K key, V value){
		if(map.get(key) != null){
			map.put(key, new CachedObject<>(value, clock, ttl));
			return true;
		}
		if(map.size() < maxSize){
			map.put(key, new CachedObject<>(value, clock, ttl));
			return false;
		}
		// remove the oldest (first) item to make space
		Iterator<K> it = map.keySet().iterator();
		if(it.hasNext()){
			it.next();
			it.remove();
			map.put(key, new CachedObject<>(value, clock, ttl));
		}
		return false;
	}

	public synchronized boolean contains(K key){
		return getIfNotExpired(key) != null;
	}

	public synchronized void invalidate(){
		map.clear();
	}

	public synchronized int currentSize(){
		return map.size();
	}

	private synchronized V getIfNotExpired(K key){
		CachedObject<V> object = map.get(key);
		if(object == null){
			return null;
		}
		if(object.isExpired(clock)){
			map.remove(key);
			return null;
		}
		return object.value;
	}

	// only used for tests
	protected void updateClock(Clock clock){
		this.clock = clock;
	}

}
