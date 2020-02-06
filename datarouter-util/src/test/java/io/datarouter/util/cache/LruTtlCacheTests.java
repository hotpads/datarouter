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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.cache.LruTtlCache.LruTtlCacheBuilder;

public class LruTtlCacheTests{

	@Test
	public void testTtl(){
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		LruTtlCache<String,String> cache = new LruTtlCacheBuilder<String,String>()
				.withMaxSize(5)
				.withExpireTtl(Duration.ofDays(1))
				.withClock(clock)
				.build();

		Assert.assertFalse(cache.contains("key0"));
		cache.put("key1", "value1");
		Assert.assertTrue(cache.contains("key1"));
		Assert.assertEquals(cache.get("key1"), "value1");

		clock = Clock.offset(clock, Duration.ofDays(2));
		cache.updateClock(clock);

		Assert.assertNull(cache.get("a"));
		Assert.assertNull(cache.get("b"));
		Assert.assertNull(cache.get("key1"));
		Assert.assertFalse(cache.contains("key1"));
	}

	@Test
	public void testMaxCapcaity(){
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		LruTtlCache<String,String> cache = new LruTtlCacheBuilder<String,String>()
				.withMaxSize(3)
				.withExpireTtl(Duration.ofHours(10))
				.withClock(clock)
				.build();

		cache.put("key1", "value1");
		Assert.assertTrue(cache.contains("key1"));
		cache.put("key2", "value2");
		Assert.assertTrue(cache.contains("key2"));
		cache.put("key3", "value3");
		Assert.assertTrue(cache.contains("key3"));
		cache.put("key4", "value4");

		Assert.assertFalse(cache.contains("key1"));
		Assert.assertTrue(cache.contains("key4"));

		cache.put("key5", "value5");
		Assert.assertFalse(cache.contains("key2"));
		Assert.assertTrue(cache.contains("key5"));

		clock = Clock.offset(clock, Duration.ofMinutes(10));
		cache.updateClock(clock);

		Assert.assertNotNull(cache.get("key5"));

		clock = Clock.offset(clock, Duration.ofHours(10));
		cache.updateClock(clock);

		Assert.assertNull(cache.get("key1"));
		Assert.assertNull(cache.get("key2"));
		Assert.assertNull(cache.get("key3"));
	}

	@Test
	public void testPut(){
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		LruTtlCache<String,String> cache = new LruTtlCacheBuilder<String,String>()
				.withMaxSize(5)
				.withExpireTtl(Duration.ofHours(1))
				.withClock(clock)
				.build();

		cache.put("key1", "value1");
		Assert.assertNotNull(cache.get("key1"));
		clock = Clock.offset(clock, Duration.ofHours(1));
		cache.updateClock(clock);

		cache.put("key2", "value2");
		Assert.assertTrue(cache.contains("key2"));
		cache.put("key3", "value3");
		Assert.assertTrue(cache.contains("key3"));
		cache.put("key4", "value4");
		Assert.assertTrue(cache.contains("key4"));
		cache.put("key5", "value5");
		Assert.assertTrue(cache.contains("key5"));

		clock = Clock.offset(clock, Duration.ofHours(1));
		cache.updateClock(clock);

		cache.put("key6", "value6");
		Assert.assertTrue(cache.contains("key6"));
		Assert.assertNull(cache.get("key1"));
		clock = Clock.offset(clock, Duration.ofHours(10));
		cache.updateClock(clock);
		Assert.assertNull(cache.get("key2"));
		Assert.assertNull(cache.get("key3"));
		Assert.assertNull(cache.get("key4"));
		Assert.assertNull(cache.get("key5"));
		Assert.assertNull(cache.get("key6"));
	}

}