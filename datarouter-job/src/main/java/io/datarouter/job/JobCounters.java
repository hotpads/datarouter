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
package io.datarouter.job;

import java.time.Duration;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.storage.util.DatarouterCounters;

public class JobCounters{

	private static final String PREFIX = "job";

	public static void exception(Class<? extends BaseJob> jobClass){
		count(jobClass, "exception");
	}

	public static void finished(Class<? extends BaseJob> jobClass){
		count(jobClass, "finished");
	}

	public static void interrupted(Class<? extends BaseJob> jobClass){
		count(jobClass, "interrupted");
	}

	public static void timedOut(Class<? extends BaseJob> jobClass){
		count(jobClass, "timedOut");
	}

	public static void missedNextTrigger(Class<? extends BaseJob> jobClass){
		count(jobClass, "missed next trigger");
	}

	public static void started(Class<? extends BaseJob> jobClass){
		count(jobClass, "started");
	}

	public static void startedAfterLongDelay(Class<? extends BaseJob> jobClass){
		count(jobClass, "started after long delay");
	}

	public static void heartbeat(String jobName){
		count(jobName, "heartbeat");
	}

	public static void shouldStop(String jobName, String reason){
		count(jobName, "shouldStop " + reason);
	}

	public static void duration(Class<? extends BaseJob> jobClass, Duration elapsedTime){
		String name = "durationMs";
		long value = elapsedTime.toMillis();
		String jobName = jobClass.getSimpleName();
		Metrics.measure(DatarouterCounters.PREFIX + " " + PREFIX + " " + name, value);
		Metrics.measure(DatarouterCounters.PREFIX + " " + PREFIX + " " + jobName + " " + name, value);
	}

	/*---------------- private -----------------*/

	private static void count(Class<? extends BaseJob> jobClass, String name){
		count(jobClass.getSimpleName(), name);
	}

	private static void count(String jobName, String name){
		Metrics.count(DatarouterCounters.PREFIX + " " + PREFIX + " " + name);
		Metrics.count(DatarouterCounters.PREFIX + " " + PREFIX + " " + jobName + " " + name);
	}

}
