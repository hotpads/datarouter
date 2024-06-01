/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.joblet.JobletCounters;
import io.datarouter.joblet.dto.RunningJoblet;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.mutable.MutableBoolean;
import io.datarouter.util.number.NumberFormatter;

public class JobletProcessor{
	private static final Logger logger = LoggerFactory.getLogger(JobletProcessor.class);

	// sleep durations
	private static final Duration SLEEP_WHEN_DISABLED = Duration.ofSeconds(10);
	private static final Duration SLEEP_AFTER_RECENT_QUEUE_MISSES = Duration.ofSeconds(1);
	private static final Duration SLEEP_AFTER_REJECTED_EXECUTION = Duration.ofMillis(100);
	private static final Duration SLEEP_AFTER_EXCEPTION = Duration.ofSeconds(5);

	private static final Duration MAX_WAIT_FOR_SHUTDOWN = Duration.ofSeconds(5);
	public static final Long RUNNING_JOBLET_TIMEOUT_MS = 1000L * 60 * 10;  //10 minutes

	// injectable
	private final DatarouterJobletSettingRoot jobletSettings;
	private final JobletRequestQueueManager jobletRequestQueueManager;
	private final JobletCallableFactory jobletCallableFactory;
	private final JobletService jobletService;
	// not injectable
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
			JobletService jobletService,
			AtomicLong idGenerator,
			JobletType<?> jobletType){
		this.jobletSettings = jobletSettings;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
		this.jobletCallableFactory = jobletCallableFactory;
		this.jobletService = jobletService;

		this.idGenerator = idGenerator;
		this.jobletType = jobletType;
		// Create a separate shutdownRequested for each processor so we can disable them independently.
		this.shutdownRequested = new MutableBoolean(false);
		ThreadFactory threadFactory = new NamedThreadFactory("joblet-" + jobletType.getPersistentString(), true);
		this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new SynchronousQueue<>(),
				threadFactory);
		this.jobletCallableById = new ConcurrentHashMap<>();
		this.jobletFutureById = new ConcurrentHashMap<>();
		this.driverThread = new Thread(
				null,
				this::run,
				jobletType.getPersistentString() + " JobletProcessor worker thread");
		driverThread.start();
	}

	public void requestShutdown(){
		shutdownRequested.set(true);
		driverThread.interrupt();
		ExecutorServiceTool.shutdown(exec, MAX_WAIT_FOR_SHUTDOWN);
	}

	/**
	 * @return true is the thread was found
	 */
	public boolean killThread(long threadId){
		return Optional.ofNullable(jobletFutureById.get(threadId))
				.map(future -> future.cancel(true))
				.isPresent();
	}

	/*----------------- private --------------------*/

	private boolean shouldRun(int numThreads){
		return jobletSettings.runJoblets.get()
				&& numThreads > 0;
	}

	// This method must continue indefinitely, so be sure to catch all exceptions.
	public void run(){
		while(true){
			try{
				if(Thread.interrupted()){
					logger.warn("joblet thread shutting down for type={}", jobletType);
					return;
				}
				int numThreads = getThreadCount();
				if(!shouldRun(numThreads)){
					// Don't poll the queues as often when disabled.
					sleepABit("disabled", SLEEP_WHEN_DISABLED);
					continue;
				}
				if(!jobletRequestQueueManager.shouldCheckAnyQueues(jobletType)){
					// A previous task recorded that all queues were empty within the polling period.
					sleepABit("recentQueueMisses", SLEEP_AFTER_RECENT_QUEUE_MISSES);
					continue;
				}
				// Must be > 0.  Call only when shouldRun=true.
				exec.setMaximumPoolSize(numThreads);
				tryEnqueueJobletCallable();
			}catch(Throwable e){//catch everything; don't let the loop break
				logger.error("", e);
				try{
					// Avoid flooding the logs and other things if there's a recurring error.
					sleepABit("exception", SLEEP_AFTER_EXCEPTION);
				}catch(Exception problemSleeping){
					logger.error("uh oh, problem sleeping", problemSleeping);
				}
			}
		}
	}

	private void tryEnqueueJobletCallable(){
		try{
			long id = idGenerator.incrementAndGet();
			JobletCallable jobletCallable = jobletCallableFactory.create(shutdownRequested, this, jobletType, id);
			Future<Void> jobletFuture = exec.submit(jobletCallable);
			jobletCallableById.put(id, jobletCallable);
			jobletFutureById.put(id, jobletFuture);
			// Return so we loop back immediately.
			return;
		}catch(RejectedExecutionException ree){
			JobletCounters.rejectedCallable(jobletType);
			// The executor is maxed out.  Trying to feed it too quickly could be wasteful.
			sleepABit("rejectedExecution", SLEEP_AFTER_REJECTED_EXECUTION);
		}
	}

	private void sleepABit(String reason, Duration duration){
		logger.debug("sleeping {}", new KvString()
				.add("type", jobletType.getPersistentString())
				.add("millis", duration.toMillis(), NumberFormatter::addCommas)
				.add("reason", reason));
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
				.toList();
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
