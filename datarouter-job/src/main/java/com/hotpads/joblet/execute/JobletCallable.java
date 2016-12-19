package com.hotpads.joblet.execute;

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
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletCallable implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(JobletCallable.class);

	private final DatarouterProperties datarouterProperties;
	private final JobletNodes jobletNodes;
	private final JobletService jobletService;
	private final JobletFactory jobletFactory;
	private final JobletCounters jobletCounters;

	private final MutableBoolean shutdownRequested;
	private final JobletProcessor processor;// for callback
	private final JobletType<?> jobletType;
	private final long id;
	private final Date startedAt;
	private Optional<JobletPackage> jobletPackage;

	public JobletCallable(DatarouterProperties datarouterProperties, JobletNodes jobletNodes,
			JobletService jobletService, JobletFactory jobletFactory, JobletCounters jobletCounters,
			MutableBoolean shutdownRequested, JobletProcessor processor, JobletType<?> jobletType, long id){
		this.datarouterProperties = datarouterProperties;
		this.jobletNodes = jobletNodes;
		this.jobletService = jobletService;
		this.jobletFactory = jobletFactory;
		this.jobletCounters = jobletCounters;
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
			PhaseTimer timer = new PhaseTimer(jobletType.getPersistentString() + "-" + id);
			jobletPackage = dequeueJobletPackage(timer);
			if(!jobletPackage.isPresent()){
				return null;
			}
			JobletRequest jobletRequest = jobletPackage.get().getJobletRequest();
			try{
				jobletRequest.setReservedAt(System.currentTimeMillis());
				jobletNodes.jobletRequest().put(jobletRequest, null);
				timer.add("setReservedAt");
				processJobletWithStats(timer, jobletPackage.get());
				jobletService.handleJobletCompletion(timer, jobletRequest);
			}catch(JobInterruptedException e){
				try{
					jobletService.handleJobletInterruption(timer, jobletRequest);
				}catch(Exception e1){
					logger.error("", e1);
				}
			}catch(Exception e){
				logger.error("", e);
				try{
					jobletService.handleJobletError(timer, jobletRequest, e, jobletRequest.getClass().getSimpleName());
				}catch(Exception lastResort){
					logger.error("", lastResort);
					timer.add("couldn't mark failed");
				}
			}
			logger.info(timer.toString());
			return null;
		}finally{
			processor.onCompletion(id);
		}
	}

	private final Optional<JobletPackage> dequeueJobletPackage(PhaseTimer timer){
		String reservedBy = getReservedByString();
		Optional<JobletRequest> jobletRequest = jobletService.getJobletRequestForProcessing(timer, jobletType,
				reservedBy);
		if(jobletRequest.isPresent()){
			timer.add("dequeued " + jobletRequest.get().getKey());
		}else{
			timer.add("no JobletRequest found");
			return Optional.empty();
		}
		jobletRequest.get().setShutdownRequested(shutdownRequested);
		JobletPackage jobletPackage = jobletService.getJobletPackageForJobletRequest(jobletRequest.get());
		timer.add("getJobletData");
		return Optional.of(jobletPackage);
	}

	private String getReservedByString(){
		return datarouterProperties.getServerName()
				+ "_" + DrDateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(System.currentTimeMillis())
				+ "_" + Thread.currentThread().getId()
				+ "_" + id;
	}

	private void processJobletWithStats(PhaseTimer timer, JobletPackage jobletPackage){
		Joblet<?> joblet = jobletFactory.createForPackage(jobletPackage);
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		long startTimeMs = System.currentTimeMillis();
		joblet.process();

		// counters
		jobletCounters.incNumJobletsProcessed();
		jobletCounters.incNumJobletsProcessed(jobletType);
		int numItemsProcessed = Math.max(1, jobletRequest.getNumItems());
		jobletCounters.incItemsProcessed(jobletType, numItemsProcessed);
		timer.add("processed " + numItemsProcessed + " items");
		int numTasksProcessed = Math.max(1, jobletRequest.getNumTasks());
		jobletCounters.incTasksProcessed(jobletType, numTasksProcessed);
		long endTimeMs = System.currentTimeMillis();
		long durationMs = endTimeMs - startTimeMs;
		String itemsPerSecond = DrNumberFormatter.format((double)jobletRequest.getNumItems() / ((double)durationMs
				/ (double)1000), 1);
		String tasksPerSecond = DrNumberFormatter.format((double)jobletRequest.getNumTasks() / ((double)durationMs
				/ (double)1000), 1);

		// logging
		String typeAndQueue = jobletType.getPersistentString();
		if(DrStringTool.notEmpty(jobletRequest.getQueueId())){
			typeAndQueue += " " + jobletRequest.getQueueId();
		}
		logger.debug("Finished " + typeAndQueue
				+ " with " + jobletRequest.getNumItems() + " items"
				+ " and " + jobletRequest.getNumTasks() + " tasks"
				+ " in " + DrNumberFormatter.addCommas(durationMs) + "ms"
				+ " at " + itemsPerSecond + " items/sec"
				+ " and " + tasksPerSecond + " tasks/sec");
	}

	public RunningJoblet getRunningJoblet(){
		return new RunningJoblet(jobletType, id, startedAt, jobletPackage);
	}

}
