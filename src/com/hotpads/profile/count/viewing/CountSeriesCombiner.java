package com.hotpads.profile.count.viewing;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class CountSeriesCombiner extends AbstractCountSeries{

	public CountSeriesCombiner(String name, Long startMs, int numPeriods, Long periodMs,
			List<CountSeriesFormatter> paddedCountSeriesList){
		super();
		this.name =name;
		this.startMs = startMs;
		this.numPeriods = numPeriods;
		this.periodMs = periodMs;
		this.endMs = startMs + numPeriods * periodMs;
		this.countSeries = aggregatePaddedCountSerie(paddedCountSeriesList);
	}

	private List<Long> aggregatePaddedCountSerie(List<CountSeriesFormatter> paddedCountSeriesList){
		List<Long> toReturn = fillWithZero();
		for(CountSeriesFormatter paddedCountSeries : paddedCountSeriesList){
			if(checkTimeInformation(paddedCountSeries)){
				for(int index = 0; index <= numPeriods; index++)
					toReturn.set(index, toReturn.get(index) + paddedCountSeries.get(index));
			}
		}
		return toReturn;
	}

	private boolean checkTimeInformation(CountSeriesFormatter paddedCountSeries){
		return paddedCountSeries.getStartMs().equals(startMs) && paddedCountSeries.getEndMs().equals(endMs)
				&& numPeriods == paddedCountSeries.getNumPeriods() && paddedCountSeries.getPeriodMs().equals(periodMs);
	}

}
