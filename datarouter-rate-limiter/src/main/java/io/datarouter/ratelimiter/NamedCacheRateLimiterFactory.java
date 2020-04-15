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

import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.util.Require;

@Singleton
public class NamedCacheRateLimiterFactory{

	public class NamedCacheRateLimiterBuilder{

		private final String name;
		private Long maxAvgRequests;
		private Long maxSpikeRequests;
		private Integer numIntervals;
		private Integer bucketTimeInterval;
		private TimeUnit unit;

		public NamedCacheRateLimiterBuilder(String name){
			this.name = name;
		}

		public NamedCacheRateLimiterBuilder maxAvgRequests(long maxAvgRequests){
			this.maxAvgRequests = maxAvgRequests;
			return this;
		}

		public NamedCacheRateLimiterBuilder maxSpikeRequests(long maxSpikeRequests){
			this.maxSpikeRequests = maxSpikeRequests;
			return this;
		}

		public NamedCacheRateLimiterBuilder numIntervals(int numIntervals){
			this.numIntervals = numIntervals;
			return this;
		}

		public NamedCacheRateLimiterBuilder bucketTimeInterval(int bucketTimeInterval, TimeUnit unit){
			this.bucketTimeInterval = bucketTimeInterval;
			this.unit = unit;
			return this;
		}

		public NamedCacheRateLimiter build(){
			List<Object> arguments = Arrays.asList(name, maxAvgRequests, maxSpikeRequests, numIntervals,
					bucketTimeInterval, unit);
			Require.isFalse(arguments.contains(null));
			return new NamedCacheRateLimiter(name, maxAvgRequests, maxSpikeRequests, numIntervals, bucketTimeInterval,
					unit);
		}

	}

	@Inject
	private BaseTallyDao tallyDao;

	public class NamedCacheRateLimiter extends BaseNamedCacheRateLimiter{

		public NamedCacheRateLimiter(String name, long maxAvgRequests, long maxSpikeRequests, int numIntervals,
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

}
