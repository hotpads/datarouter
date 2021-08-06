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
package io.datarouter.joblet.storage.jobletrequest;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskStatus;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class JobletTaskTracker implements TaskTracker{
private static final Logger logger = LoggerFactory.getLogger(JobletTaskTracker.class);

	private final String name;
	private final JobletRequest jobletRequest;
	private final String serverName;
	private final AtomicLong scheduledTimeMs;
	private final AtomicLong startTimeMs;
	private final AtomicLong finishTimeMs;
	private final AtomicLong lastHeartbeatMs;
	private final AtomicLong count;
	private final AtomicReference<String> lastItem;
	private final AtomicReference<TaskStatus> status;

	public JobletTaskTracker(String name, JobletRequest jobletRequest){
		this.name = name;
		this.jobletRequest = jobletRequest;
		this.serverName = jobletRequest.getReservedBy();
		this.scheduledTimeMs = new AtomicLong(jobletRequest.getReservedAt());
		this.startTimeMs = new AtomicLong(jobletRequest.getReservedAt());
		this.finishTimeMs = new AtomicLong();
		this.lastHeartbeatMs = new AtomicLong();
		this.count = new AtomicLong();
		this.lastItem = new AtomicReference<>();
		this.status = new AtomicReference<>();
	}

	public JobletTaskTracker(Class<?> cls, JobletRequest jobletRequest){
		this(cls.getSimpleName(), jobletRequest);
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
	public TaskTracker onStart(){
		startTimeMs.set(System.currentTimeMillis());
		return this;
	}

	@Override
	public TaskTracker setStartTime(Instant startTime){
		startTimeMs.set(startTime.toEpochMilli());
		return this;
	}

	@Override
	public Instant getStartTime(){
		return Instant.ofEpochMilli(startTimeMs.get());
	}

	@Override
	public TaskTracker onFinish(){
		finishTimeMs.set(System.currentTimeMillis());
		return this;
	}

	@Override
	public TaskTracker setFinishTime(Instant finishTime){
		finishTimeMs.set(finishTime.toEpochMilli());
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
		throw new UncheckedInterruptedException("Joblet was requested to stop");
	}

	@Override
	public boolean shouldStop(){
		logger.info("{} shouldStop check requested on {}", name, serverName);
		if(Thread.interrupted()){
			logger.warn("setting shutdownRequested=true for {} because of Thread.interrupted() on {}", name,
					serverName);
			jobletRequest.getShutdownRequested().set(true);
			logger.warn("{} interrupted on {}", name, serverName);
			return true;
		}
		if(jobletRequest.getShutdownRequested().isTrue()){
			logger.warn("shutdownRequested for the {} on {}", name, serverName);
			return true;
		}
		return false;
	}

}
