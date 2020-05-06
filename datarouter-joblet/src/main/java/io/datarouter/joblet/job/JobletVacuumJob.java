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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletdata.JobletDataKey;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.number.NumberFormatter;

public class JobletVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletVacuumJob.class);

	private static final int BATCH_SIZE = 500;
	private static final Duration TOO_OLD_DURATION = Duration.ofDays(3);

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletDataDao jobletDataDao;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	private int jobletRequestsDeleted = 0;
	private int itemsDeleted = 0;

	@Override
	public void run(TaskTracker tracker){
		Iterable<JobletRequest> jobletRequests = jobletRequestDao.scan().iterable();
		List<JobletRequestKey> jobletsToDelete = new ArrayList<>();
		List<JobletDataKey> jobletDataToDelete = new ArrayList<>();
		for(JobletRequest jobletRequest : jobletRequests){
			if(shouldDelete(jobletRequest)){
				jobletsToDelete.add(jobletRequest.getKey());
				jobletDataToDelete.add(jobletRequest.getJobletDataKey());
				jobletRequestsDeleted++;
				itemsDeleted = itemsDeleted + jobletRequest.getNumItems();
				if(jobletsToDelete.size() >= BATCH_SIZE){
					deleteJobletRequests(jobletsToDelete, jobletDataToDelete);
				}
			}
		}
		if(CollectionTool.nullSafeNotEmpty(jobletsToDelete)){
			deleteJobletRequests(jobletsToDelete, jobletDataToDelete);
		}
	}

	private void deleteJobletRequests(List<JobletRequestKey> jobletRequestKeys, List<JobletDataKey> jobletDataKeys){
		jobletRequestDao.deleteMulti(jobletRequestKeys);
		jobletDataDao.deleteMulti(jobletDataKeys);
		jobletRequestKeys.clear();
		jobletDataKeys.clear();
		logger.warn("num jobletRequests deleted: {}, num items deleted: {}",
				NumberFormatter.addCommas(jobletRequestsDeleted),
				NumberFormatter.addCommas(itemsDeleted));
	}

	private boolean shouldDelete(JobletRequest jobletRequest){
		if(!jobletTypeFactory.isRecognizedType(jobletRequest)){
			return true;
		}
		boolean old = ComparableTool.gt(jobletRequest.getKey().getAge(), TOO_OLD_DURATION);
		if(!old){
			return false;
		}
		boolean failed = jobletRequest.getStatus() == JobletStatus.FAILED;
		boolean timedOut = jobletRequest.getStatus() == JobletStatus.TIMED_OUT;
		boolean running = jobletRequest.getStatus() == JobletStatus.RUNNING;
		return failed || timedOut || running;
	}

}
