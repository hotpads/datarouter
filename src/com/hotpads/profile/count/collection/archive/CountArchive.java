package com.hotpads.profile.count.collection.archive;

import java.util.List;

import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;

public interface CountArchive extends Comparable<CountArchive>{

	void saveCounts(CountMapPeriod countMap);

	String getName();
	long getPeriodMs();
	String getSourceType();
	String getSource();
	Integer getNumToRetain();
	
	Long getNumCounters();
	Long getNumCounts();
	Long getNumBytes();
	
	List<AvailableCounter> getAvailableCounters(String nameLike);
	List<Count> getCounts(String name, Long startMs, Long endMs);
	
	String getPeriodAbbreviation();
}
