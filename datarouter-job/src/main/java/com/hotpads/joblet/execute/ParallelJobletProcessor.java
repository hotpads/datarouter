package com.hotpads.joblet.execute;

import java.time.Duration;
import java.util.Collection;

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
			return new ParallelJobletProcessor(jobletType, jobSettings, jobletService, datarouterProperties,
					jobletThrottle, jobletSettings, jobletExecutorThreadPoolFactory);
		}
	}

	public static final long SLEEP_MS_WHEN_NO_WORK = Duration.ofSeconds(1).toMillis();
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//intentionally shared across any instances that might exist
	private static final MutableBoolean interrupted = new MutableBoolean(false);
	private volatile boolean shutdownRequested = false;

	private final JobletType<?> jobletType;
	private Thread workerThread;
	private JobletScheduler jobletScheduler;
	private JobletExecutorThreadPool threadPool;

	private final JobSettings jobSettings;
	private final JobletService jobletService;
	private final DatarouterProperties datarouterProperties;
	private final JobletThrottle jobletThrottle;
	private final JobletSettings jobletSettings;

	private ParallelJobletProcessor(JobletType<?> jobletType, JobSettings jobSettings, JobletService jobletService,
			DatarouterProperties datarouterProperties, JobletThrottle jobletThrottle, JobletSettings jobletSettings,
			JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory) {
		this.jobletType = jobletType;
		this.jobSettings = jobSettings;
		this.jobletService = jobletService;
		this.datarouterProperties = datarouterProperties;
		this.jobletThrottle = jobletThrottle;
		this.jobletSettings = jobletSettings;
		this.threadPool = jobletExecutorThreadPoolFactory.create(0, jobletType);
		this.jobletScheduler = new JobletSchedulerImp(threadPool);
		this.workerThread = new Thread(null, this::processJobletsInParallel, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		workerThread.start();
	}

	public static void interruptAllJoblets(){
		interrupted.set(true);
	}

	public void requestShutdown() {
		shutdownRequested  = true;
	}

	/*----------------- private --------------------*/

	private boolean shouldRun() {
		return jobSettings.getProcessJobs().getValue()
				&& jobletSettings.getRunJoblets().getValue()
				&& jobletSettings.getThreadCountForJobletType(jobletType) > 0
				&& !shutdownRequested
				&& !interrupted.get();
	}

	private void processJobletsInParallel(){
		int counter = 0;
		while(true){
			if(interrupted.get()){
				return;
			}
			if(!shouldRun()){
				sleepABit();
			}
			threadPool.resize(jobletSettings.getThreadCountForJobletType(jobletType));
			PhaseTimer timer = new PhaseTimer();
			jobletScheduler.blockUntilReadyForNewJoblet();
			JobletPackage jobletPackage = getJobletPackage(counter++);
			timer.add("acquired");
			if(jobletPackage != null){
				jobletPackage.getJoblet().setTimer(timer);
				jobletScheduler.submitJobletPackage(jobletPackage);
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
			jobletRequest.setInterrupted(interrupted);
			jobletPackage = jobletService.getJobletPackageForJobletRequest(jobletRequest);
		}else{
			jobletThrottle.releasePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
		}
		return jobletPackage;
	}

	private String getReservedByString(int counter){
		return datarouterProperties.getServerName() + "_" + DrDateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(
				System.currentTimeMillis()) + "_" + Thread.currentThread().getId() + "_" + counter;
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
		return threadPool.getRunningJobletExecutorThreads();
	}

	public Collection<JobletExecutorThread> getWaitingJobletExecutorThreads(){
		return threadPool.getWaitingJobletExecutorThreads();
	}

	/*-------------- get/set -----------------*/

	public JobletType<?> getJobletType(){
		return jobletType;
	}

	public JobletScheduler getJobletScheduler(){
		return jobletScheduler;
	}

}
