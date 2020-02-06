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
package io.datarouter.util.buffer;

import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;

public class FlushingSetBuffer<T>{
	private static final Logger logger = LoggerFactory.getLogger(FlushingSetBuffer.class);

	static final Duration MAX_BUFFER_FLUSH_CHECK_INTERVAL = Duration.ofMillis(100);

	private final Set<T> set;
	private final String name;
	private final int maxSize;
	private final Duration maxBufferedDuration;
	private final Consumer<Set<T>> flushConsumer;
	private long timeOfBufferExpirationMs;

	Clock clock; // not final for tests

	private FlushingSetBuffer(String name, int maxSize, Duration maxBufferedDuration, Consumer<Set<T>> flushConsumer,
			Clock clock){
		this.set = new HashSet<>();
		this.name = name;
		this.maxSize = maxSize;
		this.maxBufferedDuration = maxBufferedDuration;
		this.flushConsumer = flushConsumer;
		this.clock = clock;
		this.timeOfBufferExpirationMs = Clock.offset(this.clock, maxBufferedDuration).millis();

		long flushCheckInterval = Math.min(MAX_BUFFER_FLUSH_CHECK_INTERVAL.toMillis(), maxBufferedDuration.toMillis());
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(this::flushIfBufferExpired, flushCheckInterval,
				flushCheckInterval, TimeUnit.MILLISECONDS);
	}

	public static class FlushingSetBufferBuilder<T>{

		private static final Duration DEFAULT_MAX_BUFFERED_DURATION = Duration.ofSeconds(30);
		private static final int DEFAULT_MAX_SIZE = 10_000;

		private String name = FlushingSetBuffer.class.getSimpleName();
		private Duration maxBufferedDuration = DEFAULT_MAX_BUFFERED_DURATION;
		private int maxSize = DEFAULT_MAX_SIZE;
		private Clock clock = Clock.systemDefaultZone();
		private Consumer<Set<T>> flushConsumer;

		public FlushingSetBufferBuilder<T> withName(String name){
			this.name = name;
			return this;
		}

		public FlushingSetBufferBuilder<T> withMaxBufferedDuration(Duration maxBufferedDuration){
			this.maxBufferedDuration = maxBufferedDuration;
			return this;
		}

		public FlushingSetBufferBuilder<T> withMaxSize(int maxSize){
			this.maxSize = maxSize;
			return this;
		}

		public FlushingSetBufferBuilder<T> withClock(Clock clock){
			this.clock = clock;
			return this;
		}

		public FlushingSetBufferBuilder<T> withFlushConsumer(Consumer<Set<T>> flushConsumer){
			this.flushConsumer = flushConsumer;
			return this;
		}

		public FlushingSetBuffer<T> build(){
			return new FlushingSetBuffer<>(name, maxSize, maxBufferedDuration, flushConsumer, clock);
		}

	}

	public synchronized boolean add(T item){
		if(item == null){
			return false; // don't buffer null items
		}
		if(!set.add(item)){
			return false; // item already in buffer
		}
		if(set.size() >= maxSize){
			flush("max size reached");
		}
		return true;
	}

	private synchronized void flushIfBufferExpired(){
		if(clock.millis() >= timeOfBufferExpirationMs){
			flush("buffer expired");
		}
	}

	private synchronized void flush(String flushReason){
		try{
			flushConsumer.accept(set);
			Counters.inc("Datarouter buffer " + name + " " + flushReason);
		}catch(Exception e){
			Counters.inc("Datarouter buffer " + name + " failed on " + flushReason);
			logger.error("Failed to flush buffer because the flush consumer failed.", e);
		}
		set.clear();
		timeOfBufferExpirationMs = Clock.offset(clock, maxBufferedDuration).millis();
	}

}
