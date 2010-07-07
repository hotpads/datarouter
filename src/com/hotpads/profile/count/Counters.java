package com.hotpads.profile.count;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class Counters implements Counter{
	static Logger logger = Logger.getLogger(Counters.class);
	
	/****************** static ********************************/
	
	protected static Counters counters;
	static{
		reset(5000, DateTool.MILLISECONDS_IN_HOUR, null);//default to 5s with no persistence
	}
	
	public static void reset(long periodMs, long retainForMs, CountArchive persistentArchive){
		counters = new Counters(5000, DateTool.MILLISECONDS_IN_HOUR, persistentArchive);
	}
	
	public static void disable(){
		counters = null;
	}

	public static Long inc(String key) {
		return counters==null?null:counters.increment(key);
	}

	public static Long inc(String key, long delta) {
		return counters==null?null:counters.increment(key, delta);
	}
	
	public static Counters get(){
		return counters;
	}
	
	
	
	/******************* instance *****************************/

	protected CounterManager liveManager;
	protected List<CounterManager> archives;

	public Counters(long periodMs, long retainForMs, CountArchive persistentArchive){
		this.liveManager = new CounterManager(periodMs, retainForMs, persistentArchive);
		this.archives = ListTool.createArrayList();
	}
	
	public void addArchive(long periodMs, long retainForMs, CountArchive persistentArchive){
		archives.add(new CounterManager(periodMs, retainForMs, persistentArchive));
	}

	@Override
	public long increment(String key) {
		return liveManager.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		return liveManager.increment(key, delta);
	}
	
	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return liveManager.getCountByKey();
	}

	@Override
	public long getLengthMs(){
		return liveManager.getLengthMs();
	}

	@Override
	public long getStartTimeMs(){
		return liveManager.getStartTimeMs();
	}

	@Override
	public void merge(Counter other){
		liveManager.merge(other);
	}
	
	/*************************** standard **************************/
	
	@Override
	public String toString(){
		if(liveManager==null){ return "liveManager=null"; }
		return "Counters[liveManager.startTime="+liveManager.getStartTimeMs()+"]";
	}

	/************************ flushing/rolling ***********************/
	
	public static class MemoryFlusher implements Runnable{
		public void run(){
			if(counters==null){ return; }
			while(true){
				logger.warn("memFlush");
				Counter source = counters.liveManager.pollMemoryFlushQueue();
				if(source==null){ break; }
				logger.warn("counter starting at:"+source.getStartTimeMs()+"ms");
				for(Counter destination : IterableTool.nullSafe(counters.archives)){
					destination.merge(source);
				}
			}
		}
	}
	
	public static class PersistentFlusher implements Runnable{
		public void run(){
			if(counters==null){ return; }
			while(true){
				logger.warn("persistentFlush");
				Counter source = counters.liveManager.pollMemoryFlushQueue();
				if(source==null){ break; }
				logger.warn("counter starting at:"+source.getStartTimeMs()+"ms");
				for(CounterManager destination : IterableTool.nullSafe(counters.archives)){
					destination.doPersistentFlush();
				}
			}
		}
	}
	
	protected static ScheduledExecutorService memFlushScheduler;
	
	static{
		memFlushScheduler = Executors.newSingleThreadScheduledExecutor();
		memFlushScheduler.scheduleAtFixedRate(new MemoryFlusher(), 0, 1, TimeUnit.SECONDS);  
		memFlushScheduler.scheduleAtFixedRate(new PersistentFlusher(), 0, 1, TimeUnit.SECONDS); 
		logger.warn("scheduler started");
	}
	
}
