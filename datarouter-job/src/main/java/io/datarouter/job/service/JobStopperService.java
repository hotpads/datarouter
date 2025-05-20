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
package io.datarouter.job.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.job.BaseJob;
import io.datarouter.job.detached.DetachedJobStopper.DetachedJobStopperSupplier;
import io.datarouter.job.job.DatarouterJobStopperJob;
import io.datarouter.job.lock.LocalTriggerLockService;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.job.scheduler.JobWrapper;
import io.datarouter.job.storage.stopjobrequest.StopJobRequest;
import io.datarouter.job.storage.stopjobrequest.StopJobRequestDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.concurrent.ThreadTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobStopperService{

	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private LocalTriggerLockService localTriggerLockService;
	@Inject
	private StopJobRequestDao stopJobRequestDao;
	@Inject
	private ServerName serverName;
	@Inject
	private DetachedJobStopperSupplier detachedJobStopperSupplier;

	/**
	 * generates requests to stop a job running on the specified servers in the cluster. the requests are not processed
	 * until {@link JobStopperService#stopRequestedLocalJobs} is called (see {@link DatarouterJobStopperJob})
	 * @param jobName the name of the job (class) to stop
	 * @param serverNames the servers on which to attempt to stop the job
	 * @param username the username that requested the stop
	 */
	public void requestStop(String jobName, List<String> serverNames, String username){
		Instant jobTriggerDeadline = Instant.now();
		Instant requestExpiration = jobTriggerDeadline.plus(3L, ChronoUnit.MINUTES);
		Scanner.of(serverNames)
				.map(serverName -> new StopJobRequest(
						serverName,
						requestExpiration,
						jobTriggerDeadline,
						jobName,
						username))
				.flush(stopJobRequestDao::putMulti);
	}

	/**
	 * stop all jobs that are running on this server and for which a stop request was made
	 * @param shouldStop this method will cease processing and return once this returns true
	 */
	public void stopRequestedLocalJobs(Supplier<Boolean> shouldStop){
		stopJobRequestDao.scanUnexpiredRequestsForServer(serverName.get())
				.advanceUntil(_ -> shouldStop.get())
				.forEach(this::stopRequestedLocalJob);
	}

	private void stopRequestedLocalJob(StopJobRequest stopJobRequest){
		var key = stopJobRequest.getKey();
		detachedJobStopperSupplier.get().stop(stopJobRequest);
		stopJobRequestDao.delete(key);
	}

	//TODO licenses
	public void stopLocalJob(String jobClassString, String username, Instant jobTriggerDeadline){
		Class<? extends BaseJob> jobClass = BaseJob.parseClass(jobClassString);
		JobWrapper jobWrapper = localTriggerLockService.getForClass(jobClass);
		//job isn't running anymore
		if(jobWrapper == null){
			return;
		}
		//the currently running job is not the one that the request was for
		if(jobWrapper.triggerTime.isAfter(jobTriggerDeadline)){
			return;
		}
		jobWrapper.requestStop();
		//wait for job to gracefully stop itself
		ThreadTool.sleepUnchecked(JobScheduler.JOB_STOP_GRACE_PERIOD_MS);
		Future<Void> future = jobWrapper.getFuture();
		if(!future.isDone()){
			future.cancel(true);
		}
		var changelog = new DatarouterChangelogDtoBuilder("Job", jobClassString, "interrupt", username).build();
		changelogRecorder.record(changelog);
	}

}
