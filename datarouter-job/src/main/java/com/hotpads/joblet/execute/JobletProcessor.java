package com.hotpads.joblet.execute;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.datastructs.MutableBoolean;
import com.hotpads.webappinstance.CachedNumServersAliveOfType;

public class JobletProcessor implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(JobletProcessor.class);

	private static final Duration SLEEP_TIME_WHEN_DISABLED = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_WHEN_NO_WORK = Duration.ofSeconds(1);
	private static final Duration MAX_EXEC_BACKOFF_TIME = Duration.ofSeconds(1);
	private static final Duration MAX_WAIT_FOR_EXECUTOR = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_AFTER_EXCEPTION = Duration.ofSeconds(5);
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//injectable
	private final JobletSettings jobletSettings;
	private final JobletRequestQueueManager jobletRequestQueueManager;
	private final JobletCallableFactory jobletCallableFactory;
	private final JobletCounters jobletCounters;
	private final CachedNumServersAliveOfType cachedNumServersAliveOfType;
	//not injectable
	private final AtomicLong idGenerator;
	private final JobletType<?> jobletType;
	private final MutableBoolean shutdownRequested;
	private final ThreadPoolExecutor exec;
	private final Map<Long,JobletCallable> jobletCallableById;
	private final Map<Long,Future<Void>> jobletFutureById;
	private final Thread driverThread;


	public JobletProcessor(JobletSettings jobletSettings, JobletRequestQueueManager jobletRequestQueueManager,
			JobletCallableFactory jobletCallableFactory, JobletCounters jobletCounters,
			CachedNumServersAliveOfType cachedNumServersAliveOfType, AtomicLong idGenerator, JobletType<?> jobletType){
		this.jobletSettings = jobletSettings;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
		this.jobletCallableFactory = jobletCallableFactory;
		this.jobletCounters = jobletCounters;
		this.cachedNumServersAliveOfType = cachedNumServersAliveOfType;

		this.idGenerator = idGenerator;
		this.jobletType = jobletType;
		//create a separate shutdownRequested for each processor so we can disable them independently
		this.shutdownRequested = new MutableBoolean(false);
		ThreadFactory threadFactory = new NamedThreadFactory(null, "joblet-" + jobletType.getPersistentString(), true);
		this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), threadFactory);
		this.jobletCallableById = new ConcurrentHashMap<>();
		this.jobletFutureById = new ConcurrentHashMap<>();
		this.driverThread = new Thread(null, this::run, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		driverThread.setDaemon(true);
		driverThread.start();
	}

	public void requestShutdown(){
		shutdownRequested.set(true);
	}

	public void killThread(long threadId){
		Optional.ofNullable(jobletFutureById.get(threadId))
				.map(future -> future.cancel(true));
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
	 * aggressively try to add to the queue until MAX_EXEC_BACKOFF_TIME.  this is to keep topping off the queue when
	 * we're processing joblets that finish quickly
	 *
	 * TODO make a BlockingRejectedExecutionHandler?
	 */
	private void tryEnqueueJobletCallable(){
		int numThreads = getThreadCountFromSettings();
		exec.setMaximumPoolSize(numThreads);//must be >0
		long startMs = System.currentTimeMillis();
		long backoffMs = 10L;
		while(System.currentTimeMillis() - startMs < MAX_WAIT_FOR_EXECUTOR.toMillis()){
			if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
				return;//stop trying
			}
			try{
				long id = idGenerator.incrementAndGet();
				JobletCallable jobletCallable = jobletCallableFactory.create(shutdownRequested, this, jobletType, id);
				Future<Void> jobletFuture = exec.submit(jobletCallable);
				jobletCallableById.put(id, jobletCallable);
				jobletFutureById.put(id, jobletFuture);
				return;//return so we loop back immediately
			}catch(RejectedExecutionException ree){
//				logger.warn("{} #{} rejected, backing off {}ms", jobletType, counter, backoffMs);
				jobletCounters.rejectedCallable(jobletType);
				sleepABit(Duration.ofMillis(backoffMs), "executor full");
				backoffMs = Math.min(2 * backoffMs, MAX_EXEC_BACKOFF_TIME.toMillis());
			}
		}
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

	public void onCompletion(Long id){
		jobletCallableById.remove(id);
		jobletFutureById.remove(id);
	}

	public List<RunningJoblet> getRunningJoblets(){
		return jobletCallableById.values().stream()
				.map(JobletCallable::getRunningJoblet)
				.filter(RunningJoblet::hasPayload)
				.collect(Collectors.toList());
	}

	public int getNumRunningJoblets(){
		return jobletCallableById.size();
	}

	public int getThreadCountFromSettings(){
		int numInstancesOfThisType = cachedNumServersAliveOfType.get();
		int clusterLimit = jobletSettings.getClusterThreadCountForJobletType(jobletType);
		int perInstanceClusterLimit = (int)Math.ceil((double)clusterLimit / (double)numInstancesOfThisType);
		int instanceLimit = jobletSettings.getThreadCountForJobletType(jobletType);
		int min = Math.min(perInstanceClusterLimit, instanceLimit);
//		logger.warn(
//				"jobletType={}, numInstancesOfThisType={}, clusterLimit={}, perInstanceClusterLimit={}, instanceLimit={}, min={}",
//				jobletType, numInstancesOfThisType, clusterLimit, perInstanceClusterLimit, instanceLimit, min);
		return min;
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
