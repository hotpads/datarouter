package com.hotpads.profile.count;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface Counter {
	
	public abstract long getStartTimeMs();
	public abstract long getLengthMs();
	public abstract Map<String,AtomicLong> getCountByKey();

	public abstract long increment(String key);
	public abstract long increment(String key, long delta);
	
	public abstract void merge(Counter other);

}