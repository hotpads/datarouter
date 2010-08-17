package com.hotpads.profile.count.collection;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.util.core.ExceptionTool;

public class Counters implements CountMap{
	static Logger logger = Logger.getLogger(Counters.class);
	
//	public static final long DEFAULT_ROLLOVER_PERIOD = 5000L;
	
	/****************** static ********************************/
	
	protected static Counters counters;
//	static{
//		reset(DateTool.MILLISECONDS_IN_HOUR, null);//default to 5s with no persistence
//	}
	
	public static void reset(CountArchive primaryArchive){
		counters = new Counters(primaryArchive);
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

	protected CounterManager manager;

	public Counters(CountArchive primaryArchive){
		this.manager = new CounterManager(primaryArchive);
	}
	
	public void addArchive(boolean sync, CountArchive archive){
		manager.addArchive(archive);
	}
	
	public CounterManager getManager(){
		return manager;
	}

	@Override
	public long increment(String key) {
		return manager.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		return manager.increment(key, delta);
	}
	
	@Override
	public AtomicCounter getCounter(){
		return manager.getCounter();
	}
	
	@Override
	public AtomicCounter deepCopy(){
		return manager.deepCopy();
	}
	
	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return manager.getCountByKey();
	}
	
	/*************************** standard **************************/
	
	@Override
	public String toString(){
		if(manager==null){ return "liveManager=null"; }
		return "Counters[liveManager.startTime="+manager.periodMs+"]";
	}

	/************************ flushing/rolling ***********************/
	
	protected static ScheduledExecutorService memFlushScheduler;
	
	static{
		memFlushScheduler = Executors.newSingleThreadScheduledExecutor();
		memFlushScheduler.scheduleAtFixedRate(new PersistentFlusher(), 0, 1, TimeUnit.SECONDS); 
		logger.warn("Counters Async Flusher started");
	}
	
	public static class PersistentFlusher implements Runnable{
		public void run(){
			try{
				Thread.currentThread().setName("Counters - AsyncFlusher");
				if(counters==null){ return; }
				if(counters.manager==null){ return; }
//				logger.warn("persistentFlush");
				counters.manager.flushToArchives();
			}catch(Exception e){
				logger.error(ExceptionTool.getStackTraceAsString(e));
			}
		}
	}
	
}
