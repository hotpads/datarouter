package com.hotpads.analytics.profiling;

import java.util.Deque;
import java.util.Queue;

import com.hotpads.util.core.profile.PhaseTimer;


public abstract class StratifiedStatistics implements Statistics{

	protected final int NUMBER_OF_STRATA;
	
	protected TimerGroup currentStratum;
	
	protected Deque<TimerGroup> stratifiedEvents;
	protected Deque<String> stratumNames;

	public StratifiedStatistics(int numStrata) {
		this.NUMBER_OF_STRATA = numStrata;
	}
	
	public Queue<String> getStratumNames() {
		return stratumNames;
	}
	
	public Queue<TimerGroup> getStratifiedEvents() {
		return stratifiedEvents;
	}
	
	@Override
	public long getAverageExecutionTimeMillis() {
		if(getNumEvents() == 0){
			return 0;
		}
		return getExecutionTimeSum() / getNumEvents();
	}
	
	@Override
	public long getExecutionTimeSum() {
		long sum = 0;
		for(TimerGroup group : stratifiedEvents){
			sum += group.getExecutionTimeSum();
		}
		return sum;
	}
	
	@Override
	public int getNumEvents() {
		int numEvents = 0;
		for(TimerGroup group : stratifiedEvents){
			numEvents += group.getNumEvents();
		}
		return numEvents;
	}
	
	@Override
	public void logEvent(PhaseTimer timer) {
		createNewStrataIfNeeded(timer);
		currentStratum.logEvent(timer);
	}

	protected abstract void createNewStrataIfNeeded(PhaseTimer timer);
	
}
