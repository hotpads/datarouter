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
package io.datarouter.joblet.execute;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.joblet.JobletCounters;
import io.datarouter.joblet.dto.RunningJoblet;
import io.datarouter.joblet.model.Joblet;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletFactory;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.DateTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.mutable.MutableBoolean;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.util.ExceptionTool;

public class JobletCallable implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(JobletCallable.class);

	private static final Duration LOG_JOBLETS_SLOWER_THAN = Duration.ofMinutes(5);

	private final ServerName serverName;
	private final JobletService jobletService;
	private final JobletFactory jobletFactory;

	private final MutableBoolean shutdownRequested;
	private final JobletProcessor processor;// for callback
	private final JobletType<?> jobletType;
	private final long id;
	private final Instant startedAt;
	private Optional<JobletPackage> jobletPackage;

	public JobletCallable(
			ServerName serverName,
			JobletService jobletService,
			JobletFactory jobletFactory,
			MutableBoolean shutdownRequested,
			JobletProcessor processor,
			JobletType<?> jobletType,
			long id){
		this.serverName = serverName;
		this.jobletService = jobletService;
		this.jobletFactory = jobletFactory;
		this.shutdownRequested = shutdownRequested;
		this.processor = processor;
		this.jobletType = jobletType;
		this.id = id;
		this.startedAt = Instant.now();
		this.jobletPackage = Optional.empty();
	}

	@Override
	public Void call(){
		try{
			var timer = new PhaseTimer(jobletType.getPersistentString() + "-" + id);
			jobletPackage = dequeueJobletPackage(timer);
			if(jobletPackage.isEmpty()){
				return null;
			}
			JobletRequest jobletRequest = jobletPackage.get().getJobletRequest();
			try{
				processJobletWithStats(timer, jobletPackage.get());
				jobletService.handleJobletCompletion(timer, jobletRequest);
			}catch(Throwable e){
				boolean isInterrupted = ExceptionTool.isInterrupted(e);
				@SuppressWarnings("deprecation")
				Exception wrappingException = new Exception("isInterrupted=" + isInterrupted + " jobletPackage="
						+ DatarouterGsons.withUnregisteredEnums().toJson(jobletPackage.get()), e);
				logger.error("joblet failed", wrappingException);
				if(isInterrupted){
					JobletCounters.incInterrupted(jobletType);
					try{
						jobletService.handleJobletInterruption(timer, jobletRequest);
					}catch(Exception e1){
						logger.error("", e1);
					}
				}else{
					JobletCounters.incErrored(jobletType);
					try{
						jobletService.handleJobletError(
								timer,
								jobletRequest,
								wrappingException,
								jobletRequest.getKey().getType());
					}catch(Exception lastResort){
						logger.error("", lastResort);
						timer.add("couldn't mark failed");
					}
				}
			}
			logger.info("finished {} {}", jobletPackage.map(JobletPackage::getJobletRequest), timer);
			return null;
		}catch(Exception e){
			logger.error("", e);
			throw e;
		}finally{
			processor.onCompletion(id);
		}
	}

	private Optional<JobletPackage> dequeueJobletPackage(PhaseTimer timer){
		String reservedBy = getReservedByString();
		Optional<JobletRequest> optJobletRequest = jobletService.getJobletRequestForProcessing(timer, jobletType,
				reservedBy);
		if(optJobletRequest.isEmpty()){
			timer.add("no JobletRequest found");
			return Optional.empty();
		}
		JobletRequest jobletRequest = optJobletRequest.get();
		timer.add("dequeued " + jobletRequest.getKey());
		JobletPackage jobletPackage = jobletService.getJobletPackageForJobletRequest(jobletRequest);
		if(jobletPackage.getJobletData() == null){
			JobletCounters.ignoredDataMissingFromDb(jobletType);
			jobletService.handleMissingJobletData(jobletRequest);
			timer.add("deleted, missing JobletData");
			return Optional.empty();
		}
		timer.add("getJobletData");
		return Optional.of(jobletPackage);
	}

	private String getReservedByString(){
		String timeString = DateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(System.currentTimeMillis());
		String threadIdString = Thread.currentThread().getId() + "";
		String idString = id + "";
		return String.join("_", serverName.get(), timeString, threadIdString, idString);
	}

	private void processJobletWithStats(PhaseTimer timer, JobletPackage jobletPackage) throws Throwable{
		Joblet<?> joblet = jobletFactory.createForPackage(jobletPackage, shutdownRequested);
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		long startTimeMs = System.currentTimeMillis();
		joblet.process();

		// counters
		JobletCounters.incNumJobletsProcessed();
		JobletCounters.incNumJobletsProcessed(jobletType);
		JobletCounters.incNumJobletsProcessed(jobletType, jobletRequest.getKey().getPriority());
		int numItemsProcessed = Math.max(1, jobletRequest.getNumItems());
		JobletCounters.incItemsProcessed(jobletType, numItemsProcessed);
		timer.add("processed " + numItemsProcessed + " items");
		long endTimeMs = System.currentTimeMillis();
		long durationMs = endTimeMs - startTimeMs;
		JobletCounters.recordDuration(jobletType, durationMs, jobletRequest.getNumItems());
		String itemsPerSecond = NumberFormatter.format((double)jobletRequest.getNumItems() / ((double)durationMs
				/ (double)1000), 1);

		// logging
		String message = "finished"
				+ " type=" + jobletType.getPersistentString()
				+ " queue=" + jobletRequest.getQueueId()
				+ " itemCount=" + jobletRequest.getNumItems()
				+ " durationMs=" + durationMs
				+ " duration=" + new DatarouterDuration(durationMs, TimeUnit.MILLISECONDS)
				+ " itemsPerSecond=" + itemsPerSecond;
		if(durationMs > JobletProcessor.RUNNING_JOBLET_TIMEOUT_MS){
			logger.warn("finally {}", message);
		}else if(durationMs > LOG_JOBLETS_SLOWER_THAN.toMillis()){
			logger.warn(message);
		}else{
			logger.info(message);
		}
	}

	public RunningJoblet getRunningJoblet(){
		return new RunningJoblet(jobletType, id, startedAt, jobletPackage);
	}

}
