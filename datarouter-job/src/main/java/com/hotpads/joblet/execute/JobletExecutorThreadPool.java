package com.hotpads.joblet.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.JobletExecutorThread.JobletExecutorThreadFactory;

public class JobletExecutorThreadPool {
	private static final Logger logger = LoggerFactory.getLogger(JobletExecutorThreadPool.class);

	@Singleton
	public static class JobletExecutorThreadPoolFactory{
		@Inject
		private JobletExecutorThreadFactory jobletExecutorThreadFactory;

		public JobletExecutorThreadPool create(Integer threadPoolSize, JobletType<?> jobletType){
			return new JobletExecutorThreadPool(threadPoolSize, jobletType, jobletExecutorThreadFactory);
		}
	}

	private static final ThreadGroup jobletThreadGroup = new ThreadGroup("joblet");

	private final ReentrantLock assignmentLock = new ReentrantLock();
	private final Condition saturatedCondition = assignmentLock.newCondition();
	private final BlockingQueue<JobletExecutorThread> waitingExecutorThreads = new LinkedBlockingQueue<>();
	private final List<JobletExecutorThread> runningExecutorThreads = new ArrayList<>();
	private final List<JobletExecutorThread> allExecutorThreads = new ArrayList<>();
	private final ThreadGroup threadGroup;
	private final JobletType<?> jobletType;
	private final JobletExecutorThreadFactory jobletExecutorThreadFactory;

	private int numThreadsToLayOff = 0;
	private int numThreads;

	private JobletExecutorThreadPool(Integer threadPoolSize, JobletType<?> jobletType,
			JobletExecutorThreadFactory jobletExecutorThreadFactory) {
		this.jobletType = jobletType;
		this.jobletExecutorThreadFactory = jobletExecutorThreadFactory;
		this.threadGroup = new ThreadGroup(jobletThreadGroup, jobletType.getPersistentString());
		resize(threadPoolSize);
	}

	public void assignJobletPackage(JobletPackage jobletPackage) {
		assignmentLock.lock();
		try{
			JobletExecutorThread thread = waitingExecutorThreads.poll();
			if(thread == null){
				throw new IllegalStateException("No thread available!");
			}
			thread.submitJoblet(jobletPackage);
			runningExecutorThreads.add(thread);
		}finally{
			assignmentLock.unlock();
		}
	}

	public boolean isSaturated() {
		assignmentLock.lock();
		try{
			return waitingExecutorThreads.isEmpty() || runningExecutorThreads.size() == numThreads;
		}finally{
			assignmentLock.unlock();
		}
	}

	public void resize(int threadPoolSize) {
		numThreads = threadPoolSize;
		assignmentLock.lock();
		try{
			if(threadPoolSize == allExecutorThreads.size()){
				return;
			}else if(threadPoolSize > allExecutorThreads.size()){
				while(threadPoolSize > allExecutorThreads.size()){
					addNewExecutorThreadToPool();
				}
			}else if(threadPoolSize + numThreadsToLayOff < allExecutorThreads.size()){
				while(threadPoolSize + numThreadsToLayOff < allExecutorThreads.size()){
					removeAnExecutorThreadFromPool();
				}
			}
		}finally{
			assignmentLock.unlock();
		}
	}

	private void removeAnExecutorThreadFromPool() {
		//try to find an idle one
		JobletExecutorThread threadToLayOff;
		if((threadToLayOff = waitingExecutorThreads.poll()) != null){
			removeExecutorThreadFromPool(threadToLayOff);
			return;
		}
		//mark a running one to die after current job
		numThreadsToLayOff++;
	}

	public void removeExecutorThreadFromPool(JobletExecutorThread thread) {
		waitingExecutorThreads.remove(thread);
		runningExecutorThreads.remove(thread);
		allExecutorThreads.remove(thread);
		thread.interrupt();
	}

	public void addNewExecutorThreadToPool() {
		if(numThreadsToLayOff > 0){
			numThreadsToLayOff --;
			return;
		}
		JobletExecutorThread newThread = jobletExecutorThreadFactory.create(this, threadGroup);
		newThread.start();
		allExecutorThreads.add(newThread);
		submitIdleThread(newThread);
	}

	public void submitIdleThread(JobletExecutorThread thread){
		waitingExecutorThreads.add(thread);
		runningExecutorThreads.remove(thread);
		saturatedCondition.signal();
	}

	public void findAndKillRunawayJoblets() {
		assignmentLock.lock();
		try{
			ArrayList<JobletExecutorThread> threadsToKill = new ArrayList<>();
			for(JobletExecutorThread thread : allExecutorThreads){
				Long runningTime = thread.getRunningTime();
				if(runningTime != null && runningTime > ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS){
					if(!thread.getJobletPackage().getJobletRequest().getRestartable()){
						continue;//don't kill non-restartable threads due to timeout (such as feeds)
					}
					threadsToKill.add(thread);
				}
			}
			for(JobletExecutorThread thread : threadsToKill){
				logger.error("killing runaway thread "+thread.getName());
				logger.error("before:");
				logger.error("allExecutorThreads: "+allExecutorThreads.size());
				logger.error("waitingExecutorThreads: "+waitingExecutorThreads.size());
				logger.error("numThreadsToLayOff: "+numThreadsToLayOff);
				thread.interrupt();
				removeExecutorThreadFromPool(thread);
				addNewExecutorThreadToPool();
				logger.error("after:");
				logger.error("allExecutorThreads: "+allExecutorThreads.size());
				logger.error("waitingExecutorThreads: "+waitingExecutorThreads.size());
				logger.error("numThreadsToLayOff: "+numThreadsToLayOff);
			}
		}finally{
			assignmentLock.unlock();
		}
	}

	public List<JobletExecutorThread> getRunningJobletExecutorThreads() {
		return new ArrayList<>(runningExecutorThreads);
	}

	public List<JobletExecutorThread> getWaitingJobletExecutorThreads(){
		return new ArrayList<>(waitingExecutorThreads);
	}

	public void decrementNumThreadsToLayoff(){
		--numThreadsToLayOff;
	}


	/*-------------------- get/set -----------------------*/

	public int getNumThreadsToLayOff(){
		return numThreadsToLayOff;
	}

	public Lock getJobAssignmentLock() {
		return assignmentLock ;
	}

	public Condition getSaturatedCondition() {
		return saturatedCondition;
	}

}
