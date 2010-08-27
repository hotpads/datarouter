package com.hotpads.profile.count.collection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;

public class Counters implements CountMap{
	static Logger logger = Logger.getLogger(Counters.class);
	
//	public static final long DEFAULT_ROLLOVER_PERIOD = 5000L;
	
	/****************** static ********************************/
	
	protected static Counters counters;
	
	public static void reset(long rollPeriodMs){
		counters = new Counters(rollPeriodMs);
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

	public Counters(long rollPeriodMs){
		this.manager = new CounterManager(rollPeriodMs);
	}
	
	public void addFlusher(CountArchiveFlusher flusher){
		manager.addFlusher(flusher);
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
		return "Counters[liveManager.startTime="+manager.rollPeriodMs+"]";
	}
	
}
