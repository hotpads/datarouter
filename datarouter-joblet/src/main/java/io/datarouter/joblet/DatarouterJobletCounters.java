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
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.metric.Gauges;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobletCounters{

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

	public record JobletUiLinkNamesAndPrefix(
			String linkName,
			String prefix){
	}

	@Inject
	private Gauges gauges;

	public static String makeQueueLengthJobletsCreatedPrefix(String jobletType){
		return String.format("Joblet queue length joblets created %s", jobletType);
	}

	public void saveQueueLengthJoblets(JobletStatus jobletStatus, String jobletType, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString + " " + jobletType,
				queueLength);
	}

	public void saveQueueLengthJobletsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString + " " + jobletType + " "
				+ queueId, queueLength);
	}

	public void saveGlobalQueueLengthJoblets(JobletStatus jobletStatus, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_JOBLETS + jobletStatus.persistentString, queueLength);
	}

	public void saveQueueLengthItems(JobletStatus jobletStatus, String jobletType, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString + " " + jobletType, queueLength);
	}

	public void saveQueueLengthItemsForQueueId(JobletStatus jobletStatus, String jobletType, String queueId,
			long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString + " " + jobletType + " "
				+ queueId, queueLength);
	}

	public void saveGlobalQueueLengthItems(JobletStatus jobletStatus, long queueLength){
		gauges.save(PREFIX + QUEUE_LENGTH_ITEMS + jobletStatus.persistentString, queueLength);
	}

	public void saveFirst(JobletStatus jobletStatus, String jobletType, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.persistentString + " " + jobletType, firstCreated);
	}

	public void saveFirstForQueueId(JobletStatus jobletStatus, String jobletType, String queueId, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.persistentString + " " + jobletType + " " + queueId,
				firstCreated);
	}

	public void saveGlobalFirst(JobletStatus jobletStatus, long firstCreated){
		gauges.save(PREFIX + FIRST + jobletStatus.persistentString, firstCreated);
	}

	public void saveNumServers(long numServers){
		gauges.save(PREFIX + "num servers", numServers);
	}

	public void saveTargetServers(long numTargetServers){
		gauges.save(PREFIX + "target servers", numTargetServers);
	}

	public void recordDuration(JobletType<?> jobletType, long durationMs, int numItems){
		gauges.save(PREFIX + "durationMs " + jobletType, durationMs);
		gauges.save(PREFIX + "item durationMs " + jobletType, durationMs / numItems);
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

	public void ignoredDataMissingFromDb(JobletType<?> jobletType){
		Counters.inc(PREFIX + "ignored data missing from db " + jobletType.getPersistentString());
	}

}
