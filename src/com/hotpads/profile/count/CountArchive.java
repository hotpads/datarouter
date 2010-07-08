package com.hotpads.profile.count;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface CountArchive{

	void saveCounts(long periodMs, long startTimeMs, Map<String,AtomicLong> countByKey);
	List<String> listCounters();
	List<Integer> getCounts(long period, long startTimeMs, String name);
}
