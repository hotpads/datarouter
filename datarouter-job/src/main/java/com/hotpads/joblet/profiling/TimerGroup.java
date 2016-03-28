/**
 * 
 */
package com.hotpads.analytics.profiling;

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
	
	public int getNumEvents(){
		return numEvents;
	}
	
	public long getExecutionTimeSum(){
		return eventDurationSum;
	}
	
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