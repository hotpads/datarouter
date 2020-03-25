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
package io.datarouter.metric.counter.collection.archive;

import java.time.Duration;

public enum CountPartitions{
	PERIOD_5s(
			Duration.ofSeconds(5),
			Duration.ofSeconds(5),
			Duration.ofDays(7)),
	PERIOD_20s(
			Duration.ofSeconds(20),
			Duration.ofSeconds(20),
			Duration.ofDays(31)),
	PERIOD_1m(
			Duration.ofMinutes(1),
			Duration.ofMinutes(1),
			Duration.ofDays(93)),
	PERIOD_5m(
			Duration.ofMinutes(5),
			Duration.ofMinutes(5),
			Duration.ofDays(365)),
	PERIOD_20m(
			Duration.ofMinutes(20),
			Duration.ofMinutes(10),
			Duration.ofDays(365 * 5)),
	PERIOD_1h(
			Duration.ofHours(1),
			Duration.ofMinutes(30),
			Duration.ofSeconds(Integer.MAX_VALUE)),
	PERIOD_4h(
			Duration.ofHours(4),
			Duration.ofHours(1),
			Duration.ofSeconds(Integer.MAX_VALUE)),
	PERIOD_1d(
			Duration.ofDays(1),
			Duration.ofHours(1),
			Duration.ofSeconds(Integer.MAX_VALUE));

	private final Duration period;
	private final Duration flushPeriod;
	@SuppressWarnings("unused")
	private final Duration ttl; // suggested TTL

	CountPartitions(Duration period, Duration flushPeriod, Duration ttl){
		this.period = period;
		this.flushPeriod = flushPeriod;
		this.ttl = ttl;
	}

	public long getPeriodMs(){
		return period.toMillis();
	}

	public long getFlushPeriodMs(){
		return flushPeriod.toMillis();
	}

}
