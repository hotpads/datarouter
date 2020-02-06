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
import java.util.Map;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.cache.LoadingCache.LoadingCacheBuilder;
import io.datarouter.util.collection.MapTool;
import io.datarouter.util.tuple.Pair;

public class LoadingCacheTests{

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