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
package io.datarouter.util.cache;

import java.lang.StackWalker.Option;
import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.instrumentation.trace.TracerTool;

//LRU TTL Loading Cache
public class LoadingCache<K,V>{

	private static final StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

	private final Map<K,CachedObject<V>> map;
	private final Duration expireTtl;
	private final int maxSize;
	private final Function<K,V> loadingFunction;
	private final Function<K,RuntimeException> exceptionFunction;
	private final String name;

	private Clock clock; // not final for tests

	private LoadingCache(Duration expireTtl, int maxSize, Clock clock, Function<K,V> loadingFunction,
			Function<K,RuntimeException> exceptionFunction, String name){
		this.expireTtl = expireTtl;
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<>(maxSize, 0.75F, true);
		this.clock = clock;
		this.loadingFunction = loadingFunction;
		this.exceptionFunction = exceptionFunction;
		this.name = name;
	}

	public static class LoadingCacheBuilder<K,V>{

		private static final Duration DEFAULT_EXPIRE_TTL = Duration.ofSeconds(30);
		private static final int DEFAULT_MAX_SIZE = 10_000;

		private Duration expireTtl = DEFAULT_EXPIRE_TTL;
		private int maxSize = DEFAULT_MAX_SIZE;
		private Clock clock = Clock.systemDefaultZone();
		private Function<K,V> loadingFunction;
		private Function<K,RuntimeException> exceptionFunction = K -> new RuntimeException("Failed to lookup " + K);
		private String name = walker.getCallerClass().getSimpleName();

		public LoadingCacheBuilder<K,V> withExpireTtl(Duration expireTtl){
			this.expireTtl = expireTtl;
			return this;
		}

		public LoadingCacheBuilder<K,V> withMaxSize(int maxSize){
			this.maxSize = maxSize;
			return this;
		}

		public LoadingCacheBuilder<K,V> withLoadingFunction(Function<K,V> loadingFunction){
			this.loadingFunction = loadingFunction;
			return this;
		}

		public LoadingCacheBuilder<K,V> withExceptionFunction(Function<K,RuntimeException> exceptionFunction){
			this.exceptionFunction = exceptionFunction;
			return this;
		}

		public LoadingCacheBuilder<K,V> withName(String name){
			this.name = name;
			return this;
		}

		LoadingCacheBuilder<K,V> withClock(Clock clock){
			this.clock = clock;
			return this;
		}

		public LoadingCache<K,V> build(){
			return new LoadingCache<>(expireTtl, maxSize, clock, loadingFunction, exceptionFunction, name);
		}

	}

	public Optional<V> get(K key){
		try(var $ = TracerTool.startSpan(name + " get")){
			TracerTool.appendToSpanInfo(key.toString());
			return getSynchronized(key);
		}
	}

	private synchronized Optional<V> getSynchronized(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return getInternal(key);
	}

	public V getOrThrows(K key){
		try(var $ = TracerTool.startSpan(name + " getOrThrows")){
			TracerTool.appendToSpanInfo(key.toString());
			return getOrThrowsSynchronized(key);
		}
	}

	private synchronized V getOrThrowsSynchronized(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return getInternal(key)
				.orElseThrow(() -> exceptionFunction.apply(key));
	}

	/**
	 * The value is stored in the cache using the lookup function with the key.
	 * Null values can never be stored.
	 *
	 * @param key the key of the object to be stored
	 * @return whether the value exists in the cache before inserting
	 */
	public boolean load(K key){
		try(var $ = TracerTool.startSpan(name + " load")){
			TracerTool.appendToSpanInfo(key.toString());
			return loadSynchronized(key);
		}
	}

	private synchronized boolean loadSynchronized(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return put(key, loadingFunction.apply(key));
	}

	/**
	 * contains has no impact on the ttl
	 *
	 * @param key the key of the object to be checked
	 * @return whether the key exists in the cache
	 */
	public boolean contains(K key){
		try(var $ = TracerTool.startSpan(name + " contains")){
			TracerTool.appendToSpanInfo(key.toString());
			return containsSynchronized(key);
		}
	}

	private synchronized boolean containsSynchronized(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return getIfNotExpired(key) != null;
	}

	private boolean put(K key, V value){
		try(var $ = TracerTool.startSpan(name + " put")){
			TracerTool.appendToSpanInfo(key.toString());
			return putSynchronized(key, value);
		}
	}

	private synchronized boolean putSynchronized(K key, V value){
		if(value == null){
			// don't cache null values
			return false;
		}
		if(map.get(key) != null){
			// already exists, so only update the expire ttl
			map.put(key, new CachedObject<>(value, clock, expireTtl));
			return true;
		}
		if(map.size() < maxSize){
			map.put(key, new CachedObject<>(value, clock, expireTtl));
			return false;
		}
		// remove the oldest (first) item to make space
		Iterator<K> it = map.keySet().iterator();
		if(it.hasNext()){
			it.next();
			it.remove();
			map.put(key, new CachedObject<>(value, clock, expireTtl));
		}
		return false;
	}

	private Optional<V> getInternal(K key){
		CachedObject<V> object = getIfNotExpired(key);
		if(object != null){
			return Optional.of(object.value);
		}
		load(key);
		object = getIfNotExpired(key);
		if(object == null){
			return Optional.empty();
		}
		return Optional.of(object.value);
	}

	private CachedObject<V> getIfNotExpired(K key){
		try(var $ = TracerTool.startSpan(name + " getIfNotExpired")){
			TracerTool.appendToSpanInfo(key.toString());
			return getIfNotExpiredSynchronized(key);
		}
	}

	private synchronized CachedObject<V> getIfNotExpiredSynchronized(K key){
		CachedObject<V> object = map.get(key);
		if(object == null){
			return null;
		}
		if(object.isExpired(clock)){
			map.remove(key);
			return null;
		}
		return object;
	}

	// only used for tests
	void updateClock(Clock clock){
		this.clock = clock;
	}

}