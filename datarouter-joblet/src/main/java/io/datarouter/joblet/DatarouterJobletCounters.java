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
package io.datarouter.joblet;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.util.tuple.Twin;

@Singleton
public class DatarouterJobletCounters{

	private static final String PREFIX = "Joblet ";

	private static final String QUEUE_LENGTH_JOBLETS = "queue length joblets ";
	private static final String QUEUE_LENGTH_ITEMS = "queue length items ";
	private static final String FIRST = "first ";

	public static final List<Twin<String>> UI_LINK_NAMES_AND_PREFIXES = List.of(
			Twin.of("First created", "Joblet first created "),
			Twin.of("Queue Length Joblets", "Joblet queue length joblets "),
			Twin.of("Queue Length Items", "Joblet queue length items "),
			Twin.of("Joblets Inserted", "Joblet inserted "),
			Twin.of("Joblets Processed", "Joblet processed "),
			Twin.of("Items Processed", "Joblet items processed "),
			Twin.of("Target Servers", "Joblet target servers "),
			Twin.of("Actual Servers", "Joblet num servers "));


	@Inject
	private Gauges gauges;

	public static String makeQueueLengthJobletsCreatedPrefix(String jobletType){
		return String.format("Joblet queue length joblets created %s", jobletType);
	}

	public void saveQueueLengthJoblets(JobletStatus jobletStatus, String jobletType, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.getPersistentString() + " " + jobletType,
				queueLength);
	}

	public void saveQueueLengthJobletsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.getPersistentString() + " " + jobletType + " "
				+ queueId, queueLength);
	}

	public void saveGlobalQueueLengthJoblets(JobletStatus jobletStatus, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.getPersistentString(), queueLength);
	}

	public void saveQueueLengthItems(JobletStatus jobletStatus, String jobletType, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.getPersistentString() + " " + jobletType, queueLength);
	}

	public void saveQueueLengthItemsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.getPersistentString() + " " + jobletType + " "
				+ queueId, queueLength);
	}

	public void saveGlobalQueueLengthItems(JobletStatus jobletStatus, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.getPersistentString(), queueLength);
	}

	public void saveFirst(JobletStatus jobletStatus, String jobletType, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.getPersistentString() + " " + jobletType, firstCreated);
	}

	public void saveFirstForQueueId(JobletStatus jobletStatus, String jobletType, String queueId, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.getPersistentString() + " " + jobletType + " " + queueId,
				firstCreated);
	}

	public void saveGlobalFirst(JobletStatus jobletStatus, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.getPersistentString(), firstCreated);
	}

	public void saveNumServers(long numServers){
		gauges.save(PREFIX + "num servers", numServers);
	}

	public void saveTargetServers(long numTargetServers){
		gauges.save(PREFIX + "target servers", numTargetServers);
	}

	public void recordDuration(JobletType<?> jobletType, long durationMs){
//		UI is currently (2018-06-11) not view metrics across multiple server (aggregate by sum instead of avg)
//		metrics.save(PREFIX + "duration " + jobletType, durationMs);
//		metrics.save(PREFIX + "item duration " + jobletType, durationMs / numItems);
//		so hack it with counter: avg(duration) = sum(duration) / numProssed
		Counters.inc(PREFIX + "cumulated duration", durationMs);
		Counters.inc(PREFIX + "cumulated duration " + jobletType.getPersistentString(), durationMs);
	}

	public void incNumJobletsInserted(long by){
		Counters.inc(PREFIX + "inserted", by);
	}

	public void incNumJobletsInserted(JobletType<?> jobletType, long by){
		Counters.inc(PREFIX + "inserted " + jobletType.getPersistentString(), by);
	}

	public void incNumJobletsInserted(JobletType<?> jobletType, String queueId){
		Counters.inc(PREFIX + "inserted " + jobletType.getPersistentString() + " " + queueId, 1);
	}

	public void incNumJobletsExpired(long by){
		Counters.inc(PREFIX + "expired", by);
	}

	public void incNumJobletsExpired(JobletType<?> jobletType, long by){
		Counters.inc(PREFIX + "expired " + jobletType.getPersistentString(), by);
	}

	public void incNumJobletsProcessed(){
		Counters.inc(PREFIX + "processed");
	}

	public void incNumJobletsProcessed(JobletType<?> jobletType){
		Counters.inc(PREFIX + "processed " + jobletType.getPersistentString());
	}

	public void incItemsProcessed(JobletType<?> jobletType, long delta){
		Counters.inc(PREFIX + "items processed " + jobletType.getPersistentString(), delta);
	}

	public void incQueueSkip(String key){
		Counters.inc(PREFIX + "queue " + key + " skip");
	}

	public void incQueueHit(String key){
		Counters.inc(PREFIX + "queue " + key + " hit");
	}

	public void incQueueMiss(String key){
		Counters.inc(PREFIX + "queue " + key + " miss");
	}

	public void rejectedCallable(JobletType<?> jobletType){
		Counters.inc(PREFIX + "rejected callable " + jobletType.getPersistentString());
	}

	public void ignoredRequestMissingFromDb(JobletType<?> jobletType){
		Counters.inc(PREFIX + "ignored request missing from db " + jobletType.getPersistentString());
	}

}
