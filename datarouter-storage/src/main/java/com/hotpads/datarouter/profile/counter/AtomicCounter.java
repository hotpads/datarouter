package com.hotpads.datarouter.profile.counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrMapTool;

public class AtomicCounter implements CountCollectorPeriod{
	public static final Integer INITIAL_CAPACITY = 512;//try to set higher than est num counters

	private final long startTimeMs;
	private final long lengthMs;
	private final ConcurrentMap<String,AtomicLong> countByKey;
	private final String createdByThreadId;

	public AtomicCounter(long startTimeMs, long lengthMs){
		this.startTimeMs = startTimeMs;
		this.lengthMs = lengthMs;
		this.countByKey = new ConcurrentHashMap<String,AtomicLong>(INITIAL_CAPACITY);
		Thread createdByThread = Thread.currentThread();
		this.createdByThreadId = createdByThread.getId()+"-"+createdByThread.getName();
	}

	@Override
	public String toString(){
		String time = DrDateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(startTimeMs);
		return getClass().getSimpleName()+"["+time+","+Counters.getSuffix(lengthMs)
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

	public void merge(CountCollector other){
		for(Map.Entry<String,AtomicLong> otherEntry : DrMapTool.nullSafe(other.getCountByKey()).entrySet()){
			AtomicLong existingValue = countByKey.get(otherEntry.getKey());
			if(existingValue != null) {
				existingValue.addAndGet(otherEntry.getValue().longValue());
			}else{
				countByKey.put(otherEntry.getKey(), new AtomicLong(otherEntry.getValue().longValue()));
			}
		}
	}

	private AtomicLong getOrCreate(String key){
		AtomicLong count = countByKey.get(key);
		if(count != null) {
			return count;
		}
		AtomicLong newVal = new AtomicLong(0L);// could be wasted
		AtomicLong existingVal = countByKey.putIfAbsent(key, newVal);
		return existingVal == null ? newVal : existingVal;
	}

	@Override
	public AtomicCounter getCounter(){
		return this;
	}

	public AtomicCounter deepCopy(){
		AtomicCounter copy = new AtomicCounter(startTimeMs, lengthMs);
		for(Map.Entry<String,AtomicLong> entry : DrMapTool.nullSafe(countByKey).entrySet()){
			copy.countByKey.put(entry.getKey(), new AtomicLong(entry.getValue().longValue()));
		}
		return copy;
	}

	@Override
	public void stopAndFlushAll(){
		//no-op
	}
}
