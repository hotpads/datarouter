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
package io.datarouter.plugin.dataexport.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import io.datarouter.util.number.NumberFormatter;

public class RateTracker{

	private static final long NANOS_PER_SEC = TimeUnit.SECONDS.toNanos(1);

	private final Instant startTime = Instant.now();
	private final LongAdder totalCount = new LongAdder();
	private final AtomicLong lastLogCount = new AtomicLong();
	private Instant lastLogTime = Instant.now();

	/*--------- input --------*/

	public void markLogged(){
		lastLogTime = Instant.now();
		lastLogCount.set(totalCount.sum());
	}

	public void increment(@SuppressWarnings("unused") Object unused){
		increment();
	}

	public void increment(){
		incrementBy(1);
	}

	public void incrementBy(long count){
		totalCount.add(count);
	}

	public void incrementBySize(Collection<?> collection){
		incrementBy(collection.size());
	}

	/*--------- output ---------*/

	public long totalCount(){
		return totalCount.sum();
	}

	public long perSec(){
		long lastCount = totalCount.sum() - lastLogCount.get();
		long nanos = Duration.between(lastLogTime, Instant.now()).toNanos();
		if(lastCount == 0 || nanos == 0){
			return 0;
		}
		return NANOS_PER_SEC * lastCount / nanos;
	}

	public long perSecAvg(){
		long nanos = Duration.between(startTime, Instant.now()).toNanos();
		if(totalCount.sum() == 0 || nanos == 0){
			return 0;
		}
		return NANOS_PER_SEC * totalCount.sum() / nanos;
	}

	public String totalCountDisplay(){
		return NumberFormatter.addCommas(totalCount.sum());
	}

	public String perSecDisplay(){
		return NumberFormatter.addCommas(perSec());
	}

	public String perSecAvgDisplay(){
		return NumberFormatter.addCommas(perSecAvg());
	}

}