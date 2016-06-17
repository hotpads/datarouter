package com.hotpads.joblet.execute;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.job.trigger.JobSettings;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.databean.JobletData;
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
		private Datarouter datarouter;
		@Inject
		private JobletThrottle jobletThrottle;
		@Inject
		private JobletSettings jobletSettings;
		@Inject
		private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;

		public ParallelJobletProcessor create(JobletType<?> jobletType){
			return new ParallelJobletProcessor(jobletType, jobSettings, jobletService, datarouter, jobletThrottle,
					jobletSettings, jobletExecutorThreadPoolFactory);
		}

	}

	public static Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//intentionally shared across any instances that might exist
	private static final MutableBoolean interrupted = new MutableBoolean(false);
	private boolean stop = false;

	private final JobletType<?> jobletType;
	private Thread workerThread;
	private JobletScheduler jobletScheduler;
	private JobletExecutorThreadPool threadPool;

	private final JobSettings jobSettings;
	private final JobletService jobletService;
	private final Datarouter datarouter;
	private final JobletThrottle jobletThrottle;
	private final JobletSettings jobletSettings;

	private ParallelJobletProcessor(JobletType<?> jobletType, JobSettings jobSettings, JobletService jobletService,
			Datarouter datarouter, JobletThrottle jobletThrottle, JobletSettings jobletSettings,
			JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory) {
		this.jobletType = jobletType;
		this.jobSettings = jobSettings;
		this.jobletService = jobletService;
		this.datarouter = datarouter;
		this.jobletThrottle = jobletThrottle;
		this.jobletSettings = jobletSettings;
		this.threadPool = jobletExecutorThreadPoolFactory.create(0, jobletType);
		this.jobletScheduler = new JobletSchedulerImp(threadPool);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "," + jobletType + ",numThreads:"
				+ getNumThreads() + "]";
	}

	private boolean shouldRun() {
		if(jobSettings.getProcessJobs().getValue()
			&& jobletSettings.getRunJoblets().getValue()
			&& getNumThreads() > 0
			&& !stop
			&& !interrupted.get()){
			return true;
		}

		return false;
	}

	public static void interruptAllJoblets(){
		interrupted.set(true);
	}

	public boolean isAwake() {
		return workerThread != null;
	}

	public void wakeUp(){
		if(isAwake()){
			return;
		}
		startThread();
	}

	private void startThread(){
		workerThread = new Thread(null, buildWorker(), jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		workerThread.start();
	}

	private void endThread(){
		workerThread = null;
	}

	private Runnable buildWorker(){
		return new Runnable(){
			@Override
			public void run() {
				processJobletsInParallel();
				endThread();
			}
		};
	}

	private void processJobletsInParallel(){
		int counter = 0;
		int numThreads = getNumThreads();
		threadPool.resize(numThreads);
		while(shouldRun()){
			if(interrupted.get()){
				return;
			}
			threadPool.resize(getNumThreads());
			PhaseTimer pt = new PhaseTimer();
			jobletScheduler.blockUntilReadyForNewJoblet();
			JobletPackage jobletPackage = getJoblet(counter++);
			pt.add("acquired");
			JobletRequest joblet = jobletPackage.getJoblet();
			if(joblet == null){
				return;
			}
			joblet.setTimer(pt);
			jobletScheduler.submitJoblet(jobletPackage);
		}

	}

	private int getNumThreads(){
		Integer numThreads = jobletSettings.getThreadCountForJobletType(jobletType);
		return numThreads == null ? 0 : numThreads;
	}

	public void stop() {
		stop  = true;
	}

	private String getReservedByString(int counter){
		return datarouter.getServerName() + "_" + DrDateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(
				System.currentTimeMillis()) + "_" + Thread.currentThread().getId() + "_" + counter;
	}

	public JobletScheduler getJobletScheduler(){
		return jobletScheduler;
	}

	public Collection<JobletExecutorThread> getRunningJobletExecutorThreads() {
		return threadPool.getRunningJobletExecutorThreads();
	}

	public Collection<JobletExecutorThread> getWaitingJobletExecutorThreads(){
		return threadPool.getWaitingJobletExecutorThreads();
	}

	private final JobletPackage getJoblet(int counter){
		String reservedBy = getReservedByString(counter);
		JobletRequest jobletRequest = null;
		jobletThrottle.acquirePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
		try{
			jobletRequest = jobletService.getJobletRequestForProcessing(jobletType, reservedBy,
					RUNNING_JOBLET_TIMEOUT_MS);
		}catch(Exception e){
			logger.warn("", e);
		}
		JobletData jobletData = null;
		if(jobletRequest != null){
			jobletRequest.setInterrupted(interrupted);
			jobletData = jobletService.getJobletData(jobletRequest);
		}else{
			jobletThrottle.releasePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
		}
		return new JobletPackage(jobletRequest, jobletData);
	}

}
