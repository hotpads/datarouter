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
package io.datarouter.joblet;

import java.util.List;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.gauge.Gauges;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.type.JobletType;

public class JobletCounters{

	private static final String PREFIX = "Joblet ";

	private static final String QUEUE_LENGTH_JOBLETS = "queue length joblets ";
	private static final String QUEUE_LENGTH_ITEMS = "queue length items ";
	private static final String FIRST = "first ";

	public static final List<JobletUiLinkNamesAndPrefix> UI_LINK_NAMES_AND_PREFIXES = List.of(
			new JobletUiLinkNamesAndPrefix("First created", "Joblet first created "),
			new JobletUiLinkNamesAndPrefix("Queue Length Joblets", "Joblet queue length joblets "),
			new JobletUiLinkNamesAndPrefix("Queue Length Items", "Joblet queue length items "),
			new JobletUiLinkNamesAndPrefix("Joblets Inserted", "Joblet inserted "),
			new JobletUiLinkNamesAndPrefix("Joblets Processed", "Joblet processed "),
			new JobletUiLinkNamesAndPrefix("Items Processed", "Joblet items processed "),
			new JobletUiLinkNamesAndPrefix("Target Servers", "Joblet target servers "),
			new JobletUiLinkNamesAndPrefix("Actual Servers", "Joblet num servers "));

	public static String makeQueueLengthJobletsCreatedPrefix(String jobletType){
		return String.format("Joblet queue length joblets created %s", jobletType);
	}

	public static void saveQueueLengthJoblets(JobletStatus jobletStatus, String jobletType, long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString + " " + jobletType, queueLength);
	}

	public static void saveQueueLengthJobletsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString + " " + jobletType + " " + queueId,
				queueLength);
	}

	public static void saveGlobalQueueLengthJoblets(JobletStatus jobletStatus, long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString, queueLength);
	}

	public static void saveQueueLengthItems(JobletStatus jobletStatus, String jobletType, long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString + " " + jobletType, queueLength);
	}

	public static void saveQueueLengthItemsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString + " " + jobletType + " " + queueId,
				queueLength);
	}

	public static void saveGlobalQueueLengthItems(JobletStatus jobletStatus, long queueLength){
		Gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString, queueLength);
	}

	public static void saveFirst(JobletStatus jobletStatus, String jobletType, long firstCreated){
		Gauges.save(PREFIX + FIRST + jobletStatus.persistentString + " " + jobletType, firstCreated);
	}

	public static void saveFirstForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long firstCreated){
		Gauges.save(PREFIX + FIRST + jobletStatus.persistentString + " " + jobletType + " " + queueId, firstCreated);
	}

	public static void saveGlobalFirst(JobletStatus jobletStatus, long firstCreated){
		Gauges.save(PREFIX + FIRST + jobletStatus.persistentString, firstCreated);
	}

	public static void saveNumServers(long numServers){
		Gauges.save(PREFIX + "num servers", numServers);
	}

	public static void saveTargetServers(long numTargetServers){
		Gauges.save(PREFIX + "target servers", numTargetServers);
	}

	public static void recordDuration(JobletType<?> jobletType, long durationMs, int numItems){
		Gauges.save(PREFIX + "durationMs " + jobletType, durationMs);
		Gauges.save(PREFIX + "item durationMs " + jobletType, durationMs / numItems);
//		so hack it with counter: avg(duration) = sum(duration) / numProssed
		Counters.inc(PREFIX + "cumulated duration", durationMs);
		Counters.inc(PREFIX + "cumulated duration " + jobletType.getPersistentString(), durationMs);
	}

	public static void incNumJobletsInserted(long by){
		Counters.inc(PREFIX + "inserted", by);
	}

	public static void incNumJobletsInserted(JobletType<?> jobletType, long by){
		Counters.inc(PREFIX + "inserted " + jobletType.getPersistentString(), by);
	}

	public static void incNumJobletsInserted(JobletType<?> jobletType, String queueId){
		Counters.inc(PREFIX + "inserted " + jobletType.getPersistentString() + " " + queueId, 1);
	}

	public static void incNumJobletsExpired(long by){
		Counters.inc(PREFIX + "expired", by);
	}

	public static void incNumJobletsExpired(JobletType<?> jobletType, long by){
		Counters.inc(PREFIX + "expired " + jobletType.getPersistentString(), by);
	}

	public static void incNumJobletsProcessed(){
		Counters.inc(PREFIX + "processed");
	}

	public static void incNumJobletsProcessed(JobletType<?> jobletType){
		Counters.inc(PREFIX + "processed " + jobletType.getPersistentString());
	}

	public static void incItemsProcessed(JobletType<?> jobletType, long delta){
		Counters.inc(PREFIX + "items processed " + jobletType.getPersistentString(), delta);
	}

	public static void incQueueSkip(String key){
		Counters.inc(PREFIX + "queue " + key + " skip");
	}

	public static void incQueueHit(String key){
		Counters.inc(PREFIX + "queue " + key + " hit");
	}

	public static void incQueueMiss(String key){
		Counters.inc(PREFIX + "queue " + key + " miss");
	}

	public static void rejectedCallable(JobletType<?> jobletType){
		Counters.inc(PREFIX + "rejected callable " + jobletType.getPersistentString());
	}

	public static void ignoredRequestMissingFromDb(JobletType<?> jobletType){
		Counters.inc(PREFIX + "ignored request missing from db " + jobletType.getPersistentString());
	}

	public static void ignoredDataMissingFromDb(JobletType<?> jobletType){
		Counters.inc(PREFIX + "ignored data missing from db " + jobletType.getPersistentString());
	}

	public record JobletUiLinkNamesAndPrefix(
			String linkName,
			String prefix){
	}

}
