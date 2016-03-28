package com.hotpads.joblet;

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

import com.hotpads.joblet.JobletExecutorThread.JobletExecutorThreadFactory;

public class JobletExecutorThreadPool {
	private static final Logger logger = LoggerFactory.getLogger(JobletExecutorThreadPool.class);

	@Singleton
	public static class JobletExecutorThreadPoolFactory{

		@Inject
		private JobletThrottle jobletThrottle;
		@Inject
		private JobletExecutorThreadFactory jobletExecutorThreadFactory;

		public JobletExecutorThreadPool create(Integer threadPoolSize, JobletType jobletType){
			return new JobletExecutorThreadPool(threadPoolSize, jobletType, jobletThrottle,
					jobletExecutorThreadFactory);
		}

	}

	private static final ThreadGroup jobletThreadGroup = new ThreadGroup("joblet");

	ReentrantLock jobAssignmentLock = new ReentrantLock();
	int numThreadsToLayOff = 0;

	private Condition saturatedCondition = jobAssignmentLock.newCondition();
	private BlockingQueue<JobletExecutorThread> waitingExecutorThreads = new LinkedBlockingQueue<>();
	private List<JobletExecutorThread> runningExecutorThreads = new ArrayList<>();
	private List<JobletExecutorThread> allExecutorThreads = new ArrayList<>();
	private int numThreads;
	private ThreadGroup threadGroup;

	private final JobletType jobletType;

	private final JobletThrottle jobletThrottle;
	private final JobletExecutorThreadFactory jobletExecutorThreadFactory;

	private JobletExecutorThreadPool(Integer threadPoolSize, JobletType jobletType, JobletThrottle jobletThrottle,
			JobletExecutorThreadFactory jobletExecutorThreadFactory) {
		this.jobletType = jobletType;
		this.jobletThrottle = jobletThrottle;
		this.jobletExecutorThreadFactory = jobletExecutorThreadFactory;
		this.threadGroup = new ThreadGroup(jobletThreadGroup, jobletType.getPersistentString());
		resize(threadPoolSize);
	}

	public Lock getJobAssignmentLock() {
		return jobAssignmentLock ;
	}

	public Condition getSaturatedCondition() {
		return saturatedCondition;
	}

	public void assignJoblet(JobletPackage jobletPackage) {
		jobAssignmentLock.lock();
		try{
			JobletExecutorThread thread = waitingExecutorThreads.poll();
			if(thread == null){
				throw new IllegalStateException("No thread available!");
			}
			jobletThrottle.adjustCpuMemoryPermits();
			thread.submitJoblet(jobletPackage);
			runningExecutorThreads.add(thread);
		}finally{
			jobAssignmentLock.unlock();
		}
	}

	public boolean isSaturated() {
		jobAssignmentLock.lock();
		try{
			return waitingExecutorThreads.isEmpty() || runningExecutorThreads.size() == numThreads;
		}finally{
			jobAssignmentLock.unlock();
		}
	}

	public void resize(int threadPoolSize) {
		numThreads = threadPoolSize;
		jobAssignmentLock.lock();
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
			jobAssignmentLock.unlock();
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
	void removeExecutorThreadFromPool(JobletExecutorThread thread) {
		waitingExecutorThreads.remove(thread);
		runningExecutorThreads.remove(thread);
		allExecutorThreads.remove(thread);
		thread.interrupt();
	}

	void addNewExecutorThreadToPool() {
		if(numThreadsToLayOff > 0){
			numThreadsToLayOff --;
			return;
		}
		JobletExecutorThread newThread = jobletExecutorThreadFactory.create(this, threadGroup);
		newThread.start();
		allExecutorThreads.add(newThread);
		submitIdleThread(newThread);
	}

	void submitIdleThread(JobletExecutorThread thread){
		waitingExecutorThreads.add(thread);
		runningExecutorThreads.remove(thread);
		saturatedCondition.signal();
	}

	public void findAndKillRunawayJoblets() {
		jobAssignmentLock.lock();
		try{
			ArrayList<JobletExecutorThread> threadsToKill = new ArrayList<>();
			for(JobletExecutorThread thread : allExecutorThreads){
				Long runningTime = thread.getRunningTime();
				if(runningTime != null && runningTime > ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS){
					if(!thread.getJobletPackage().getJoblet().getRestartable()){
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
				jobletThrottle.releasePermits(jobletType.getCpuPermits(), jobletType.getMemoryPermits());
				addNewExecutorThreadToPool();
				logger.error("after:");
				logger.error("allExecutorThreads: "+allExecutorThreads.size());
				logger.error("waitingExecutorThreads: "+waitingExecutorThreads.size());
				logger.error("numThreadsToLayOff: "+numThreadsToLayOff);
			}
		}finally{
			jobAssignmentLock.unlock();
		}
	}

	public List<JobletExecutorThread> getRunningJobletExecutorThreads() {
		//List<JobletExecutorThread> runningJobletThreads = new ArrayList<JobletExecutorThread>(allExecutorThreads);
		//return runningJobletThreads;

		List<JobletExecutorThread> runningJobletThreads = new ArrayList<>(runningExecutorThreads);
		return runningJobletThreads;
	}

	public List<JobletExecutorThread> getWaitingJobletExecutorThreads(){
		List<JobletExecutorThread> waitingJobletThreads = new ArrayList<>(waitingExecutorThreads);
		return waitingJobletThreads;
	}

}
