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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DatarouterRateLimiterConfig{

	public final String name;
	public final Long maxAverageRequests;
	public final Long maxSpikeRequests;
	public final Integer numIntervals;
	public final Integer bucketTimeInterval;
	public final int bucketIntervalMs;
	public final TimeUnit unit;
	public final Duration expiration;

	/**
	 * @param name name of the ratelimiter
	 * @param maxAverageRequests threshold average number of requests
	 * @param maxSpikeRequests threshold max number of requests
	 * @param numIntervals number of buckets
	 * @param bucketTimeInterval length of each bucket
	 * @param unit time unit of bucketTimeInterval
	 */
	public DatarouterRateLimiterConfig(String name, Long maxAverageRequests, Long maxSpikeRequests,
			Integer numIntervals, Integer bucketTimeInterval, TimeUnit unit){
		this.name = name;
		this.maxAverageRequests = maxAverageRequests;
		this.maxSpikeRequests = maxSpikeRequests;
		this.numIntervals = numIntervals;
		this.bucketTimeInterval = bucketTimeInterval;
		this.bucketIntervalMs = Math.toIntExact(unit.toMillis(bucketTimeInterval));
		this.unit = unit;
		this.expiration = Duration.ofMillis(bucketIntervalMs * (numIntervals + 1));
	}

	public static class DatarouterRateLimiterConfigBuilder{

		public final String name;

		public Long maxAverageRequests;
		public Long maxSpikeRequests;
		public Integer numIntervals;
		public Integer bucketTimeInterval;
		public TimeUnit unit;

		public DatarouterRateLimiterConfigBuilder(String name){
			this.name = name;
		}

		public DatarouterRateLimiterConfigBuilder setMaxAverageRequests(long maxAverageRequests){
			this.maxAverageRequests = maxAverageRequests;
			return this;
		}

		public DatarouterRateLimiterConfigBuilder setMaxSpikeRequests(long maxSpikeRequests){
			this.maxSpikeRequests = maxSpikeRequests;
			return this;
		}

		public DatarouterRateLimiterConfigBuilder setNumIntervals(int numIntervals){
			this.numIntervals = numIntervals;
			return this;
		}

		public DatarouterRateLimiterConfigBuilder setBucketTimeInterval(int bucketTimeInterval, TimeUnit unit){
			this.bucketTimeInterval = bucketTimeInterval;
			this.unit = unit;
			return this;
		}

		public DatarouterRateLimiterConfig build(){
			return new DatarouterRateLimiterConfig(name, maxAverageRequests, maxSpikeRequests, numIntervals,
					bucketTimeInterval, unit);
		}

	}

}
