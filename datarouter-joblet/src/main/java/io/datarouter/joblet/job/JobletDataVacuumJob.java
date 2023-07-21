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
package io.datarouter.joblet.job;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.model.databean.Databean;
import io.datarouter.util.number.NumberFormatter;
import jakarta.inject.Inject;

public class JobletDataVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletDataVacuumJob.class);

	@Inject
	private DatarouterJobletDataDao jobletDataDao;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	@Override
	public void run(TaskTracker tracker) throws RuntimeException{
		AtomicLong earliestCreated = new AtomicLong(Instant.now().toEpochMilli());
		jobletRequestDao.scan()
				.advanceUntil($ -> tracker.shouldStop())
				.each($ -> tracker.increment())
				.forEach(jobletRequest -> {
					if(jobletRequest.getKey().getCreated() <= earliestCreated.longValue()){
						earliestCreated.set(jobletRequest.getKey().getCreated());
					}
				});
		AtomicInteger jobletDeletionCount = new AtomicInteger(0);
		jobletDataDao.scan()
				.advanceUntil($ -> tracker.shouldStop())
				.batch(1_000)
				.each(batch -> tracker.increment(batch.size()))
				.map(batch -> {
					return batch.stream()
							.filter(data -> data.getCreated() == null
									|| data.getCreated() < earliestCreated.longValue())
							.map(Databean::getKey)
							.collect(Collectors.toList());
				})
				.each(jobletDataDao::deleteMulti)
				.map(List::size)
				.each(jobletDeletionCount::getAndAdd)
				.forEach($ -> logger.warn("JobletDataVacuumJob deleted {} JobletDatas",
							NumberFormatter.addCommas(jobletDeletionCount)));
		logger.warn("Completed JobletDataVacuumJob deleted {} total JobletDatas",
				NumberFormatter.addCommas(jobletDeletionCount));
	}

}
