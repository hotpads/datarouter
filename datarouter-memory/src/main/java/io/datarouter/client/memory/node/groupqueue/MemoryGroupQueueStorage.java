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
package io.datarouter.client.memory.node.groupqueue;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.datarouter.client.memory.util.CloseableReentrantReadWriteLock;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.concurrent.ExecutorServiceTool;

public class MemoryGroupQueueStorage{

	private static final Duration EXPIRATION_INTERVAL = Duration.ofSeconds(1);

	private final CloseableReentrantReadWriteLock lock;
	private final Queue<MemoryGroupQueueMessage> queue;
	private final Map<String,MemoryGroupQueueMessage> visibleById;
	private final ScheduledExecutorService visibilityExpirationExec;

	public MemoryGroupQueueStorage(){
		lock = new CloseableReentrantReadWriteLock();
		queue = new LinkedList<>();
		visibleById = new HashMap<>();
		visibilityExpirationExec = Executors.newSingleThreadScheduledExecutor();
		visibilityExpirationExec.scheduleWithFixedDelay(
				this::expireVisible,
				EXPIRATION_INTERVAL.toMillis(),
				EXPIRATION_INTERVAL.toMillis(),
				TimeUnit.MILLISECONDS);
	}

	public void shutdown(){
		ExecutorServiceTool.shutdown(visibilityExpirationExec, Duration.ofSeconds(1));
	}

	public void add(MemoryGroupQueueMessage message){
		try(var _ = lock.lockForWriting()){
			queue.add(message);
		}
	}

	public MemoryGroupQueueMessage poll(){
		try(var _ = lock.lockForWriting()){
			return queue.poll();
		}
	}

	public List<MemoryGroupQueueMessage> peek(int limit, long visibilityTimeout){
		long visibleExpirationMs = System.currentTimeMillis() + visibilityTimeout;
		try(var _ = lock.lockForWriting()){
			return Scanner.generate(queue::poll)
					.advanceWhile(Objects::nonNull)
					.limit(limit)
					.each(message -> message.setVisibilityExpirationMs(visibleExpirationMs))
					.each(message -> visibleById.put(message.getId(), message))
					.list();
		}
	}

	public void ack(String id){
		try(var _ = lock.lockForWriting()){
			visibleById.remove(id);
		}
	}

	private void expireVisible(){
		List<MemoryGroupQueueMessage> messages;
		try(var _ = lock.lockForReading()){
			messages = Scanner.of(visibleById.values())
					.include(MemoryGroupQueueMessage::isVisibilityExpired)
					.list();
		}
		messages.forEach(MemoryGroupQueueMessage::clearVisibilityExpiration);
		try(var _ = lock.lockForWriting()){
			Scanner.of(messages)
					.each(message -> visibleById.remove(message.getId()))
					.forEach(queue::add);
		}

	}

}
