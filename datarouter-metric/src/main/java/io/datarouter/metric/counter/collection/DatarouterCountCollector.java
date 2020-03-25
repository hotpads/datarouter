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
package io.datarouter.metric.counter.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.AtomicCounter;
import io.datarouter.instrumentation.count.CountCollector;
import io.datarouter.instrumentation.count.CountCollectorPeriod;
import io.datarouter.metric.counter.collection.archive.CountArchiveFlusher;

public class DatarouterCountCollector implements CountCollector{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterCountCollector.class);

	private final long rollPeriodMs;
	private final List<CountArchiveFlusher> flushers;
	private long latestStartMs;
	private long nextStartMs;

	private CountCollectorPeriod liveCounter;

	public DatarouterCountCollector(long rollPeriodMs){
		this.rollPeriodMs = rollPeriodMs;
		long now = System.currentTimeMillis();
		long startTime = now - now % rollPeriodMs;
		this.liveCounter = new AtomicCounter(startTime, rollPeriodMs);
		this.flushers = new ArrayList<>();
		this.checkAndRoll();
		logger.warn("created " + this);
	}

	public void addFlusher(CountArchiveFlusher flusher){
		flushers.add(flusher);
	}

	// called on every increment right now. currentTimeMillis is supposedly as cheap as a memory access
	private void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			checkAndRoll();
		}
	}

	private synchronized void checkAndRoll(){
		// a few threads may slip past the rollIfNecessary call and pile up here

		long now = System.currentTimeMillis();
		long nowPeriodStart = now - now % rollPeriodMs;

		if(liveCounter != null && nowPeriodStart == liveCounter.getStartTimeMs()){
			return; // another thread already rolled it
		}
		latestStartMs = nowPeriodStart;
		nextStartMs = latestStartMs + rollPeriodMs;// now other threads should return rollIfNecessary=false
		// only one thread (per period) should get to this point because of the logical check above
		// protect against multiple periods overlapping? we may get count skew here if things get backed up
		// swap in the new counter
		CountCollectorPeriod oldCounter = liveCounter;
		liveCounter = new AtomicCounter(latestStartMs, rollPeriodMs);
		// add previous counter to flush queue
		flushers.forEach(fluster -> fluster.offer(oldCounter));
	}

	@Override
	public void stopAndFlushAll(){
		for(CountArchiveFlusher flusher : flushers){
			flusher.shutdownAndFlushAll();
		}
	}

	@Override
	public long increment(String key){
		rollIfNecessary();
		return liveCounter.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		rollIfNecessary();
		return liveCounter.increment(key, delta);
	}

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return liveCounter.getCountByKey();
	}

	@Override
	public AtomicCounter getCounter(){
		return liveCounter.getCounter();
	}

}
