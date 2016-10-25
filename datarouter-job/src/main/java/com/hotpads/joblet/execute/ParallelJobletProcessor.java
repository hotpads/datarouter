package com.hotpads.joblet.execute;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.JobletExecutorThreadPool.JobletExecutorThreadPoolFactory;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.datastructs.MutableBoolean;

public class ParallelJobletProcessor{
	private static final Logger logger = LoggerFactory.getLogger(ParallelJobletProcessor.class);

	@Singleton
	public static class ParallelJobletProcessorFactory{
		@Inject
		private JobletService jobletService;
		@Inject
		private DatarouterProperties datarouterProperties;
		@Inject
		private JobletSettings jobletSettings;
		@Inject
		private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;

		public ParallelJobletProcessor create(JobletType<?> jobletType){
			return new ParallelJobletProcessor(datarouterProperties, jobletSettings, jobletService,
					jobletExecutorThreadPoolFactory, jobletType);
		}
	}

	private static final long SLEEP_MS_WHEN_NO_WORK = Duration.ofSeconds(1).toMillis();
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//injected
	private final DatarouterProperties datarouterProperties;
	private final JobletSettings jobletSettings;
	private final JobletService jobletService;
	//not injected
	private final JobletType<?> jobletType;
	private final MutableBoolean shutdownRequested;
	private final JobletExecutorThreadPool workerThreadPool;
	private final Thread driverThread;


	public ParallelJobletProcessor(DatarouterProperties datarouterProperties, JobletSettings jobletSettings,
			JobletService jobletService, JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory,
			JobletType<?> jobletType){
		this.datarouterProperties = datarouterProperties;
		this.jobletSettings = jobletSettings;
		this.jobletService = jobletService;

		this.jobletType = jobletType;
		//create a separate shutdownRequested for each processor so we can disable them independently
		this.shutdownRequested = new MutableBoolean(false);
		this.workerThreadPool = jobletExecutorThreadPoolFactory.create(0, jobletType);
		this.driverThread = new Thread(null, this::fetchJobletsAndAssignToPool, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		driverThread.start();
	}

	public void requestShutdown() {
		shutdownRequested.set(true);
	}

	/*----------------- private --------------------*/

	private boolean shouldRun(){
		return !shutdownRequested.get()
				&& jobletSettings.getRunJoblets().getValue()
				&& jobletSettings.getThreadCountForJobletType(jobletType) > 0;
	}

	//this method must continue indefinitely, so be sure to catch all exceptions
	private void fetchJobletsAndAssignToPool(){
		long counter = 0;
		while(!shutdownRequested.get()){
			try{
				if(!shouldRun()){
					logger.debug("sleeping since shouldRun false for {}", jobletType.getPersistentString());
					sleepABit();
					continue;
				}
				workerThreadPool.resize(jobletSettings.getThreadCountForJobletType(jobletType));
				PhaseTimer timer = new PhaseTimer();
				JobletPackage jobletPackage = getJobletPackage(counter++);
				if(jobletPackage != null){
					timer.add("acquired");
					jobletPackage.getJobletRequest().setTimer(timer);
					assignJobletPackageToThreadPool(jobletPackage);
				}else{
					logger.debug("sleeping since no joblet found for {}", jobletType.getPersistentString());
					sleepABit();
				}
			}catch(Exception e){//catch everything; don't let the loop break
				logger.error("", e);
				try{
					sleepABit();
				}catch(Exception problemSleeping){
					logger.error("uh oh, problem sleeping", problemSleeping);
				}
			}
		}
	}

	private final JobletPackage getJobletPackage(long counter){
		String reservedBy = getReservedByString(counter);
		//TODO pass a later startAtPriority if the first queue(s) are repeatedly empty
		JobletRequest jobletRequest = jobletService.getJobletRequestForProcessing(jobletType, null, reservedBy);
		if(jobletRequest == null){
			return null;
		}
		jobletRequest.setShutdownRequested(shutdownRequested);
		return jobletService.getJobletPackageForJobletRequest(jobletRequest);
	}

	private String getReservedByString(long counter){
		return datarouterProperties.getServerName()
				+ "_" + DrDateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(System.currentTimeMillis())
				+ "_" + Thread.currentThread().getId()
				+ "_" + counter;
	}

	public void assignJobletPackageToThreadPool(JobletPackage jobletPackage){
		Lock lock = workerThreadPool.getJobAssignmentLock();
		lock.lock();
		try{
			while(workerThreadPool.isSaturated()){
				if(!workerThreadPool.getSaturatedCondition().await(1, TimeUnit.SECONDS)){
					workerThreadPool.findAndKillRunawayJoblets();
				}
			}
			workerThreadPool.assignJobletPackage(jobletPackage);
		}catch(InterruptedException e){
			return;
		}finally{
			lock.unlock();
		}
	}

	private void sleepABit(){
		try{
			Thread.sleep(SLEEP_MS_WHEN_NO_WORK);
		}catch(InterruptedException e){
			logger.warn("", e);
			Thread.interrupted();//continue.  we have explicit interrupted handling for terminating
		}
	}

	/*---------------- Object methods ----------------*/

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + jobletType + ", numThreads:" + jobletSettings
				.getThreadCountForJobletType(jobletType) + "]";
	}

	/*---------------- convenience ---------------*/

	public Collection<JobletExecutorThread> getRunningJobletExecutorThreads() {
		return workerThreadPool.getRunningJobletExecutorThreads();
	}

	public Collection<JobletExecutorThread> getWaitingJobletExecutorThreads(){
		return workerThreadPool.getWaitingJobletExecutorThreads();
	}

	/*-------------- get/set -----------------*/

	public JobletType<?> getJobletType(){
		return jobletType;
	}

}
