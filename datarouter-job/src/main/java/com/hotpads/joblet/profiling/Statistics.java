package com.hotpads.joblet.profiling;

import com.hotpads.util.core.profile.PhaseTimer;

public interface Statistics {

	long getAverageExecutionTimeMillis();
	long getExecutionTimeSum();
	int getNumEvents();

	void logEvent(PhaseTimer timer);

}
