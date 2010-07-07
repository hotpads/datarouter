package com.hotpads.profile.count;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter implements Counter {
	
	long startTimeMs;
	long lengthMs;
	long lastMs;
	protected ConcurrentMap<String,AtomicLong> countByKey;
	
	public AtomicCounter(long startTimeMs, long lengthMs){
		this.startTimeMs = startTimeMs;
		this.lengthMs = lengthMs;
		this.lastMs = startTimeMs + lengthMs - 1;
		this.countByKey = new ConcurrentHashMap<String,AtomicLong>();
	}
	
	@Override
	public long getLengthMs(){
		return startTimeMs;
	}

	@Override
	public long getStartTimeMs(){
		return lengthMs;
	}

	@Override
	public Map<String,AtomicLong> getCountByKey(){
		return countByKey;
	}

	@Override
	public long increment(String key){
		return getOrCreate(key).getAndAdd(1);
	}

	@Override
	public long increment(String key, long delta){
		return getOrCreate(key).getAndAdd(delta);
	}
	
	@Override
	public void merge(Counter other){
		if(other.getStartTimeMs() < startTimeMs || other.getStartTimeMs() > lastMs){
			throw new IllegalArgumentException(other.getStartTimeMs()+" outside the range of this counter ["+startTimeMs+","+lastMs+"]");
		}
		for(Map.Entry<String,AtomicLong> entry : other.getCountByKey().entrySet()){
			getOrCreate(entry.getKey()).getAndAdd(entry.getValue().get());
		}
	}

	protected AtomicLong getOrCreate(String key){
		AtomicLong count = countByKey.get(key);
		if(count!=null){ return count; }
		AtomicLong newVal = new AtomicLong(0L);//could be wasted
		AtomicLong existingVal = countByKey.putIfAbsent(key, newVal);
		return existingVal==null?newVal:existingVal;
	}
	
}
