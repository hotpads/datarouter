package com.hotpads.profile.count.collection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface CountMap {
	
	public Map<String,AtomicLong> getCountByKey();

	public long increment(String key);
	public long increment(String key, long delta);
	public AtomicCounter getCounter();

	public AtomicCounter deepCopy();
}