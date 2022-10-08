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

import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.job.config.DatarouterJobExecutors.DatarouterJobExecutor;
import io.datarouter.job.scheduler.JobWrapper;
import io.datarouter.job.util.Outcome;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.ExceptionTool;

@Singleton
public class LocalJobProcessor{
	private static final Logger logger = LoggerFactory.getLogger(LocalJobProcessor.class);

	private static final Duration MAX_JOB_TIMEOUT = Duration.ofDays(60);

	@Inject
	private DatarouterJobExecutor jobExecutor;
	@Inject
	private JobCounters jobCounters;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	public Outcome run(JobWrapper jobWrapper){
		if(jobExecutor.isShutdown()){
			logger.warn("DatarouterJobExecutor is shutdown, {} cannot be triggered.",
					jobWrapper.jobClass.getSimpleName());
			return Outcome.failure("DatarouterJobExecutor is shutdown");
		}else{
			Duration hardTimeout = getHardTimeout(jobWrapper);
			Future<Void> future;
			try{
				future = jobExecutor.submit(jobWrapper);
				jobWrapper.setFuture(future);
			}catch(RejectedExecutionException e){
				jobWrapper.finishWithStatus(LongRunningTaskStatus.ERRORED);
				throw wrapAndSaveException("rejected", jobWrapper, hardTimeout, e);
			}
			try{
				future.get(hardTimeout.toMillis(), TimeUnit.MILLISECONDS);
				return Outcome.success();
			}catch(InterruptedException | ExecutionException | CancellationException e){
				if(ExceptionTool.isFromInstanceOf(e,
						InterruptedException.class,
						UncheckedInterruptedException.class,//TODO this can't match here, since it's not in catch?
						InterruptedIOException.class,
						CancellationException.class)){
					future.cancel(true);
					jobWrapper.finishWithStatus(LongRunningTaskStatus.INTERRUPTED);
					jobCounters.interrupted(jobWrapper.jobClass);
					logger.warn("", wrapAndSaveException("interrupted", jobWrapper, hardTimeout, e));
					return Outcome.failure("Interrupted. exception=" + e);
				}
				jobWrapper.finishWithStatus(LongRunningTaskStatus.ERRORED);
				throw wrapAndSaveException("failed", jobWrapper, hardTimeout, e);
			}catch(TimeoutException e){
				future.cancel(true);
				jobWrapper.finishWithStatus(LongRunningTaskStatus.TIMED_OUT);
				jobCounters.timedOut(jobWrapper.jobClass);
				throw wrapAndSaveException("didn't complete on time", jobWrapper, hardTimeout, e);
			}
		}
	}

	public void shutdown(){
		jobExecutor.shutdownNow();
	}

	private RuntimeException wrapAndSaveException(String msg, JobWrapper jobWrapper, Duration hardTimeout,
			Exception ex){
		var elapsed = DatarouterDuration.age(jobWrapper.triggerTime);
		var exception = new RuntimeException(msg
				+ " jobName=" + jobWrapper.jobClass.getName()
				+ " elapsed=" + elapsed
				+ " deadline=" + new DatarouterDuration(hardTimeout), ex);
		exceptionRecorder
				.tryRecordException(exception, jobWrapper.jobClass.getName(), JobExceptionCategory.JOB)
				.ifPresent(exceptionRecord -> jobWrapper.setExceptionRecordId(exceptionRecord.id()));
		return exception;
	}

	private Duration getHardTimeout(JobWrapper jobWrapper){
		return jobWrapper.jobPackage.getHardDeadline(jobWrapper.triggerTime)
				.map(deadline -> Duration.between(Instant.now(), deadline))
				.orElse(MAX_JOB_TIMEOUT);
	}
}
