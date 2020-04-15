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
package io.datarouter.ratelimiter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.ratelimiter.NamedCacheRateLimiterFactory.NamedCacheRateLimiter;

@Guice(moduleFactory = RateLimiterTestNgModuleFactory.class)
public class NamedCacheRateLimiterIntegrationTests{

	private static final long maxAverageRequests = 10;
	private static final long maxSpikeRequests = 30;
	private static final int numIntervals = 7;
	private static final int bucketTimeInterval = 5;
	private static final TimeUnit unit = TimeUnit.SECONDS;
	private static final String rateLimiterName = NamedCacheRateLimiterIntegrationTests.class.getSimpleName();

	@Inject
	private NamedCacheRateLimiterFactory namedMemcachedRateLimiterFactory;

	private NamedCacheRateLimiter makeNamedMemcachedRateLimiter(){
		return namedMemcachedRateLimiterFactory.new NamedCacheRateLimiter(
				rateLimiterName,
				maxAverageRequests,
				maxSpikeRequests,
				bucketTimeInterval,
				numIntervals,
				unit);
	}

	@Test
	public void testIncr(){
		NamedCacheRateLimiter rateLimiter = makeNamedMemcachedRateLimiter();
		String testKey1 = "one" + System.currentTimeMillis();
		String testKey2 = "two" + System.currentTimeMillis();

		for(Long i = 1L; i < 10; i++){
			Assert.assertEquals(rateLimiter.increment(testKey1), i, "i=" + i);
		}

		Assert.assertEquals(rateLimiter.increment(testKey2), Long.valueOf(1));
	}

	@Test
	public void testRateLimit(){
		NamedCacheRateLimiter rateLimiter = makeNamedMemcachedRateLimiter();
		String testKey1 = "one" + System.currentTimeMillis();
		String testKey2 = "two" + System.currentTimeMillis();

		for(int i = 0; i < maxSpikeRequests; i++){
			Assert.assertTrue(rateLimiter.allowed(testKey1), "i=" + i);
		}
		if(rateLimiter.allowed(testKey1)){
			for(int i = 0; i < maxSpikeRequests; i++){
				rateLimiter.allowed(testKey1);
			}
			Assert.assertFalse(rateLimiter.allowed(testKey1));
		}
		Assert.assertTrue(rateLimiter.allowed(testKey2));
	}

}
