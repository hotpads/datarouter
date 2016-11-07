package com.hotpads.joblet.execute.v2;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.job.JobInterruptedException;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletCallable implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(JobletCallable.class);

	private final DatarouterProperties datarouterProperties;
	private final JobletNodes jobletNodes;
	private final JobletService jobletService;
	private final JobletFactory jobletFactory;

	private final MutableBoolean shutdownRequested;
	private final JobletProcessorV2 processor;//for callback
	private final JobletType<?> jobletType;
	private final long id;
	private final Date startedAt;
	private Optional<JobletPackage> jobletPackage;


	public JobletCallable(DatarouterProperties datarouterProperties, JobletNodes jobletNodes,
			JobletService jobletService, JobletFactory jobletFactory, MutableBoolean shutdownRequested,
			JobletProcessorV2 processor, JobletType<?> jobletType, long id){
		this.datarouterProperties = datarouterProperties;
		this.jobletNodes = jobletNodes;
		this.jobletService = jobletService;
		this.jobletFactory = jobletFactory;
		this.shutdownRequested = shutdownRequested;
		this.processor = processor;
		this.jobletType = jobletType;
		this.id = id;
		this.startedAt = new Date();
		this.jobletPackage = Optional.empty();
	}

	@Override
	public Void call(){
		try{
			jobletPackage = dequeueJobletPackage();
			if(!jobletPackage.isPresent()){
				return null;
			}
			JobletRequest jobletRequest = jobletPackage.get().getJobletRequest();
			PhaseTimer timer = jobletRequest.getTimer();
			try{
				jobletRequest.setReservedAt(System.currentTimeMillis());
				jobletNodes.jobletRequest().put(jobletRequest, null);
				processJobletWithStats(jobletPackage.get());
				timer.add("processed");
				jobletService.handleJobletCompletion(jobletRequest);
				timer.add("completed");
			}catch(JobInterruptedException e){
				try{
					jobletService.handleJobletInterruption(jobletRequest);
				}catch(Exception e1){
					logger.error("", e1);
				}
				timer.add("interrupted");
			}catch(Exception e){
				logger.error("", e);
				try{
					jobletService.handleJobletError(jobletRequest, e, jobletRequest.getClass().getSimpleName());
					timer.add("failed");
				}catch(Exception lastResort){
					logger.error("", lastResort);
					timer.add("couldn't mark failed");
				}
			}
			return null;
		}finally{
			processor.onCompletion(id);
		}
	}


	private final Optional<JobletPackage> dequeueJobletPackage(){
		String reservedBy = getReservedByString();
		Optional<JobletRequest> jobletRequest = jobletService.getJobletRequestForProcessing(jobletType, reservedBy);
		if(!jobletRequest.isPresent()){
			return Optional.empty();
		}
		jobletRequest.get().setShutdownRequested(shutdownRequested);
		return jobletRequest.map(jobletService::getJobletPackageForJobletRequest);
	}

	private String getReservedByString(){
		return datarouterProperties.getServerName()
				+ "_" + DrDateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(System.currentTimeMillis())
				+ "_" + Thread.currentThread().getId()
				+ "_" + id;
	}

	private void processJobletWithStats(JobletPackage jobletPackage){
		Joblet<?> joblet = jobletFactory.createForPackage(jobletPackage);
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		long startTimeMs = System.currentTimeMillis();
		joblet.process();

		//counters
		JobletCounters.incNumJobletsProcessed();
		JobletCounters.incNumJobletsProcessed(jobletType.getPersistentString());
		int numItemsProcessed = Math.max(1, jobletRequest.getNumItems());
		JobletCounters.incItemsProcessed(jobletType.getPersistentString(), numItemsProcessed);
		int numTasksProcessed = Math.max(1, jobletRequest.getNumTasks());
		JobletCounters.incTasksProcessed(jobletType.getPersistentString(), numTasksProcessed);
		long endTimeMs = System.currentTimeMillis();
		long durationMs = endTimeMs - startTimeMs;
		String itemsPerSecond = DrNumberFormatter.format((double)jobletRequest.getNumItems() / ((double)durationMs
				/ (double)1000), 1);
		String tasksPerSecond = DrNumberFormatter.format((double)jobletRequest.getNumTasks() / ((double)durationMs
				/ (double)1000), 1);

		//logging
		String typeAndQueue = jobletType.getPersistentString();
		if(DrStringTool.notEmpty(jobletRequest.getQueueId())){
			typeAndQueue += " " + jobletRequest.getQueueId();
		}
		logger.info("Finished " + typeAndQueue
				+ " with " + jobletRequest.getNumItems() + " items"
				+ " and " + jobletRequest.getNumTasks() + " tasks"
				+ " in " + DrNumberFormatter.addCommas(durationMs)+"ms"
				+ " at "+itemsPerSecond+" items/sec"
				+ " and "+tasksPerSecond+" tasks/sec");
	}

	public RunningJoblet getRunningJoblet(){
		return new RunningJoblet(jobletType, id, startedAt, jobletPackage);
	}

}
