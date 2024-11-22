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
package io.datarouter.instrumentation.task;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MemoryTaskTracker implements TaskTracker{

	private final String name;
	private final String serverName;
	private final AtomicLong scheduledTimeMs;
	private final AtomicLong startTimeMs;
	private final AtomicLong finishTimeMs;
	private final AtomicLong lastHeartbeatMs;
	private final AtomicLong count;
	private final AtomicReference<String> lastItem;
	private final AtomicReference<TaskStatus> status;
	private final AtomicBoolean stopRequested;

	public MemoryTaskTracker(String name, String serverName, Instant scheduledTime){
		this.name = name;
		this.serverName = serverName;
		this.scheduledTimeMs = new AtomicLong(scheduledTime.toEpochMilli());
		this.startTimeMs = new AtomicLong();
		this.finishTimeMs = new AtomicLong();
		this.lastHeartbeatMs = new AtomicLong();
		this.count = new AtomicLong();
		this.lastItem = new AtomicReference<>();
		this.status = new AtomicReference<>();
		this.stopRequested = new AtomicBoolean(false);
	}

	public MemoryTaskTracker(String name){
		this(name, null, Instant.now());
	}

	public MemoryTaskTracker(Class<?> cls){
		this(cls.getSimpleName());
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	@Override
	public TaskTracker setScheduledTime(Instant scheduledTime){
		scheduledTimeMs.set(scheduledTime.toEpochMilli());
		return this;
	}

	@Override
	public Instant getScheduledTime(){
		return Instant.ofEpochMilli(scheduledTimeMs.get());
	}

	@Override
	public TaskTracker start(){
		startTimeMs.set(System.currentTimeMillis());
		return this;
	}

	@Override
	public Instant getStartTime(){
		return Instant.ofEpochMilli(startTimeMs.get());
	}

	@Override
	public TaskTracker finish(){
		finishTimeMs.set(System.currentTimeMillis());
		return this;
	}

	@Override
	public Instant getFinishTime(){
		return Instant.ofEpochMilli(finishTimeMs.get());
	}

	@Override
	public TaskTracker heartbeat(){
		lastHeartbeatMs.set(System.currentTimeMillis());
		return this;
	}

	@Override
	public TaskTracker heartbeat(long latestCount){
		heartbeat();
		count.set(latestCount);
		return this;
	}

	@Override
	public TaskTracker setCount(long value){
		count.set(value);
		return this;
	}

	@Override
	public TaskTracker increment(){
		count.incrementAndGet();
		return this;
	}

	@Override
	public TaskTracker increment(long incrementBy){
		count.addAndGet(incrementBy);
		return this;
	}

	@Override
	public long getCount(){
		return count.get();
	}

	@Override
	public TaskTracker setLastItemProcessed(String lastItemProcessed){
		heartbeat();
		lastItem.set(lastItemProcessed);
		return this;
	}

	@Override
	public String getLastItem(){
		return lastItem.get();
	}

	@Override
	public TaskTracker setStatus(TaskStatus status){
		this.status.set(status);
		return this;
	}

	@Override
	public TaskStatus getStatus(){
		return status.get();
	}

	@Override
	public TaskTracker requestStop(){
		stopRequested.set(true);
		return this;
	}

	@Override
	public boolean shouldStop(){
		return stopRequested.get();
	}

}
