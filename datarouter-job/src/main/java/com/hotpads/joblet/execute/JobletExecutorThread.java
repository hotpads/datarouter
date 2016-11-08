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

	private final ReentrantLock workLock = new ReentrantLock();
	private final Condition hasWorkToBeDone = workLock.newCondition();

	private volatile boolean stopRequested = false;
	private JobletPackage jobletPackage;
	private Long processingStartTime;

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


	//called from other threads to kill this one.
	public void interruptMe(boolean replace) {
		jobletExecutorThreadPool.getJobAssignmentLock().lock();
		try{
			jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
			stopRequested = true;
			if(replace){
				jobletExecutorThreadPool.addNewExecutorThreadToPool();
			}
		}finally{
			jobletExecutorThreadPool.getJobAssignmentLock().unlock();
		}
	}

	public void submitJoblet(JobletPackage jobletPackage){
		workLock.lock();
		try{
			this.jobletPackage = jobletPackage;
			hasWorkToBeDone.signal();
		}finally{
			workLock.unlock();
		}
	}

	/*------------------ processing loop -----------------*/

	@Override
	public void run() {
		while(!stopRequested){
			workLock.lock();
			try{
				while(jobletPackage == null){
					hasWorkToBeDone.await();
				}
				PhaseTimer timer = new PhaseTimer("JobletExecutorThread");
				jobletPackage.getJobletRequest().setReservedAt(System.currentTimeMillis());
				jobletNodes.jobletRequest().put(jobletPackage.getJobletRequest(), null);
				timer.add("update reservedAt");
				setName(jobletName + " - working");
				processingStartTime = System.currentTimeMillis();
				internalProcessJobletWithExceptionHandlingAndStats(timer);
			}catch(InterruptedException ie){
				logger.warn(ie.toString());
				stopRequested = true;
				return;
			}catch(Exception e){
				logger.warn("", e);
			}finally{
				jobletPackage = null;
				processingStartTime = null;
				setName(jobletName + " - idle");
				workLock.unlock();
				recycleThread();
			}
		}
	}

	private void internalProcessJobletWithExceptionHandlingAndStats(PhaseTimer timer){
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		timer.add("waited for processing");
		try{
			internalProcessJobletWithStats(jobletPackage);
			timer.add("processed");
			jobletService.handleJobletCompletion(jobletRequest, timer);
			timer.add("completed");
		}catch(JobInterruptedException e){
			try{
				jobletService.handleJobletInterruption(jobletRequest);
			}catch(Exception e1){
				logger.error("", e1);
			}
			timer.add("interrupted");
		}catch(Exception e){
			logger.error("", e);
			try{
				jobletService.handleJobletError(jobletRequest, e, jobletRequest.getClass().getSimpleName());
				timer.add("failed");
			}catch(Exception lastResort){
				logger.error("", lastResort);
				timer.add("couldn't mark failed");
			}
		}
	}

	private final void internalProcessJobletWithStats(JobletPackage jobletPackage){
		Joblet<?> joblet = jobletFactory.createForPackage(jobletPackage);
		JobletType<?> jobletType = jobletTypeFactory.fromJobletPackage(jobletPackage);
		JobletRequest jobletRequest = jobletPackage.getJobletRequest();
		long startTimeMs = System.currentTimeMillis();
		joblet.process();

		//counters
		JobletCounters.incNumJobletsProcessed();
		JobletCounters.incNumJobletsProcessed(jobletType);
		int numItemsProcessed = Math.max(1, jobletRequest.getNumItems());
		JobletCounters.incItemsProcessed(jobletType, numItemsProcessed);
		int numTasksProcessed = Math.max(1, jobletRequest.getNumTasks());
		JobletCounters.incTasksProcessed(jobletType, numTasksProcessed);
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
				+ " and "+tasksPerSecond+" tasks/sec");
	}

	private void recycleThread(){
		jobletExecutorThreadPool.getJobAssignmentLock().lock();
		try{
			if(isInterrupted()){
				jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
				stopRequested = true;
				logger.warn(getId() + " is interrupted");
			}else if(jobletExecutorThreadPool.getNumThreadsToLayOff() > 0){
				jobletExecutorThreadPool.decrementNumThreadsToLayoff();
				jobletExecutorThreadPool.removeExecutorThreadFromPool(this);
				stopRequested = true;
				logger.debug(getId() + " is laid off");
			}else if(stopRequested){
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

	/*----------------- getters ---------------------*/

	public Long getRunningTime(){
		Long localProcessingStartTime = processingStartTime;//thread safe copy
		if(localProcessingStartTime == null){
			return null;
		}
		return System.currentTimeMillis() - localProcessingStartTime;
	}

	//used in runningJoblets.jspf
	public String getRunningTimeString(){
		Long localStartTime = processingStartTime;//thread safe copy
		if(localStartTime == null){
			return "idle";
		}
		long duration = System.currentTimeMillis() - localStartTime;
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

	//used by runningJoblets.jspf
	public JobletRequest getJobletRequest(){
		return Optional.ofNullable(jobletPackage).map(JobletPackage::getJobletRequest).orElse(null);
	}

	//used by runningJoblets.jspf
	public JobletData getJobletData(){
		return Optional.ofNullable(jobletPackage).map(JobletPackage::getJobletData).orElse(null);
	}

	public JobletPackage getJobletPackage() {
		return jobletPackage;
	}

}
