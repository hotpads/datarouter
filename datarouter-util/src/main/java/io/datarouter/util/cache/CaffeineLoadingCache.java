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

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.datarouter.util.lang.LineOfCode;

// Loading cache using caffeine cache
// https://github.com/ben-manes/caffeine
public class CaffeineLoadingCache<K,V>{

	private final com.github.benmanes.caffeine.cache.LoadingCache<K,V> cache;
	private final Function<K,V> loadingFunction;
	private final Function<K,RuntimeException> exceptionFunction;
	public final String name;

	private CaffeineLoadingCache(
			Duration expireTtl,
			int maxSize,
			Function<K,V> loadingFunction,
			Function<K,RuntimeException> exceptionFunction,
			String name,
			boolean recordStats){
		this.loadingFunction = loadingFunction;
		this.exceptionFunction = exceptionFunction;
		this.name = name;
		Caffeine<Object, Object> builder = Caffeine.newBuilder()
				.maximumSize(maxSize)
				.expireAfterWrite(expireTtl);
		if(recordStats){
			builder.recordStats();
		}
		this.cache = builder.build(loadingFunction::apply);
	}

	public static class CaffeineLoadingCacheBuilder<K,V>{

		private static final Duration DEFAULT_EXPIRE_TTL = Duration.ofSeconds(30);
		private static final int DEFAULT_MAX_SIZE = 10_000;

		private Duration expireTtl = DEFAULT_EXPIRE_TTL;
		private int maxSize = DEFAULT_MAX_SIZE;
		private Function<K,V> loadingFunction;
		private Function<K,RuntimeException> exceptionFunction = key ->
				new RuntimeException("loadingFunction=" + loadingFunction + " returned empty for key=" + key);
		private String name = new LineOfCode(1).getClassName();
		private boolean recordStats = false;

		public CaffeineLoadingCacheBuilder<K,V> withExpireTtl(Duration expireTtl){
			this.expireTtl = expireTtl;
			return this;
		}

		public CaffeineLoadingCacheBuilder<K,V> withMaxSize(int maxSize){
			this.maxSize = maxSize;
			return this;
		}

		public CaffeineLoadingCacheBuilder<K,V> withLoadingFunction(Function<K,V> loadingFunction){
			this.loadingFunction = loadingFunction;
			return this;
		}

		public CaffeineLoadingCacheBuilder<K,V> withExceptionFunction(Function<K,RuntimeException> exceptionFunction){
			this.exceptionFunction = exceptionFunction;
			return this;
		}

		public CaffeineLoadingCacheBuilder<K,V> withName(String name){
			this.name = name;
			return this;
		}

		public CaffeineLoadingCacheBuilder<K, V> withStatsRecording(){
			this.recordStats = true;
			return this;
		}

		public CaffeineLoadingCache<K,V> build(){
			return new CaffeineLoadingCache<>(
					expireTtl, maxSize, loadingFunction, exceptionFunction, name, recordStats);
		}

	}

	public Optional<V> get(K key){
		return Optional.ofNullable(cache.get(key));
	}

	public V getOrThrow(K key){
		return get(key).orElseThrow(() -> exceptionFunction.apply(key));
	}

	/**
	 * The value is stored in the cache using the lookup function with the key.
	 * Null values can never be stored.
	 *
	 * @param key the key of the object to be stored
	 * @return whether the value exists in the cache before inserting
	 */
	public boolean load(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		boolean wasPresent = cache.getIfPresent(key) != null;
		cache.put(key, loadingFunction.apply(key));
		return wasPresent;
	}

	/**
	 * contains has no impact on the ttl
	 *
	 * @param key the key of the object to be checked
	 * @return whether the key exists in the cache
	 */
	public boolean contains(K key){
		return cache.getIfPresent(key) != null;
	}

	public void invalidate(){
		cache.invalidateAll();
	}

}
