package com.hotpads.profile.count.collection;

public interface CountCollectorPeriod extends CountCollector{

	long getStartTimeMs();
	long getPeriodMs();
	long getNextStartTimeMs();
	
}
