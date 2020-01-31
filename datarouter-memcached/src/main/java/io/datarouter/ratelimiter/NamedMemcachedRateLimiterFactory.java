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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import io.datarouter.client.memcached.ratelimiter.BaseTallyDao;
import io.datarouter.client.memcached.ratelimiter.TallyNodeFactory;
import io.datarouter.client.memcached.test.DatarouterMemcachedTestClientIds;
import io.datarouter.inject.testng.TestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.guice.DatarouterStorageTestGuiceModule;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.util.Require;
import io.datarouter.web.config.DatarouterWebGuiceModule;
import io.datarouter.web.config.DatarouterWebTestGuiceModule;

@Singleton
public class NamedMemcachedRateLimiterFactory{

	public class NamedMemcachedRateLimiterBuilder{

		private final String name;
		private Long maxAvgRequests;
		private Long maxSpikeRequests;
		private Integer numIntervals;
		private Integer bucketTimeInterval;
		private TimeUnit unit;

		public NamedMemcachedRateLimiterBuilder(String name){
			this.name = name;
		}

		public NamedMemcachedRateLimiterBuilder maxAvgRequests(long maxAvgRequests){
			this.maxAvgRequests = maxAvgRequests;
			return this;
		}

		public NamedMemcachedRateLimiterBuilder maxSpikeRequests(long maxSpikeRequests){
			this.maxSpikeRequests = maxSpikeRequests;
			return this;
		}

		public NamedMemcachedRateLimiterBuilder numIntervals(int numIntervals){
			this.numIntervals = numIntervals;
			return this;
		}

		public NamedMemcachedRateLimiterBuilder bucketTimeInterval(int bucketTimeInterval, TimeUnit unit){
			this.bucketTimeInterval = bucketTimeInterval;
			this.unit = unit;
			return this;
		}

		public NamedMemcachedRateLimiter build(){
			List<Object> arguments = Arrays.asList(name, maxAvgRequests, maxSpikeRequests, numIntervals,
					bucketTimeInterval, unit);
			Require.isFalse(arguments.contains(null));
			return new NamedMemcachedRateLimiter(name, maxAvgRequests, maxSpikeRequests, numIntervals,
					bucketTimeInterval, unit);
		}

	}

	@Inject
	private BaseTallyDao tallyDao;

	public class NamedMemcachedRateLimiter extends BaseNamedMemcachedRateLimiter{

		public NamedMemcachedRateLimiter(String name, long maxAvgRequests, long maxSpikeRequests, int numIntervals,
				int bucketTimeInterval, TimeUnit unit){
			super(name, maxAvgRequests, maxSpikeRequests, numIntervals, bucketTimeInterval, unit);
		}

		// null returned indicated that memcached failed the operation
		@Override
		protected Long increment(String key){
			return tallyDao.incrementAndGetCount(key, 1, expiration, Duration.ofMillis(200));
		}

		@Override
		protected Map<String,Long> readCounts(List<String> keys){
			return tallyDao.getMultiTallyCount(keys, expiration, Duration.ofMillis(200));
		}

	}

	/* tests ****************************************************************/

	@Guice(moduleFactory = NamedMemcachedRateLimiterTestNgModuleFactory.class)
	public static class NamedMemcachedRateLimiterIntegrationTests{

		private static final long maxAverageRequests = 10;
		private static final long maxSpikeRequests = 30;
		private static final int numIntervals = 7;
		private static final int bucketTimeInterval = 5;
		private static final TimeUnit unit = TimeUnit.SECONDS;
		private static final String rateLimiterName = NamedMemcachedRateLimiterIntegrationTests.class.getSimpleName();

		@Inject
		private NamedMemcachedRateLimiterFactory namedMemcachedRateLimiterFactory;

		private NamedMemcachedRateLimiter makeNamedMemcachedRateLimiter(){
			return namedMemcachedRateLimiterFactory.new NamedMemcachedRateLimiter(rateLimiterName, maxAverageRequests,
					maxSpikeRequests, bucketTimeInterval, numIntervals, unit);
		}

		@Test
		public void testIncr(){
			NamedMemcachedRateLimiter rateLimiter = makeNamedMemcachedRateLimiter();
			String testKey1 = "one" + System.currentTimeMillis();
			String testKey2 = "two" + System.currentTimeMillis();

			for(Long i = 1L; i < 10; i++){
				Assert.assertEquals(rateLimiter.increment(testKey1), i, "i=" + i);
			}

			Assert.assertEquals(rateLimiter.increment(testKey2), Long.valueOf(1));
		}

		@Test
		public void testRateLimit(){
			NamedMemcachedRateLimiter rateLimiter = makeNamedMemcachedRateLimiter();
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

	public static class DatarouterRateLimiterTestNgModuleFactory extends TestNgModuleFactory{

		public DatarouterRateLimiterTestNgModuleFactory(){
			super(Arrays.asList(
					new DatarouterWebTestGuiceModule(),
					new DatarouterStorageTestGuiceModule(),
					new DatarouterWebGuiceModule()));
		}

	}

	public static class NamedMemcachedRateLimiterTestNgModuleFactory extends DatarouterRateLimiterTestNgModuleFactory{

		@Override
		protected List<Module> getOverriders(){
			Module module = new AbstractModule(){
				@Override
				protected void configure(){
					bind(BaseTallyDao.class).to(DatarouterNamedMemcachedRateLimiterTestDao.class);
				}
			};
			List<Module> overrides = super.getOverriders();
			overrides.add(module);
			return overrides;
		}

	}

	@Singleton
	private static class DatarouterNamedMemcachedRateLimiterTestDao extends BaseTallyDao implements TestDao{

		@Inject
		public DatarouterNamedMemcachedRateLimiterTestDao(Datarouter datarouter, TallyNodeFactory nodeFactory){
			super(datarouter, nodeFactory, DatarouterMemcachedTestClientIds.MEMCACHED, 1);
		}

	}

}