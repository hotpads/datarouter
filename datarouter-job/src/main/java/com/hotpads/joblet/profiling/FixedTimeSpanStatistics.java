package com.hotpads.joblet.profiling;

import java.util.Calendar;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.collections.LimitedLinkedList;
import com.hotpads.util.core.profile.PhaseTimer;

public class FixedTimeSpanStatistics extends StratifiedStatistics{

	int stratumIndex;

	public FixedTimeSpanStatistics(int numStrata) {
		super(numStrata);
		stratifiedEvents = new LimitedLinkedList<>(NUMBER_OF_STRATA);
		stratumNames = new LimitedLinkedList<>(NUMBER_OF_STRATA);
	}

	@Override
	protected void createNewStrataIfNeeded(PhaseTimer timer) {
		int currentStratumIndex = calculateCurrentStratumIndex();
		while(currentStratum == null || stratumIndex != currentStratumIndex){
			if(currentStratum == null){
				stratumIndex=currentStratumIndex;
			}
			else{
				stratumIndex = (stratumIndex+1) % NUMBER_OF_STRATA;
			}
			currentStratum = new TimerGroup();
			String stratumName = generateTimeForStratum(currentStratumIndex);
			stratumNames.offerLast(stratumName);
			stratifiedEvents.offerLast(currentStratum);
		}
	}

	private String generateTimeForStratum(int currentStratumIndex) {
		int numMinutes = 60*24*currentStratumIndex/NUMBER_OF_STRATA;
		return numMinutes / 60 + ":" + DrStringTool.pad(numMinutes%60+"", '0', 2);
	}

	private int calculateCurrentStratumIndex() {
		Calendar now = Calendar.getInstance();
		int minutesIntoTheDay = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);
		return (int)(minutesIntoTheDay / (60*24 / (double)NUMBER_OF_STRATA));
	}

}
