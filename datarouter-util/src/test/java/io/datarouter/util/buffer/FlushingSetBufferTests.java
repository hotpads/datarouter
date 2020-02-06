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
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.buffer.FlushingSetBuffer.FlushingSetBufferBuilder;
import io.datarouter.util.concurrent.ThreadTool;

public class FlushingSetBufferTests{

	@Test
	public void testMaxSize(){
		AtomicInteger flushCallCount = new AtomicInteger(0);
		AtomicInteger flushDataCount = new AtomicInteger(0);
		FlushingSetBuffer<String> buffer = new FlushingSetBufferBuilder<String>()
				.withMaxSize(5)
				.withMaxBufferedDuration(Duration.ofDays(1))
				.withFlushConsumer(data -> {
					flushCallCount.incrementAndGet();
					flushDataCount.addAndGet(data.size());
				})
				.build();
		Assert.assertEquals(flushCallCount.get(), 0);
		Assert.assertEquals(flushDataCount.get(), 0);
		buffer.add("item 1");
		buffer.add("item 2");
		buffer.add("item 3");
		buffer.add("item 4");
		buffer.add("item 5");
		buffer.add("item 6");
		Assert.assertEquals(flushCallCount.get(), 1);
		Assert.assertEquals(flushDataCount.get(), 5);
	}

	@Test
	public void testBufferedDuration(){
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Duration bufferedDuration = Duration.ofHours(1);
		AtomicInteger flushCallCount = new AtomicInteger(0);
		FlushingSetBuffer<String> buffer = new FlushingSetBufferBuilder<String>()
				.withClock(clock)
				.withMaxSize(Integer.MAX_VALUE)
				.withMaxBufferedDuration(bufferedDuration)
				.withFlushConsumer(data -> {
					flushCallCount.incrementAndGet();
				})
				.build();
		Assert.assertEquals(flushCallCount.get(), 0);
		buffer.clock = Clock.offset(clock, bufferedDuration);
		ThreadTool.sleep(FlushingSetBuffer.MAX_BUFFER_FLUSH_CHECK_INTERVAL.toMillis() * 2);
		Assert.assertEquals(flushCallCount.get(), 1);
	}

}