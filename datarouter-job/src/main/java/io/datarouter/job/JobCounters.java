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
package io.datarouter.job;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.util.DatarouterCounters;

@Singleton
public class JobCounters{

	private static final String PREFIX = "job";

	@Inject
	private Gauges gauges;

	public void exception(Class<? extends BaseJob> jobClass){
		count(jobClass, "exception");
	}

	public void finished(Class<? extends BaseJob> jobClass){
		count(jobClass, "finished");
	}

	public void missedNextTrigger(Class<? extends BaseJob> jobClass){
		count(jobClass, "missed next trigger");
	}

	public void schedulingImmediately(Class<? extends BaseJob> jobClass){
		count(jobClass, "scheduling immediately");
	}

	public void started(Class<? extends BaseJob> jobClass){
		count(jobClass, "started");
	}

	public void startedAfterLongDelay(Class<? extends BaseJob> jobClass){
		count(jobClass, "started after long delay");
	}

	public void heartbeat(String jobName){
		count(jobName, "heartbeat");
	}

	public void shouldStop(String jobName, String reason){
		count(jobName, "shouldStop " + reason);
	}

	public void duration(Class<? extends BaseJob> jobClass, Duration elapsedTime){
		String name = "durationMs";
		long value = elapsedTime.toMillis();
		String jobName = jobClass.getSimpleName();
		gauges.save(DatarouterCounters.PREFIX + " " + PREFIX + " " + name, value);
		gauges.save(DatarouterCounters.PREFIX + " " + PREFIX + " " + jobName + " " + name, value);
	}

	/*---------------- private -----------------*/

	private void count(Class<? extends BaseJob> jobClass, String name){
		count(jobClass.getSimpleName(), name);
	}

	private void count(String jobName, String name){
		Counters.inc(DatarouterCounters.PREFIX + " " + PREFIX + " " + name);
		Counters.inc(DatarouterCounters.PREFIX + " " + PREFIX + " " + jobName + " " + name);
	}

}
