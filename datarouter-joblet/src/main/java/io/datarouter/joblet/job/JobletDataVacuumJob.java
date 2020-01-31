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
package io.datarouter.joblet.job;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletdata.JobletData;
import io.datarouter.joblet.storage.jobletdata.JobletDataKey;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public class JobletDataVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletDataVacuumJob.class);

	@Inject
	private DatarouterJobletDataDao jobletDataDao;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	@Override
	public void run(TaskTracker tracker) throws RuntimeException{
		long earliestCreated = Calendar.getInstance().getTimeInMillis();
		for(JobletRequest jobletRequest : jobletRequestDao.scan().iterable()){
			if(tracker.increment().shouldStop()){
				return;
			}
			if(jobletRequest.getKey().getCreated() <= earliestCreated){
				earliestCreated = jobletRequest.getKey().getCreated();
			}
		}
		int jobletDeletionCount = 0;
		Scanner<JobletData> scanner = jobletDataDao.scan();
		for(List<JobletData> batch : scanner.batch(1000).iterable()){
			if(tracker.shouldStop()){
				return;
			}
			final long finalEarliestCreated = earliestCreated;
			List<JobletDataKey> jobletDataKeysToDelete = batch.stream()
					.filter(data -> data.getCreated() == null || data.getCreated() < finalEarliestCreated)
					.map(Databean::getKey)
					.collect(Collectors.toList());
			jobletDataDao.deleteMulti(jobletDataKeysToDelete);
			jobletDeletionCount = jobletDeletionCount + jobletDataKeysToDelete.size();
			logger.warn("JobletDataVacuumJob deleted {} JobletDatas", NumberFormatter.addCommas(jobletDeletionCount));
			tracker.increment(batch.size());
		}
		logger.warn("Completed JobletDataVacuumJob deleted {} total JobletDatas",
				NumberFormatter.addCommas(jobletDeletionCount));
	}

}
