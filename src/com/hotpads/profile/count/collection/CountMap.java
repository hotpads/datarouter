package com.hotpads.profile.count.collection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface CountMap {
	
	public abstract Map<String,AtomicLong> getCountByKey();

	public abstract long increment(String key);
	public abstract long increment(String key, long delta);
	public abstract AtomicCounter getCounter();

}