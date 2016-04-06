package com.hotpads.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.JobletSettings;
import com.hotpads.util.core.concurrent.MutableSemaphore;

@Singleton
public class JobletThrottle {
	private static final Logger logger = LoggerFactory.getLogger(JobletThrottle.class);

	private final JobletSettings jobletSettings;

	//MBytes
	private MutableSemaphore memorySemaphore;
	private MutableSemaphore cpuSemaphore;

	private int numCpuPermits;
	private int numMemoryPermits;

	@Inject
	public JobletThrottle(JobletSettings jobletSettings){
		this.jobletSettings = jobletSettings;
		numCpuPermits = jobletSettings.getMemoryTickets().getValue();
		numMemoryPermits = jobletSettings.getMemoryTickets().getValue();
		cpuSemaphore = new MutableSemaphore(numCpuPermits, true);
		memorySemaphore = new MutableSemaphore(numMemoryPermits, true);
	}

	public synchronized void setCpuPermits(int permits){
		if(permits > numCpuPermits){
			cpuSemaphore.release(permits - numCpuPermits);
			numCpuPermits = permits;
			return;
		}
		if(permits < numCpuPermits){
			cpuSemaphore.reducePermits(numCpuPermits - permits);
			numCpuPermits = permits;
			return;
		}
	}

	public synchronized void setMemoryPermits(int permits){
		if(permits > numMemoryPermits){
			memorySemaphore.release(permits - numMemoryPermits);
			numMemoryPermits = permits;
			return;
		}
		if(permits < numMemoryPermits){
			memorySemaphore.reducePermits(numMemoryPermits - permits);
			numMemoryPermits = permits;
			return;
		}
	}

	public void acquirePermits(int cpu, int memory){
		try {
			if(memorySemaphore.availablePermits() < memory){
				logger.warn("about to block acquiring "+memory+" memory permits");
			}
			memorySemaphore.acquire(memory);
			if(memorySemaphore.availablePermits() < cpu){
				logger.warn("about to block acquiring "+cpu+" cpu permits");
			}
			cpuSemaphore.acquire(cpu);
		} catch (InterruptedException e) {
			logger.warn("", e);
		}
	}

	public void releasePermits(int cpu, int memory){
		memorySemaphore.release(memory);
		cpuSemaphore.release(cpu);
	}

	public void adjustCpuMemoryPermits(){
		int cpuPermits = jobletSettings.getCpuTickets().getValue();
		int memoryPermits = jobletSettings.getMemoryTickets().getValue();
		if(numCpuPermits != cpuPermits){
			setCpuPermits(cpuPermits);
		}
		if(numMemoryPermits != memoryPermits){
			setMemoryPermits(memoryPermits);
		}
	}

	public int getAvailableCpuPermits(){
		return cpuSemaphore.availablePermits();
	}

	public int getAvailableMemoryPermits(){
		return memorySemaphore.availablePermits();
	}

}
