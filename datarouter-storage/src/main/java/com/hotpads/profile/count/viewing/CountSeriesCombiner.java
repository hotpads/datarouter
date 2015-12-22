package com.hotpads.profile.count.viewing;

import java.util.List;

public class CountSeriesCombiner extends AbstractCountSeries{

	public CountSeriesCombiner(String name, Long startMs, int numPeriods, Long periodMs,
			List<CountSeriesFormatter> countSeriesFormatters){
		super();
		this.name =name;
		this.startMs = startMs;
		this.numPeriods = numPeriods;
		this.periodMs = periodMs;
		this.endMs = startMs + numPeriods * periodMs;
		this.countSeries = aggregateCountSeries(countSeriesFormatters);
		this.average = calculateAverage();
	}

	private List<Long> aggregateCountSeries(List<CountSeriesFormatter> countSeriesFormatters){
		List<Long> countSeries = fillWithZero();
		for(CountSeriesFormatter countSeriesFormatter : countSeriesFormatters){
			if(checkTimeInformation(countSeriesFormatter)){
				for(int index = 0; index <= numPeriods; index++)
					countSeries.set(index, countSeries.get(index) + countSeriesFormatter.get(index));
			}
		}
		return countSeries;
	}

	private boolean checkTimeInformation(CountSeriesFormatter countSeriesFormatter){
		return countSeriesFormatter.getStartMs().equals(startMs) && countSeriesFormatter.getEndMs().equals(endMs)
				&& numPeriods == countSeriesFormatter.getNumPeriods() && countSeriesFormatter.getPeriodMs().equals(periodMs);
	}

}
