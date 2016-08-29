package com.hotpads.joblet.execute;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.job.JobInterruptedException;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.util.core.profile.PhaseTimer;

public class JobletExecutorThread extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(JobletExecutorThread.class);

	@Singleton
	public static class JobletExecutorThreadFactory{
		@Inject
		private JobletTypeFactory jobletTypeFactory;
		@Inject
		private JobletFactory jobletFactory;
		@Inject
		private JobletNodes jobletNodes;
		@Inject
		private JobletService jobletService;

		public JobletExecutorThread create(JobletExecutorThreadPool jobletExecutorThreadPool, ThreadGroup threadGroup){
			return new JobletExecutorThread(jobletExecutorThreadPool, threadGroup, jobletTypeFactory, jobletFactory,
					jobletNodes, jobletService);
		}
	}

	private final JobletExecutorThreadPool jobletExecutorThreadPool;
	private final String jobletName;

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletFactory jobletFactory;
	private final JobletNodes jobletNodes;
	private final JobletService jobletService;

	private boolean shutdown = false;
	private ReentrantLock workLock = new ReentrantLock();
	private Condition hasWorkToBeDone = workLock.newCondition();
	private JobletPackage jobletPackage;
	private Long processingStartTime;
	private long totalRunTime = 0;
	private long completedTasks = 0;
	private long semaphoreWaitTime = 0;

	private JobletExecutorThread(JobletExecutorThreadPool jobletExecutorThreadPool, ThreadGroup threadGroup,
			JobletTypeFactory jobletTypeFactory, JobletFactory jobletFactory, JobletNodes jobletNodes,
			JobletService jobletService){
		super(threadGroup, threadGroup.getName() + " - idle");
		this.jobletExecutorThreadPool = jobletExecutorThreadPool;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletFactory = jobletFactory;
		this.jobletName = threadGroup.getName();
		this.jobletNodes = jobletNodes;
		this.jobletService = jobletService;
	}

	public void submitJoblet(JobletPackage jobletPackage){
		workLock.lock();
		try{
			this.jobletPackage = jobletPackage;
			// processingStartTime = System.currentTimeMillis();
			hasWorkToBeDone.signal();
		}finally{
			workLock.unlock();
		}
	}

	@Override
	public void run() {
		while(!shutdown){
			workLock.lock();
			try{
				while(jobletPackage == null){
					hasWorkToBeDone.await();
				}
				Long requestPermitTime = System.currentTimeMillis();
				// JobletThrottle.acquirePermit();
				semaphoreWaitTime = System.currentTimeMillis() - requestPermitTime;
				// JobletThrottle.acquirePermits(jobletPackage.getJoblet().getType().getCpuPermits(),
				// jobletPackage.getJoblet().getType().getMemoryPermits());
				jobletPackage.getJobletRequest().setReservedAt(System.currentTimeMillis());
				jobletNodes.jobletRequest().put(jobletPackage.getJobletRequest(), null);
				setName(jobletName + " - working");
				PhaseTimer timer = new PhaseTimer();
				processingStartTime = System.currentTimeMillis();
				executeJoblet();
				timer.add("done");
				completedTasks++;
				totalRunTime += System.currentTimeMillis() - processingStartTime;
			}catch(InterruptedException e){
				logger.warn(e.toString());
				shutdown = true;
				JobletType<?> jobletType = jobletTypeFactory.fromJobletPackage(jobletPackage);
				return;
			}catch(Throwable t){
				logger.warn("", t);
			}finally{
				// JobletThrottle.releasePermit();
				JobletType<?> jobletType = jobletTypeFactory.fromJobletPackage(jobletPackage);
				jobletPackage = null;
				processingStartTime = null;
				setName(jobletName + " - idle");
				workLock.unlock();
				recycleThread();
			}
		}
	}

	public Long getRunningTime(){
		Long processingStartTime = this.processingStartTime;
		if(processingStartTime == null){
			return null;
		}
		return System.currentTimeMillis() - processingStartTime;
	}

	private void executeJoblet(){
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		PhaseTimer pt = jobletRequest.getTimer();
		pt.add("waited for processing");
		try{
			runJoblet(jobletPackage);
			pt.add("processed");
			jobletService.handleJobletCompletion(jobletRequest);
			pt.add("completed");
		}catch(JobInterruptedException e){
			try{
				jobletService.handleJobletInterruption(jobletRequest);
			}catch(Exception e1){
				logger.error("", e1);
			}
			pt.add("interrupted");
		}catch(Exception e){
			logger.error("", e);
			try{
				jobletService.handleJobletError(jobletRequest, e, jobletRequest.getClass().getSimpleName());
				pt.add("failed");
			}catch(Exception lastResort){
				logger.error("", lastResort);
				pt.add("couldn't mark failed");
			}
		}
	}

	private final void runJoblet(JobletPackage jobletPackage) throws JobInterruptedException{
		Joblet<?> joblet = jobletFactory.createForPackage(jobletPackage);
		JobletType<?> jobletType = jobletTypeFactory.fromJobletPackage(jobletPackage);
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
				+ " and "+tasksPerSecond+" tasks/sec"
				+ " after waiting " + semaphoreWaitTime+"ms");
	}

	private void recycleThread(){
		jobletExecutorThreadPool.getJobAssignmentLock().lock();
		try{
			if(isInterrupted()){
				jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
				shutdown = true;
				logger.warn(getId() + " is interrupted");
			}else if(jobletExecutorThreadPool.getNumThreadsToLayOff() > 0){
				jobletExecutorThreadPool.decrementNumThreadsToLayoff();
				jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
				shutdown = true;
				logger.debug(getId() + " is laid off");
			}else if(shutdown){
				jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
				logger.warn(getId() + " is shutdown");
			}else{
				jobletExecutorThreadPool.submitIdleThread(this);
				logger.debug(getId() + " is recycled");
			}
		}catch(RuntimeException e){
			logger.error("", e);
			throw e;
		}finally{
			jobletExecutorThreadPool.getJobAssignmentLock().unlock();
		}
	}

	//used in jobletThreadTable.jspf
	public String getRunningTimeString(){
		Long startTime = processingStartTime;
		if(startTime == null){
			return "idle";
		}
		Long duration = System.currentTimeMillis() - startTime;
		return makeDurationString(duration);
	}

	//used in jobletThreadTable.jspf
	public String getAverageRunningTimeString(){
		if(completedTasks == 0){
			return "no tasks";
		}
		Long duration = totalRunTime/completedTasks;
		return makeDurationString(duration);
	}

	private String makeDurationString(Long duration){
		StringBuilder sb = new StringBuilder();
		int sec = (int)(duration/1000L);
		int mins = sec / 60;
		sec -= mins * 60;
		sb.append(mins);
		sb.append("m ");
		sb.append(sec);
		sb.append("s");
		return sb.toString();
	}

	public JobletPackage getJobletPackage() {
		return jobletPackage;
	}

	//used by jobletThreadTable.jspf
	public JobletRequest getJoblet(){
		return Optional.ofNullable(jobletPackage).map(JobletPackage::getJobletRequest).orElse(null);
	}

	//used by jobletThreadTable.jspf
	public JobletData getJobletData(){
		return Optional.ofNullable(jobletPackage).map(JobletPackage::getJobletData).orElse(null);
	}

	public void killMe(boolean replace) {
		//called from other threads to kill this one.
		jobletExecutorThreadPool.getJobAssignmentLock().lock();
		try{
			jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
			shutdown = true;
			JobletType<?> jobletType = jobletTypeFactory.fromJobletPackage(jobletPackage);
			if(replace){
				jobletExecutorThreadPool.addNewExecutorThreadToPool();
			}
		}finally{
			jobletExecutorThreadPool.getJobAssignmentLock().unlock();
		}
	}

}
