package com.hotpads.profile.count;

import java.util.List;

import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ListTool;

public class Counters implements Counter{
	
	/****************** static ********************************/
	
	protected static Counters counters;
	static{
		reset(5000, DateTool.MILLISECONDS_IN_HOUR);
	}
	
	public static void reset(long periodMs, long retainForMs){
		counters = new Counters(5000, DateTool.MILLISECONDS_IN_HOUR);
	}
	
	public static void disable(){
		counters = null;
	}

	public static long inc(String key) {
		return counters==null?Long.MIN_VALUE:counters.increment(key);
	}

	public static long inc(String key, long delta) {
		return counters==null?Long.MIN_VALUE:counters.increment(key, delta);
	}
	
	
	
	/******************* instance *****************************/

	protected CounterManager liveManager;
	protected List<CounterManager> archiveManagers;

	public Counters(long periodMs, long retainForMs){
		this.liveManager = new CounterManager(periodMs, retainForMs);
		this.archiveManagers = ListTool.createArrayList();
	}

	@Override
	public long increment(String key) {
		return liveManager.increment(key);
	}

	@Override
	public long increment(String key, long delta) {
		return liveManager.increment(key, delta);
	}
	
}
