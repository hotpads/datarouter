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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.MapTool;
import io.datarouter.util.tuple.Pair;

//LRU TTL Loading Cache
public class LoadingCache<K,V>{

	private final Map<K,CachedObject<V>> map;
	private final Duration expireTtl;
	private final int maxSize;
	private final Function<K,V> loadingFunction;
	private final Function<K,RuntimeException> exceptionFunction;

	private Clock clock; // not final for tests

	private LoadingCache(Duration expireTtl, int maxSize, Clock clock, Function<K,V> loadingFunction,
			Function<K,RuntimeException> exceptionFunction){
		this.expireTtl = expireTtl;
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<>(maxSize, 0.75F, true);
		this.clock = clock;
		this.loadingFunction = loadingFunction;
		this.exceptionFunction = exceptionFunction;
	}

	public static class LoadingCacheBuilder<K,V>{

		private static final Duration DEFAULT_EXPIRE_TTL = Duration.ofSeconds(30);
		private static final int DEFAULT_MAX_SIZE = 10_000;

		private Duration expireTtl = DEFAULT_EXPIRE_TTL;
		private int maxSize = DEFAULT_MAX_SIZE;
		private Clock clock = Clock.systemDefaultZone();
		private Function<K,V> loadingFunction;
		private Function<K,RuntimeException> exceptionFunction = K -> new RuntimeException("Failed to lookup " + K);

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

		private LoadingCacheBuilder<K,V> withClock(Clock clock){
			this.clock = clock;
			return this;
		}

		public LoadingCache<K,V> build(){
			return new LoadingCache<>(expireTtl, maxSize, clock, loadingFunction, exceptionFunction);
		}

	}

	public synchronized Optional<V> get(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return getInternal(key);
	}

	public synchronized V getOrThrows(K key){
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
	public synchronized boolean load(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return put(key, loadingFunction.apply(key));
	}

	/**
	 * contains has no impact on the ttl
	 *
	 * @param key the key of the object to be checked
	 * @return whether the key exists in the cache
	 */
	public synchronized boolean contains(K key){
		Objects.requireNonNull(key, "Key may not be null in LoadingCache");
		return getIfNotExpired(key) != null;
	}

	private synchronized boolean put(K key, V value){
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

	private synchronized CachedObject<V> getIfNotExpired(K key){
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
	private void updateClock(Clock clock){
		this.clock = clock;
	}

	public static class LoadingCacheTests{

		// not final, since values are updated in tests
		private static final Map<String,String> ORIGINAL_DATA_STORE = MapTool.of(
				new Pair<>("key1", "value1"),
				new Pair<>("key2", "value2"),
				new Pair<>("key3", "value3"),
				new Pair<>("key4", "value4"),
				new Pair<>("key5", "value5"),
				new Pair<>("key6", "value6"));

		private static Map<String,String> DATA_STORE = ORIGINAL_DATA_STORE;

		private static Function<String,String> LOADING_FUNCTION = DATA_STORE::get;

		@Test
		public void testTtl(){
			DATA_STORE = ORIGINAL_DATA_STORE;
			Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
			LoadingCache<String,String> cache = new LoadingCacheBuilder<String,String>()
					.withExpireTtl(Duration.ofDays(1))
					.withMaxSize(5)
					.withClock(clock)
					.withLoadingFunction(LOADING_FUNCTION)
					.build();

			Assert.assertFalse(cache.contains("key0"));
			Assert.assertFalse(cache.load("key1"));
			Assert.assertTrue(cache.contains("key1"));
			Assert.assertEquals(cache.get("key1").get(), DATA_STORE.get("key1"));

			clock = Clock.offset(clock, Duration.ofDays(2));
			cache.updateClock(clock);
			Assert.assertFalse(cache.get("a").isPresent());
			Assert.assertFalse(cache.get("b").isPresent());
			Assert.assertTrue(cache.get("key1").isPresent());

			clock = Clock.offset(clock, Duration.ofDays(3));
			cache.updateClock(clock);

			DATA_STORE.put("key1", "apricots");
			Assert.assertEquals(cache.get("key1").get(), "apricots");
		}

		@Test
		public void testMaxCapacity(){
			DATA_STORE = ORIGINAL_DATA_STORE;
			Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

			LoadingCache<String,String> cache = new LoadingCacheBuilder<String,String>()
					.withExpireTtl(Duration.ofHours(10))
					.withMaxSize(3)
					.withClock(clock)
					.withLoadingFunction(LOADING_FUNCTION)
					.build();

			cache.load("key1");
			Assert.assertTrue(cache.contains("key1"));
			cache.load("key2");
			Assert.assertTrue(cache.contains("key2"));
			cache.load("key3");
			Assert.assertTrue(cache.contains("key3"));
			cache.load("key4");

			Assert.assertFalse(cache.contains("key1"));
			Assert.assertTrue(cache.contains("key4"));

			cache.load("key5");
			Assert.assertFalse(cache.contains("key2"));
			Assert.assertTrue(cache.contains("key5"));

			clock = Clock.offset(clock, Duration.ofMinutes(10));
			cache.updateClock(clock);

			Assert.assertNotNull(cache.get("key5"));

			clock = Clock.offset(clock, Duration.ofHours(10));
			cache.updateClock(clock);

			Assert.assertFalse(cache.contains("key1"));
			Assert.assertFalse(cache.contains("key2"));
			Assert.assertFalse(cache.contains("key3"));
		}

		@Test
		public void testLoad(){
			DATA_STORE = ORIGINAL_DATA_STORE;
			Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

			LoadingCache<String,String> cache = new LoadingCacheBuilder<String,String>()
					.withExpireTtl(Duration.ofHours(10))
					.withMaxSize(5)
					.withClock(clock)
					.withLoadingFunction(LOADING_FUNCTION)
					.build();

			cache.load("key1");
			Assert.assertNotNull(cache.get("key1"));
			clock = Clock.offset(clock, Duration.ofHours(1));
			cache.updateClock(clock);

			Assert.assertFalse(cache.load("key2"));
			Assert.assertTrue(cache.load("key2"));
			Assert.assertFalse(cache.load("key3"));
			Assert.assertFalse(cache.load("key4"));
			Assert.assertFalse(cache.load("key5"));

			clock = Clock.offset(clock, Duration.ofHours(1));
			cache.updateClock(clock);

			Assert.assertTrue(cache.load("key5"));

			clock = Clock.offset(clock, Duration.ofHours(1));
			cache.updateClock(clock);

			Assert.assertTrue(cache.load("key5"));

			clock = Clock.offset(clock, Duration.ofHours(11));
			cache.updateClock(clock);

			Assert.assertFalse(cache.contains("key5"));
			Assert.assertFalse(cache.load("key5"));


			Assert.assertFalse(cache.load("key6"));
			Assert.assertTrue(cache.get("key1").isPresent());
			clock = Clock.offset(clock, Duration.ofHours(11));
			cache.updateClock(clock);
			Assert.assertFalse(cache.contains("key2"));
			Assert.assertFalse(cache.contains("key3"));
			Assert.assertFalse(cache.contains("key4"));
			Assert.assertFalse(cache.contains("key5"));
			Assert.assertFalse(cache.contains("key5"));
			Assert.assertTrue(cache.get("key2").isPresent());
			Assert.assertTrue(cache.get("key3").isPresent());
			Assert.assertTrue(cache.get("key4").isPresent());
			Assert.assertTrue(cache.get("key5").isPresent());
			Assert.assertTrue(cache.get("key6").isPresent());

			Assert.assertTrue(cache.load("key2"));
			DATA_STORE.put("key2", "orange");
			Assert.assertTrue(cache.load("key2"));
			Assert.assertEquals(cache.get("key2").get(), DATA_STORE.get("key2"), "new loaded value is orange");
		}

		@Test
		public void testGet(){
			DATA_STORE = ORIGINAL_DATA_STORE;
			Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

			LoadingCache<String,String> cache = new LoadingCacheBuilder<String,String>()
					.withExpireTtl(Duration.ofDays(5))
					.withMaxSize(5)
					.withClock(clock)
					.withLoadingFunction(LOADING_FUNCTION)
					.build();

			cache.load("key1");
			cache.load("key2");

			Assert.assertEquals(cache.get("key1").get(), DATA_STORE.get("key1"));
			Assert.assertEquals(cache.get("key2").get(), DATA_STORE.get("key2"));

			clock = Clock.offset(clock, Duration.ofHours(5));
			cache.updateClock(clock);

			Assert.assertEquals(cache.get("key1").get(), DATA_STORE.get("key1"));

			clock = Clock.offset(clock, Duration.ofDays(1));
			cache.updateClock(clock);

			Assert.assertEquals(cache.get("key1").get(), DATA_STORE.get("key1"));

			clock = Clock.offset(clock, Duration.ofDays(1));
			cache.updateClock(clock);

			DATA_STORE.put("key1", "apple");
			Assert.assertEquals(DATA_STORE.get("key1"), "apple");
			cache.load("key1");
			Assert.assertEquals(cache.get("key1").get(), DATA_STORE.get("key1"), "loaded value is now 'apple'");
		}

	}

}