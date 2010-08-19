package com.hotpads.profile.count.collection;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;



public class CounterManager implements CountMap{
	static Logger logger = Logger.getLogger(CounterManager.class);

	protected long latestStartMs;
	protected long nextStartMs;
	protected long periodMs;
	
	protected CountMapPeriod liveCounter;
	
	protected CountArchive primaryArchive;
	protected List<CountArchive> archives;
	protected Queue<CountMapPeriod> flushQueue;

	public CounterManager(CountArchive primaryArchive){
		this.primaryArchive = primaryArchive;
		this.periodMs = primaryArchive.getPeriodMs();
		long now = System.currentTimeMillis();
		long startTime = now - (now % periodMs);
		this.liveCounter = new AtomicCounter(startTime, periodMs);
		this.flushQueue = ListTool.createLinkedList();
		this.archives = ListTool.createArrayList();
		this.roll();//init
	}
	
	
	public synchronized void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			roll();
		}
	}
	
	//TODO better roll-up logic from short counters to longer ones.  not sure if it even makes any sense right now
	public synchronized void roll(){
//		logger.warn("rolling "+(liveCounter==null?"null":liveCounter.toString()));
		
		//archive the old one
		CountMapPeriod oldCounter = liveCounter;
		
		//flush it
		if(oldCounter!=null){
			if(CollectionTool.notEmpty(archives)){
				flushQueue.offer(oldCounter);
//				logger.warn("persistentFlushQueue.size:"+CollectionTool.size(flushQueue));
			}
		}
		
		//init the new one
		long now = System.currentTimeMillis();
		latestStartMs = now - (now % periodMs);
		nextStartMs = latestStartMs + periodMs;
		liveCounter = new AtomicCounter(nextStartMs, periodMs);
	}
	
	
	public void flushToArchives(){
		if(CollectionTool.isEmpty(archives)
				|| CollectionTool.isEmpty(flushQueue)){ return; }
		while(true){
			CountMapPeriod toFlush = flushQueue.poll();
			if(toFlush==null){ break; }
			primaryArchive.saveCounts(toFlush);
			for(CountArchive archive : IterableTool.nullSafe(this.archives)){
				archive.saveCounts(toFlush);
			}
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

	
	/******************************* accessor ********************************************/

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return liveCounter.getCountByKey();
	}
	
	@Override
	public AtomicCounter deepCopy(){
		return liveCounter.deepCopy();
	}

	public Queue<CountMapPeriod> getAsyncFlushQueue(){
		return flushQueue;
	}

	public AtomicCounter getCounter(){
		return liveCounter.getCounter();
	}
	
	public void addArchive(CountArchive archive){
		archives.add(archive);
	}


	public SortedSet<CountArchive> getArchives(){
		SortedSet<CountArchive> archivesWithPrimary = SetTool.createTreeSet();
		if(primaryArchive!=null){
			archivesWithPrimary.add(primaryArchive);
		}
		archivesWithPrimary.addAll(CollectionTool.nullSafe(archives));
		return archivesWithPrimary;
	}
	
	public Map<String,CountArchive> getArchiveByName(){
		Map<String,CountArchive> archiveByName = MapTool.createTreeMap();
		for(CountArchive ca : IterableTool.nullSafe(getArchives())){//don't forget the primary
			archiveByName.put(ca.getName(), ca);
		}
		return archiveByName;
	}
}
