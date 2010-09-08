package com.hotpads.profile.count.collection;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.profile.PhaseTimer;



public class CounterManager implements CountMap{
	static Logger logger = Logger.getLogger(CounterManager.class);

	protected long latestStartMs;
	protected long nextStartMs;
	protected long rollPeriodMs;
	
	protected CountMapPeriod liveCounter;
	protected List<CountArchiveFlusher> flushers;
	
	private Runtime runtime = Runtime.getRuntime();

	public CounterManager(long rollPeriodMs){
		this.rollPeriodMs = rollPeriodMs;
		long now = System.currentTimeMillis();
		long startTime = now - (now % rollPeriodMs);
		this.liveCounter = new AtomicCounter(startTime, rollPeriodMs);
		this.flushers = ListTool.createArrayList();
		this.roll();//init
	}
	
	
	public void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			roll();
		}
	}
	
	protected Object rollCheckLock = new Object();
	protected Object rollLock = new Object();
	
	//TODO better roll-up logic from short counters to longer ones.  not sure if it even makes any sense right now
	public void roll(){
		
		//a few threads may slip past the rollIfNecessary call and pile up here

		long now = System.currentTimeMillis();
		long nowPeriodStart = now - (now % rollPeriodMs);
		
		synchronized(rollCheckLock){//essentially an atomicCheckAndPut on latestStartMs
			if(liveCounter!=null && nowPeriodStart==liveCounter.getStartTimeMs()){
//				logger.warn("aborting roll "+new Date(latestStartMs));
				return; //another thread already rolled it
			}
			latestStartMs = nowPeriodStart;
			nextStartMs = latestStartMs + rollPeriodMs;//now other threads should return rollIfNecessary=false
		}
		
		//only one thread (per period) should get to this point because of the logical check above
		
		synchronized(rollLock){ //protect against multiple periods overlapping?  we may get count skew here if things get backed up
			//swap in the new counter
			CountMapPeriod oldCounter = liveCounter;
			liveCounter = new AtomicCounter(latestStartMs, rollPeriodMs);
			
			//add previous counter to flush queue
			if(oldCounter!=null){
				for(CountArchiveFlusher flusher : IterableTool.nullSafe(flushers)){
					flusher.offer(oldCounter);
				}
			}
		}
		
		addSpecialCounts(liveCounter);
	}
	
	
	protected void addSpecialCounts(CountMapPeriod counter){
		long startNs = System.nanoTime();
		//get the actual values
		//not sure if these are slow
		PhaseTimer timer = new PhaseTimer("memOps");
		long freeMemory = timer.time(runtime.freeMemory(), "freeMemory()");
		long maxMemory = timer.time(runtime.maxMemory(), "maxMemory()");
		long totalMemory = timer.time(runtime.totalMemory(), "totalMemory()");
		long ns = System.nanoTime() - startNs;
		if(timer.getElapsedTimeBetweenFirstAndLastEvent() > 1){
			logger.warn(ns+"ns "+timer);
		}
		
		long usedMemory = totalMemory - freeMemory;
		counter.increment("memory free MB", freeMemory >> 20);
		counter.increment("memory max MB", maxMemory >> 20);
		counter.increment("memory total MB", runtime.totalMemory() >> 20);
		counter.increment("memory used MB", usedMemory >> 20);
	}

	@Override
	public long increment(String key){
		rollIfNecessary();
		return liveCounter.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		rollIfNecessary();
		return liveCounter.increment(key, delta);
	}

	
	/******************************* accessor ********************************************/

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return liveCounter.getCountByKey();
	}
	
	@Override
	public AtomicCounter deepCopy(){
		return liveCounter.deepCopy();
	}

	public AtomicCounter getCounter(){
		return liveCounter.getCounter();
	}
	
	public void addFlusher(CountArchiveFlusher flusher){
		flushers.add(flusher);
	}

	public void stopAndFlushAll(){
		for(CountArchiveFlusher flusher : flushers){
			flusher.shutdownAndFlushAll();
		}
	}

	public SortedSet<CountArchive> getArchives(){
		SortedSet<CountArchive> archives = SetTool.createTreeSet();
		for(CountArchiveFlusher flusher : IterableTool.nullSafe(flushers)){
			archives.addAll(CollectionTool.nullSafe(flusher.getArchives()));
		}
		return archives;
	}
	
	public Map<String,CountArchive> getArchiveByName(){
		Map<String,CountArchive> archiveByName = MapTool.createTreeMap();
		for(CountArchive ca : IterableTool.nullSafe(getArchives())){//don't forget the primary
			archiveByName.put(ca.getName(), ca);
		}
		return archiveByName;
	}
}
