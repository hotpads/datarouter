package com.hotpads.joblet.profiling;

import com.hotpads.util.core.profile.PhaseTimer;

public class TimerGroup implements Statistics{


	//private final StratifiedStatistics stratifiedStatistics;

	//private List<PhaseTimer> timers;

	private int numEvents = 0;
	private long eventDurationSum = 0;

	public TimerGroup() {
		//this.stratifiedStatistics = stratifiedStatistics;
		//this.timers = new LinkedList<PhaseTimer>();
	}

	@Override
	public int getNumEvents(){
		return numEvents;
	}

	@Override
	public long getExecutionTimeSum(){
		return eventDurationSum;
	}

	@Override
	public long getAverageExecutionTimeMillis(){
		if(numEvents == 0){
			return eventDurationSum;
		}
		return eventDurationSum/numEvents;
	}

	@Override
	public void logEvent(PhaseTimer timer) {
		//timers.add(timer);
		numEvents++;
		eventDurationSum += timer.getElapsedTimeBetweenFirstAndLastEvent();
	}
}