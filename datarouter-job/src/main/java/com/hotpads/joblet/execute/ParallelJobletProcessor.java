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
import com.hotpads.job.trigger.JobSettings;
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
		private JobSettings jobSettings;
		@Inject
		private JobletService jobletService;
		@Inject
		private DatarouterProperties datarouterProperties;
		@Inject
		private JobletThrottle jobletThrottle;
		@Inject
		private JobletSettings jobletSettings;
		@Inject
		private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;

		public ParallelJobletProcessor create(JobletType<?> jobletType){
			return new ParallelJobletProcessor(datarouterProperties, jobletType, jobSettings, jobletSettings,
					jobletService, jobletThrottle, jobletExecutorThreadPoolFactory);
		}
	}

	public static final long SLEEP_MS_WHEN_NO_WORK = Duration.ofSeconds(1).toMillis();
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//intentionally shared across any instances that might exist
	private static final MutableBoolean shutdownRequested = new MutableBoolean(false);

	//injected
	private final DatarouterProperties datarouterProperties;
	private final JobletType<?> jobletType;
	private final JobSettings jobSettings;
	private final JobletSettings jobletSettings;
	private final JobletService jobletService;
	private final JobletThrottle jobletThrottle;
	//not injected
	private final JobletExecutorThreadPool workerThreadPool;
	private final Thread driverThread;


	public ParallelJobletProcessor(DatarouterProperties datarouterProperties, JobletType<?> jobletType,
			JobSettings jobSettings, JobletSettings jobletSettings, JobletService jobletService,
			JobletThrottle jobletThrottle, JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory){
		this.datarouterProperties = datarouterProperties;
		this.jobletType = jobletType;
		this.jobSettings = jobSettings;
		this.jobletSettings = jobletSettings;
		this.jobletService = jobletService;
		this.jobletThrottle = jobletThrottle;

		this.workerThreadPool = jobletExecutorThreadPoolFactory.create(0, jobletType);
		this.driverThread = new Thread(null, this::processJobletsInParallel, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		driverThread.start();
	}

	public void requestShutdown() {
		shutdownRequested.set(true);;
	}

	/*----------------- private --------------------*/

	private boolean shouldRun() {
		return jobSettings.getProcessJobs().getValue()
				&& jobletSettings.getRunJoblets().getValue()
				&& jobletSettings.getThreadCountForJobletType(jobletType) > 0
				&& !shutdownRequested.get();
	}

	private void processJobletsInParallel(){
		int counter = 0;
		while(!shutdownRequested.get()){
			if(!shouldRun()){
				sleepABit();
				continue;
			}
			workerThreadPool.resize(jobletSettings.getThreadCountForJobletType(jobletType));
			PhaseTimer timer = new PhaseTimer();
			JobletPackage jobletPackage = getJobletPackage(counter++);
			timer.add("acquired");
			if(jobletPackage != null){
				jobletPackage.getJobletRequest().setTimer(timer);
				assignJobletPackageToThreadPool(jobletPackage);
			}else{
				sleepABit();
			}
		}
	}

	private final JobletPackage getJobletPackage(int counter){
		String reservedBy = getReservedByString(counter);
		JobletRequest jobletRequest = null;
		jobletThrottle.acquirePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
		try{
			jobletRequest = jobletService.getJobletRequestForProcessing(jobletType, reservedBy);
		}catch(Exception e){
			logger.warn("", e);
		}
		JobletPackage jobletPackage = null;
		if(jobletRequest != null){
			jobletRequest.setShutdownRequested(shutdownRequested);
			jobletPackage = jobletService.getJobletPackageForJobletRequest(jobletRequest);
		}else{
			jobletThrottle.releasePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
		}
		return jobletPackage;
	}

	private String getReservedByString(int counter){
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
		return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "," + jobletType + ",numThreads:"
				+ jobletSettings.getThreadCountForJobletType(jobletType) + "]";
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
