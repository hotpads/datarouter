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
package io.datarouter.ratelimiter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.ratelimiter.CacheRateLimiterConfig.CacheRateLimiterConfigBuilder;
import io.datarouter.ratelimiter.storage.BaseTallyDao;

@Guice(moduleFactory = RateLimiterTestNgModuleFactory.class)
public class TallyCacheRateLimiterIntegrationTests{

	private static final long MAX_SPIKE_REQUESTS = 30;

	@Singleton
	public static class ExampleRateLimiter extends BaseTallyCacheRateLimiter{

		@Inject
		public ExampleRateLimiter(BaseTallyDao tallyDao){
			super(tallyDao, new CacheRateLimiterConfigBuilder("Example")
					.setMaxAverageRequests(10)
					.setMaxSpikeRequests(MAX_SPIKE_REQUESTS)
					.setNumIntervals(7)
					.setBucketTimeInterval(5, TimeUnit.SECONDS)
					.build());
		}

	}

	@Inject
	private ExampleRateLimiter rateLimiter;

	@Test
	public void testIncr(){
		String testKey1 = "one" + System.currentTimeMillis();
		String testKey2 = "two" + System.currentTimeMillis();

		for(Long i = 1L; i < 10; i++){
			Assert.assertEquals(rateLimiter.increment(testKey1), i, "i=" + i);
		}

		Assert.assertEquals(rateLimiter.increment(testKey2), Long.valueOf(1));
	}

	@Test
	public void testRateLimit(){
		String testKey1 = "one" + System.currentTimeMillis();
		String testKey2 = "two" + System.currentTimeMillis();

		for(int i = 0; i < MAX_SPIKE_REQUESTS; i++){
			Assert.assertTrue(rateLimiter.allowed(testKey1), "i=" + i);
		}
		if(rateLimiter.allowed(testKey1)){
			for(int i = 0; i < MAX_SPIKE_REQUESTS; i++){
				rateLimiter.allowed(testKey1);
			}
			Assert.assertFalse(rateLimiter.allowed(testKey1));
		}
		Assert.assertTrue(rateLimiter.allowed(testKey2));
	}

}
