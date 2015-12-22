package com.hotpads.profile.count.collection;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrRuntimeTool;
import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.util.core.profile.PhaseTimer;



public class DatarouterCountCollector implements CountCollector{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterCountCollector.class);

	private final long rollPeriodMs;
	private final List<CountArchiveFlusher> flushers;
	
	private long latestStartMs;
	private long nextStartMs;
	private CountCollectorPeriod liveCounter;

	public DatarouterCountCollector(long rollPeriodMs){
		this.rollPeriodMs = rollPeriodMs;
		long now = System.currentTimeMillis();
		long startTime = now - (now % rollPeriodMs);
		this.liveCounter = new AtomicCounter(startTime, rollPeriodMs);
		this.flushers = new ArrayList<>();
		this.checkAndRoll();//init
		logger.warn("created "+this);
	}
	
	public void addFlusher(CountArchiveFlusher flusher){
		flushers.add(flusher);
	}
	
	//called on every increment right now.  currentTimeMillis is supposedly as cheap as a memory access
	public void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			checkAndRoll();
		}
	}
	
	
	//TODO better roll-up logic from short counters to longer ones.  not sure if it even makes any sense right now
	public synchronized void checkAndRoll(){
		//a few threads may slip past the rollIfNecessary call and pile up here

		long now = System.currentTimeMillis();
		long nowPeriodStart = now - (now % rollPeriodMs);
		
//		synchronized(rollCheckLock){//essentially an atomicCheckAndPut on latestStartMs
			if(liveCounter!=null && nowPeriodStart==liveCounter.getStartTimeMs()){
//				logger.warn("aborting roll "+new Date(latestStartMs));
				return; //another thread already rolled it
			}
			latestStartMs = nowPeriodStart;
			nextStartMs = latestStartMs + rollPeriodMs;//now other threads should return rollIfNecessary=false
//		}
//		
//		//only one thread (per period) should get to this point because of the logical check above
//		
		//protect against multiple periods overlapping?  we may get count skew here if things get backed up
//		synchronized(rollLock){ 
			//swap in the new counter
			CountCollectorPeriod oldCounter = liveCounter;
			liveCounter = new AtomicCounter(latestStartMs, rollPeriodMs);
//			logger.warn(Thread.currentThread().getName()+" rolled CounterManager, created "+liveCounter);
			if(oldCounter.getStartTimeMs()==liveCounter.getStartTimeMs()){
				logger.warn("probably concurrency bug.  double counter instantiation "+liveCounter);
			}
			//add previous counter to flush queue
			if(oldCounter!=null){
				for(CountArchiveFlusher flusher : flushers){
					flusher.offer(oldCounter);
				}
			}
//		}
		
		addSpecialCounts(liveCounter);
	}
	
	
	//caught 4 threads in this method at the same time and then waiting to aquire the logger's lock.
	//  maybe bail out if the special counts are already in the counter.  or add a lock to the counter
	private void addSpecialCounts(CountCollectorPeriod counter){
		long startNs = System.nanoTime();
		//get the actual values
		//not sure if these are slow
		PhaseTimer timer = new PhaseTimer("memOps");
		long freeMemory = timer.time(DrRuntimeTool.getFreeMemory(), "freeMemory()");
		long maxMemory = timer.time(DrRuntimeTool.getMaxMemory(), "maxMemory()");
		long totalMemory = timer.time(DrRuntimeTool.getTotalMemory(), "totalMemory()");
		int threadCount = timer.time(ManagementFactory.getThreadMXBean().getThreadCount(), "getThreadCount()");
		long ns = System.nanoTime() - startNs;
		if(timer.getElapsedTimeBetweenFirstAndLastEvent() > 1){
			logger.warn(ns+"ns "+timer);
		}
		
		//TODO should multiply by period of time
		long usedMemory = totalMemory - freeMemory;
		counter.increment("memory free MB", freeMemory >> 20);
		counter.increment("memory max MB", maxMemory >> 20);
		counter.increment("memory total MB", totalMemory >> 20);
		counter.increment("memory used MB", usedMemory >> 20);
		counter.increment("Thread count", threadCount);
	}

	public SortedSet<CountArchive> getArchives(){
		SortedSet<CountArchive> archives = new TreeSet<>();
		for(CountArchiveFlusher flusher : DrIterableTool.nullSafe(flushers)){
			archives.addAll(DrCollectionTool.nullSafe(flusher.getArchives()));
		}
		return archives;
	}
	
	public Map<String,CountArchive> getArchiveByName(){
		Map<String,CountArchive> archiveByName = new TreeMap<>();
		for(CountArchive ca : DrIterableTool.nullSafe(getArchives())){//don't forget the primary
			archiveByName.put(ca.getName(), ca);
		}
		return archiveByName;
	}

	
	/******************************* CountCollector interface methods ********************************/

	@Override
	public void stopAndFlushAll(){
		for(CountArchiveFlusher flusher : flushers){
			flusher.shutdownAndFlushAll();
		}
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

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return liveCounter.getCountByKey();
	}
	
	@Override
	public AtomicCounter getCounter(){
		return liveCounter.getCounter();
	}
}
