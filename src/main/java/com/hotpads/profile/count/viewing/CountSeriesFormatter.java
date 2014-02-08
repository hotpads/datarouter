package com.hotpads.profile.count.viewing;

import java.util.List;

import com.hotpads.profile.count.databean.Count;

public class CountSeriesFormatter extends AbstractCountSeries{

	public CountSeriesFormatter(String name, Long startMs, int numPeriods, Long periodMs, List<Count> counts)
			throws PaddedCountSeriesManipulationException{
		super();
		this.name = name;
		this.startMs = startMs;
		this.numPeriods = numPeriods;
		this.periodMs = periodMs;
		this.endMs = startMs + numPeriods * periodMs;
		this.countSeries = initializePaddedCountSeries(counts);
		this.average = calculateAverage();
		;
	}

	private List<Long> initializePaddedCountSeries(List<Count> counts) throws PaddedCountSeriesManipulationException{
		List<Long> toReturn = fillWithZero();
		int index;
		for(Count count : counts){
			if(shouldBeInclude(count)){
				index = getIndexFromStartTime(count.getStartTimeMs());
				toReturn.set(index, count.getValue());
			}
		}
		return toReturn;
	}

	private boolean shouldBeInclude(Count count){
		return count.getStartTimeMs() >= startMs && count.getStartTimeMs() <= endMs
				&& count.getStartTimeMs() % periodMs == 0;
	}

}
