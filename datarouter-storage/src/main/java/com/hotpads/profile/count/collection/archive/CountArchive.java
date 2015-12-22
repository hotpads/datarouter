package com.hotpads.profile.count.collection.archive;

import java.util.Collection;
import java.util.List;

import com.hotpads.profile.count.collection.CountCollectorPeriod;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;

public interface CountArchive extends Comparable<CountArchive>{

	void saveCounts(CountCollectorPeriod countMap);

	String getName();
	long getPeriodMs();
	String getPeriodAbbreviation();
	String getSourceType();
	String getSource();
	Integer getNumToRetain();
	
	Long getNumCounters();
	Long getNumCounts();
	Long getNumBytes();
	
	List<AvailableCounter> getAvailableCounters(String nameLike, String nameWildcard, int limit);
	Collection<AvailableCounter> getAvailableCountersForWebApp(String nameLike, String nameWildcard, String webApp, 
			int limit);
	Collection<AvailableCounter> getAvailableCountersStartingAt(String startingAt, String namePrefix,
			String nameWildcard, String webApp, int limit);
	
	List<Count> getCountsForAllSources(String name, Long startMs, Long endMs);
	List<Count> getCountsForWebApp(String name, String webApp, Long startMs, Long endMs);
	List<Count> getCountsForWebAppWithGapsFilled(String name, String webApp, long rangeStartMs, long rangeEndMs);



}
