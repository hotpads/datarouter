package com.hotpads.joblet.execute.v2;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletCallable implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(JobletCallable.class);

	private final DatarouterProperties datarouterProperties;
	private final JobletNodes jobletNodes;
	private final JobletService jobletService;
	private final JobletFactory jobletFactory;
	private final MutableBoolean shutdownRequested;
	private final JobletType<?> jobletType;
	private final long id;


	public JobletCallable(DatarouterProperties datarouterProperties, JobletNodes jobletNodes,
			JobletService jobletService, JobletFactory jobletFactory, MutableBoolean shutdownRequested,
			JobletType<?> jobletType, long id){
		this.datarouterProperties = datarouterProperties;
		this.jobletNodes = jobletNodes;
		this.jobletService = jobletService;
		this.jobletFactory = jobletFactory;
		this.shutdownRequested = shutdownRequested;
		this.jobletType = jobletType;
		this.id = id;
	}

	@Override
	public Void call(){
		Optional<JobletPackage> jobletPackage = dequeueJobletPackage();
		if(!jobletPackage.isPresent()){
			return null;
		}
		jobletPackage.get().getJobletRequest().setReservedAt(System.currentTimeMillis());
		jobletNodes.jobletRequest().put(jobletPackage.get().getJobletRequest(), null);
		processJobletWithStats(jobletPackage.get());
		return null;
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
}
