package com.hotpads.joblet.execute;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.hotpads.joblet.JobletPackage;

public class JobletSchedulerImp implements JobletScheduler{

	private final JobletExecutorThreadPool threadPool;

	public JobletSchedulerImp(JobletExecutorThreadPool threadPool){
		this.threadPool = threadPool;
	}

	@Override
	public void blockUntilReadyForNewJoblet(){
		assignJobletPackageToThreadPool(null);
		// TODO optional - if we instead fetch a joblet then block on adding, it will increase overall throughput,
		// but a joblet may be reserved and not processed for a time.
	}

	@Override
	public void submitJobletPackage(JobletPackage jobletPackage){
		assignJobletPackageToThreadPool(jobletPackage);
	}

	private void assignJobletPackageToThreadPool(JobletPackage jobletPackage){
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
			threadPool.assignJobletPackage(jobletPackage);
		}catch(InterruptedException e){
			return;
		}finally{
			lock.unlock();
		}
	}

}
