package com.hotpads.joblet;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class JobletSchedulerImp implements JobletScheduler{

	private final JobletExecutorThreadPool threadPool;

	public JobletSchedulerImp(JobletExecutorThreadPool threadPool){
		this.threadPool = threadPool;
	}

	@Override
	public void blockUntilReadyForNewJoblet(){
		assignJobletToThreadPool(null);
		// TODO optional - if we instead fetch a joblet then block on adding, it will increase overall throughput,
		// but a joblet may be reserved and not processed for a time.
	}

	@Override
	public void submitJoblet(JobletPackage jobletPackage){
		assignJobletToThreadPool(jobletPackage);
	}

	private void assignJobletToThreadPool(JobletPackage jobletPackage){
		Lock lock = threadPool.getJobAssignmentLock();
		lock.lock();
		try{
			while(threadPool.isSaturated()){
				if(!threadPool.getSaturatedCondition().await(1, TimeUnit.SECONDS)){
					threadPool.findAndKillRunawayJoblets();
				}
			}
			if(jobletPackage == null){
				return;
			}
			threadPool.assignJoblet(jobletPackage);
		}catch(InterruptedException e){
			return;
		}finally{
			lock.unlock();
		}
	}

}
