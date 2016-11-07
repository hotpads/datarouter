package com.hotpads.joblet.execute.v2;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
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

	private static final Duration SLEEP_TIME_WHEN_DISABLED = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_WHEN_NO_WORK = Duration.ofSeconds(1);
	private static final Duration MAX_EXEC_BACKOFF_TIME = Duration.ofSeconds(1);
	private static final Duration MAX_WAIT_FOR_EXECUTOR = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_AFTER_EXCEPTION = Duration.ofSeconds(5);
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
		this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), threadFactory);
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
				&& jobletSettings.runJoblets.getValue()
				&& jobletSettings.getThreadCountForJobletType(jobletType) > 0;
	}

	//this method must continue indefinitely, so be sure to catch all exceptions
	@Override
	public void run(){
		while(true){
			try{
				if(!shouldRun()){
					sleepABit(SLEEP_TIME_WHEN_DISABLED, "shouldRun=false");
					continue;
				}
				if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
					sleepABit(SLEEP_TIME_WHEN_NO_WORK, "shouldCheckAnyQueues=false");
					continue;
				}
				tryEnqueueJobletCallable();
			}catch(Exception e){//catch everything; don't let the loop break
				logger.error("", e);
				try{
					sleepABit(SLEEP_TIME_AFTER_EXCEPTION, "exception acquiring joblet");
				}catch(Exception problemSleeping){
					logger.error("uh oh, problem sleeping", problemSleeping);
				}
			}
		}
	}

	/**
	 * aggressively try to add to the queue until MAX_EXEC_BACKOFF_TIME
	 *
	 * TODO make a BlockingRejectedExecutionHandler?
	 *
	 * @return boolean true if we were able to create a callable or didn't need to; false if the exec was too busy
	 */
	private void tryEnqueueJobletCallable(){
		int numThreads = jobletSettings.getThreadCountForJobletType(jobletType);
		exec.setMaximumPoolSize(numThreads);//must be >0
		long startMs = System.currentTimeMillis();
		long backoffMs = 1L;
		while(System.currentTimeMillis() - startMs < MAX_WAIT_FOR_EXECUTOR.toMillis()){
			if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
				return;
			}
			try{
				JobletCallable jobletCallable = jobletCallableFactory.create(shutdownRequested, jobletType, ++counter);
				exec.submit(jobletCallable);
			}catch(RejectedExecutionException ree){
//				logger.warn("{} #{} rejected, backing off {}ms", jobletType, counter, backoffMs);
				sleepABit(Duration.ofMillis(backoffMs), "executor full");
				backoffMs = Math.min(2 * backoffMs, MAX_EXEC_BACKOFF_TIME.toMillis());
			}
		}
	}

	//return whether a slot became available
	private boolean waitForExecutorSlot(int numThreads){
		long startMs = System.currentTimeMillis();
		long backoffMs = 1L;
		while(numThreads - exec.getActiveCount() < 1){
			long elapsedMs = System.currentTimeMillis() - startMs;
			if(elapsedMs > MAX_WAIT_FOR_EXECUTOR.toMillis()){
				return false;
			}
			sleepABit(Duration.ofMillis(backoffMs), "executor full");
			backoffMs = Math.min(2 * backoffMs, MAX_EXEC_BACKOFF_TIME.toMillis());
		}
		return true;
	}

	private void sleepABit(Duration duration, String reason){
		logger.debug("sleeping {} since {} for {}", duration, reason, jobletType.getPersistentString());
		try{
			Thread.sleep(duration.toMillis());
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
