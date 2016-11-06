package com.hotpads.joblet.execute.v2;

import java.time.Duration;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletProcessorV2 implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(JobletProcessorV2.class);

	private static final long SLEEP_MS_WHEN_NO_WORK = Duration.ofSeconds(1).toMillis();
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//injected
	private final JobletSettings jobletSettings;
	private final JobletRequestQueueManager jobletRequestQueueManager;
	private final JobletCallableFactory jobletCallableFactory;
	//not injected
	private final JobletType<?> jobletType;
	private final MutableBoolean shutdownRequested;
	private final ThreadPoolExecutor exec;
	private final Thread driverThread;
	private long counter;


	public JobletProcessorV2(JobletSettings jobletSettings, JobletRequestQueueManager jobletRequestQueueManager,
			JobletCallableFactory jobletCallableFactory, JobletType<?> jobletType){
		this.jobletSettings = jobletSettings;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
		this.jobletCallableFactory = jobletCallableFactory;

		this.jobletType = jobletType;
		//create a separate shutdownRequested for each processor so we can disable them independently
		this.shutdownRequested = new MutableBoolean(false);
		ThreadFactory threadFactory = new NamedThreadFactory(null, "joblet-" + jobletType.getPersistentString(), true);
		this.exec = new ThreadPoolExecutor(0, 0, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
				threadFactory);
		this.driverThread = new Thread(null, this::run, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		this.counter = 0;
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
	@Override
	public void run(){
		while(!shutdownRequested.get()){
			try{
				exec.setMaximumPoolSize(jobletSettings.getThreadCountForJobletType(jobletType));
				if(!shouldRun()){
					sleepABit("shouldRun=false");
					continue;
				}
				if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
					sleepABit("shouldCheckAnyQueues =false");
					continue;
				}
				int numSlotsAvailable = exec.getMaximumPoolSize() - exec.getActiveCount();
				if(numSlotsAvailable < 1){
					sleepABit("no slots available");
					continue;
				}
				for(int i = 0; i < numSlotsAvailable; ++i){
					JobletCallable jobletCallable = jobletCallableFactory.create(shutdownRequested, jobletType,
							++counter);
					exec.submit(jobletCallable);
				}
			}catch(Exception e){//catch everything; don't let the loop break
				logger.error("", e);
				try{
					sleepABit("exception acquiring joblet");
				}catch(Exception problemSleeping){
					logger.error("uh oh, problem sleeping", problemSleeping);
				}
			}
		}
	}

	private void sleepABit(String reason){
		logger.debug("sleeping since {} for {}", reason, jobletType.getPersistentString());
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

	/*-------------- get/set -----------------*/

	public JobletType<?> getJobletType(){
		return jobletType;
	}

}
