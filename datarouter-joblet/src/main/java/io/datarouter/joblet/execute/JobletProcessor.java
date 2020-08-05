/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.execute;

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

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.dto.RunningJoblet;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.mutable.MutableBoolean;
import io.datarouter.util.number.RandomTool;

public class JobletProcessor{
	private static final Logger logger = LoggerFactory.getLogger(JobletProcessor.class);

	private static final Duration SLEEP_TIME_WHEN_DISABLED = Duration.ofSeconds(5);
	private static final Duration WAKEUP_PERIOD = Duration.ofSeconds(5);
	private static final Duration MAX_EXEC_BACKOFF_TIME = Duration.ofSeconds(1);
	private static final Duration MAX_WAIT_FOR_EXECUTOR = Duration.ofSeconds(5);
	private static final Duration MAX_WAIT_FOR_SHUTDOWN = Duration.ofSeconds(5);
	private static final Duration SLEEP_TIME_AFTER_EXCEPTION = Duration.ofSeconds(5);
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	//injectable
	private final DatarouterJobletSettingRoot jobletSettings;
	private final JobletRequestQueueManager jobletRequestQueueManager;
	private final JobletCallableFactory jobletCallableFactory;
	private final DatarouterJobletCounters datarouterJobletCounters;
	private final JobletService jobletService;
	//not injectable
	private final AtomicLong idGenerator;
	private final JobletType<?> jobletType;
	private final MutableBoolean shutdownRequested;
	private final ThreadPoolExecutor exec;
	private final Map<Long,JobletCallable> jobletCallableById;
	private final Map<Long,Future<Void>> jobletFutureById;
	private final Thread driverThread;

	public JobletProcessor(
			DatarouterJobletSettingRoot jobletSettings,
			JobletRequestQueueManager jobletRequestQueueManager,
			JobletCallableFactory jobletCallableFactory,
			DatarouterJobletCounters datarouterJobletCounters,
			JobletService jobletService,
			AtomicLong idGenerator,
			JobletType<?> jobletType){
		this.jobletSettings = jobletSettings;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
		this.jobletCallableFactory = jobletCallableFactory;
		this.datarouterJobletCounters = datarouterJobletCounters;
		this.jobletService = jobletService;

		this.idGenerator = idGenerator;
		this.jobletType = jobletType;
		//create a separate shutdownRequested for each processor so we can disable them independently
		this.shutdownRequested = new MutableBoolean(false);
		ThreadFactory threadFactory = new NamedThreadFactory("joblet-" + jobletType.getPersistentString(), true);
		this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<>(),
				threadFactory);
		this.jobletCallableById = new ConcurrentHashMap<>();
		this.jobletFutureById = new ConcurrentHashMap<>();
		this.driverThread = new Thread(null, this::run, jobletType.getPersistentString()
				+ " JobletProcessor worker thread");
		driverThread.start();
	}

	public void requestShutdown(){
		shutdownRequested.set(true);
		driverThread.interrupt();
		ExecutorServiceTool.shutdown(exec, MAX_WAIT_FOR_SHUTDOWN);
	}

	public void killThread(long threadId){
		Optional.ofNullable(jobletFutureById.get(threadId))
				.map(future -> future.cancel(true));
	}

	/*----------------- private --------------------*/

	private boolean shouldRun(int numThreads){
		return jobletSettings.runJoblets.get()
				&& numThreads > 0;
	}

	//this method must continue indefinitely, so be sure to catch all exceptions
	public void run(){
		while(true){
			try{
				if(Thread.interrupted()){
					logger.warn("joblet thread shutting down for type=" + jobletType);
					return;
				}
				int numThreads = getThreadCount();
				if(!shouldRun(numThreads)){
					sleepABit(SLEEP_TIME_WHEN_DISABLED);
					continue;
				}
				if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
					sleepARandomBit(WAKEUP_PERIOD);
					continue;
				}
				tryEnqueueJobletCallable(numThreads);
			}catch(Exception e){//catch everything; don't let the loop break
				logger.error("", e);
				try{
					sleepABit(SLEEP_TIME_AFTER_EXCEPTION);
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
	private void tryEnqueueJobletCallable(int numThreads){
		exec.setMaximumPoolSize(numThreads);//must be > 0
		long startMs = System.currentTimeMillis();
		long backoffMs = 10L;
		while(System.currentTimeMillis() - startMs < MAX_WAIT_FOR_EXECUTOR.toMillis()){
			if(Thread.currentThread().isInterrupted()){
				break;
			}
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
				datarouterJobletCounters.rejectedCallable(jobletType);
				sleepABit(Duration.ofMillis(backoffMs));
				backoffMs = Math.min(2 * backoffMs, MAX_EXEC_BACKOFF_TIME.toMillis());
			}
		}
	}

	//sleep between .5 and 1.5x the requested value
	private void sleepARandomBit(Duration duration){
		long requestedMs = duration.toMillis();
		long randomness = RandomTool.nextPositiveLong(requestedMs);
		long actualMs = requestedMs / 2 + randomness;
		logger.debug("{} sleeping {} ms", jobletType.getPersistentString(), actualMs);
		sleepABit(Duration.ofMillis(actualMs));
	}

	private void sleepABit(Duration duration){
		try{
			Thread.sleep(duration.toMillis());
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
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

	public int getThreadCount(){
		return jobletService.getThreadCountInfoForThisInstance(jobletType).effectiveLimit;
	}

	/*------------ Object methods ----------------*/

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
