package com.hotpads.profile.count.collection;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.MapTool;

public class AtomicCounter implements CountMapPeriod{
	static Logger logger = LoggerFactory.getLogger(AtomicCounter.class);
	
	public static final Integer INITIAL_CAPACITY = 512;//try to set higher than est num counters
	
	protected long startTimeMs;
	protected long lengthMs;
	protected long lastMs;
	protected ConcurrentMap<String,AtomicLong> countByKey;
	
	protected String createdByThreadId;
	
	protected Date humanReadableStartTime;
	
	public AtomicCounter(long startTimeMs, long lengthMs){
		this.startTimeMs = startTimeMs;
		this.lengthMs = lengthMs;
		this.lastMs = startTimeMs + lengthMs - 1;
		this.countByKey = new ConcurrentHashMap<String,AtomicLong>(INITIAL_CAPACITY);
		this.humanReadableStartTime = new Date(startTimeMs);
		Thread createdByThread = Thread.currentThread();
		this.createdByThreadId = createdByThread.getId()+"-"+createdByThread.getName();
//		if(this.toString().contains("Db")){
//			logger.warn("created "+this);
//		}
	}

	@Override
	public String toString(){
		String time = DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(startTimeMs);
		return getClass().getSimpleName()+"["+time+","+CountPartitionedNode.getSuffix(lengthMs)
				+","+System.identityHashCode(this)+","+createdByThreadId+"]";
	}
	
	@Override
	public long getPeriodMs(){
		return lengthMs;
	}

	@Override
	public long getStartTimeMs(){
		return startTimeMs;
	}
	
	@Override
	public long getNextStartTimeMs(){
		return startTimeMs + lengthMs;
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
	
	public void merge(CountMap other){
		for(Map.Entry<String,AtomicLong> otherEntry : MapTool.nullSafe(other.getCountByKey()).entrySet()){
			AtomicLong existingValue = countByKey.get(otherEntry.getKey());
			if(existingValue!=null){ existingValue.addAndGet(otherEntry.getValue().longValue()); }
			else{ countByKey.put(otherEntry.getKey(), new AtomicLong(otherEntry.getValue().longValue())); }
		}
	}

	protected AtomicLong getOrCreate(String key){
		AtomicLong count = countByKey.get(key);
		if(count!=null){ return count; }
		AtomicLong newVal = new AtomicLong(0L);//could be wasted
		AtomicLong existingVal = countByKey.putIfAbsent(key, newVal);
		return existingVal==null?newVal:existingVal;
	}
	
	@Override
	public AtomicCounter getCounter(){
		return this;
	}
	
	public AtomicCounter deepCopy(){
		AtomicCounter copy = new AtomicCounter(startTimeMs, lengthMs);
		for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(countByKey).entrySet()){
			copy.countByKey.put(entry.getKey(), new AtomicLong(entry.getValue().longValue()));
		}
		return copy;
	}
}
