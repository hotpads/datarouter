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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.datarouter.util.singletonsupplier.SingletonSupplier;

public class RateLimiterConfig{

	public final String name;
	public final long avg;
	public final long spike;
	public final int periods;
	public final int bucketPeriod;
	public final TimeUnit unit;

	private Supplier<NamedRateLimiter> nameRateLimiter;

	public RateLimiterConfig(String name, long avg, long spike, int periods, int bucketPeriod, TimeUnit unit){
		this.name = name;
		this.avg = avg;
		this.spike = spike;
		this.periods = periods;
		this.bucketPeriod = bucketPeriod;
		this.unit = unit;
	}

	@Override
	public String toString(){
		return Arrays.asList(name, avg, spike, periods, bucketPeriod, unit).stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}

	public Supplier<NamedRateLimiter> getNameRateLimiter(){
		return nameRateLimiter;
	}

	public void initNameRateLimiter(NamedCacheRateLimiterFactory factory){
		this.nameRateLimiter = SingletonSupplier.of(() -> factory.new NamedCacheRateLimiter(name, avg, spike, periods,
				bucketPeriod, unit));
	}

}
