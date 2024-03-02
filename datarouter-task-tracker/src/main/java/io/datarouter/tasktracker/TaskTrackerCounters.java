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
package io.datarouter.tasktracker;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.storage.util.DatarouterCounters;
import jakarta.inject.Singleton;

@Singleton
public class TaskTrackerCounters{

	private static final String COUNTER_PREFIX = "job";

	public void heartbeat(String jobName){
		count(jobName, "heartbeat", 1);
	}

	public void increment(String jobName, long delta){
		count(jobName, "increment", delta);
	}

	public void shouldStop(String jobName, String reason){
		count(jobName, "shouldStop " + reason, 1);
	}

	private void count(String jobName, String name, long delta){
		Metrics.count(DatarouterCounters.PREFIX + " " + COUNTER_PREFIX + " " + name, delta);
		Metrics.count(DatarouterCounters.PREFIX + " " + COUNTER_PREFIX + " " + jobName + " " + name, delta);
	}

}
