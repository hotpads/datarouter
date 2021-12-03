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

import java.time.Duration;
import java.time.Instant;

public interface TaskTracker{

	String getName();

	String getServerName();

	/**
	 * Set the intended start time of the task, before it's enqueued for execution
	 */
	TaskTracker setScheduledTime(Instant scheduledTime);
	Instant getScheduledTime();

	/**
	 * Call when the task starts executing, potentially after waiting in a queue
	 */
	TaskTracker start();
	TaskTracker finish();

	Instant getStartTime();
	Instant getFinishTime();

	TaskTracker heartbeat();
	TaskTracker heartbeat(long count);

	TaskTracker increment();
	TaskTracker increment(long incrementBy);
	long getCount();

	TaskTracker setLastItemProcessed(String lastItemProcessed);
	String getLastItem();

	TaskTracker setStatus(TaskStatus status);
	TaskStatus getStatus();

	TaskTracker requestStop();
	boolean shouldStop();

	default Duration getElapsedTime(){
		return Duration.between(getStartTime(), Instant.now());
	}

}
