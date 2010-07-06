package com.hotpads.profile.count;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicCounter implements Counter {
	
	protected ConcurrentMap<String,AtomicLong> countByKey;
	
	public AtomicCounter(){
		this.countByKey = new ConcurrentHashMap<String,AtomicLong>();
	}
	
	/* (non-Javadoc)
	 * @see com.hotpads.profile.count.Counter#increment(java.lang.String)
	 */
	public long increment(String key){
		return getOrCreate(key).getAndAdd(1);
	}
	
	/* (non-Javadoc)
	 * @see com.hotpads.profile.count.Counter#increment(java.lang.String, long)
	 */
	public long increment(String key, long delta){
		return getOrCreate(key).getAndAdd(delta);
	}
	
	protected AtomicLong getOrCreate(String key){
		AtomicLong count = countByKey.get(key);
		if(count!=null){ return count; }
		AtomicLong newVal = new AtomicLong(0L);//could be wasted
		AtomicLong existingVal = countByKey.putIfAbsent(key, newVal);
		return existingVal==null?newVal:existingVal;
	}
	
}
