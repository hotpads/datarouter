package com.hotpads.datarouter.profile.counter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface CountCollector{

	void stopAndFlushAll();

	long increment(String key);
	long increment(String key, long delta);

	AtomicCounter getCounter();
	Map<String,AtomicLong> getCountByKey();

}