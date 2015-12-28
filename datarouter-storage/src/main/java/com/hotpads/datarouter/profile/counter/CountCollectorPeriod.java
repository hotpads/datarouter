package com.hotpads.datarouter.profile.counter;

public interface CountCollectorPeriod extends CountCollector{

	long getStartTimeMs();
	long getPeriodMs();
	long getNextStartTimeMs();
	
}
