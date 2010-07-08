package com.hotpads.profile.count;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;



public class CounterManager implements Counter{
	static Logger logger = Logger.getLogger(CounterManager.class);

	protected long managerStartMs;
	protected long latestStartMs;
	protected long nextStartMs;
	protected long periodMs;
	protected Counter counter;

	protected long retainForMs;
	protected int numToRetain;
	protected Counter[] archive;
	protected Queue<Counter> memoryFlushQueue;
	protected Queue<Counter> persistentFlushQueue;
	
	protected CountArchive persistentArchive;

	public CounterManager(long periodMs, long retainForMs, CountArchive persistentArchive){
		long now = System.currentTimeMillis();
		this.periodMs = periodMs;
		this.retainForMs = retainForMs;
		this.numToRetain = (int)(retainForMs / periodMs);
		this.managerStartMs = now - (now % periodMs);
		this.archive = new Counter[numToRetain];
		this.memoryFlushQueue = ListTool.createLinkedList();
		this.persistentFlushQueue = ListTool.createLinkedList();
		this.persistentArchive = persistentArchive;
		this.roll();//init
	}
	
	public Counter getCounter(){
		return counter;
	}
	
	protected long getWindowStartMs(long ms){
		return ms - (ms % periodMs);
	}
	
	protected int getIndexForMs(long ms){
		return (int)(getWindowStartMs(ms) % numToRetain);
	}
	
	public synchronized void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			roll();
		}
	}
	
	@Override
	public void merge(Counter other){
		Counter destination = getArchiveCounter(other.getStartTimeMs());
		destination.merge(other);
	}
	
	public synchronized void roll(){
		logger.warn("rolling "+(counter==null?"null":counter.toString()));
		
		//archive the old one
		Counter oldCounter = counter;
		archive[getIndexForMs(latestStartMs)] = oldCounter;
		
		//flush it
		if(oldCounter!=null){
			memoryFlushQueue.offer(oldCounter);
			logger.warn(CollectionTool.size(memoryFlushQueue));
			if(persistentArchive!=null){
				persistentFlushQueue.offer(oldCounter);
			}
		}
		
		//init the new one
		long now = System.currentTimeMillis();
		latestStartMs = now - (now % periodMs);
		nextStartMs = latestStartMs + periodMs;
		counter = new AtomicCounter(nextStartMs, periodMs);
	}
	
	

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return counter.getCountByKey();
	}

	@Override
	public long getLengthMs(){
		return retainForMs;
	}

	@Override
	public long getStartTimeMs(){
		return managerStartMs;
	}

	@Override
	public long increment(String key){
		rollIfNecessary();
		return counter.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		rollIfNecessary();
		return counter.increment(key, delta);
	}
	
	public Counter pollMemoryFlushQueue(){
		return memoryFlushQueue.poll();
	}
	
	public Counter getArchiveCounter(long otherStartTimeMs){
		long age = System.currentTimeMillis() - otherStartTimeMs;
		if(age > retainForMs){ return null; }//we don't have it anymore
		int idx = getIndexForMs(otherStartTimeMs);
		Counter counter = archive[idx];
		if(counter==null){
			counter = new AtomicCounter(getWindowStartMs(otherStartTimeMs), periodMs);
			archive[idx] = counter;
		}
		return counter;
	}
	
	public void doPersistentFlush(){
		if(persistentArchive==null){ return; }
		while(true){
			Counter counter = persistentFlushQueue.poll();
			if(counter==null){ break; }
			persistentArchive.saveCounts(periodMs, 
					getWindowStartMs(counter.getStartTimeMs()), //adjust the window to this manager's window
					counter.getCountByKey());
		}
	}
	
}
