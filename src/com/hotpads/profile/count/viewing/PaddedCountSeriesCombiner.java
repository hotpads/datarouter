package com.hotpads.profile.count.viewing;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class PaddedCountSeriesCombiner{
	private Long startMs;
	private Long endMs;
	private int numPeriods;
	private Long periodMs;
	private List<Long> aggregatedCountSeries;

	public PaddedCountSeriesCombiner(Long startMs, int numPeriods, Long periodMs,
			List<PaddedCountSeries> paddedCountSeriesList){
		super();
		this.startMs = startMs;
		this.numPeriods = numPeriods;
		this.periodMs = periodMs;
		this.endMs = startMs + numPeriods * periodMs;
		this.aggregatedCountSeries = aggregatePaddedCountSerie(paddedCountSeriesList);
	}

	private List<Long> aggregatePaddedCountSerie(List<PaddedCountSeries> paddedCountSeriesList){
		List<Long> toReturn = fillWithZero();
 		for(PaddedCountSeries paddedCountSeries : paddedCountSeriesList){
			if(checkTimeInformation(paddedCountSeries)){
				for(int index = 0; index < numPeriods; index++)
				toReturn.set(index, toReturn.get(index) + paddedCountSeries.get(index));
			}
		}
 		return toReturn;
	}

	private List<Long> fillWithZero(){
		List<Long> toReturn = ListTool.createArrayList(numPeriods);
		for(int index = 0; index < numPeriods; index++){
			toReturn.add(new Long(0));
		}

		return toReturn;
	}

	private boolean checkTimeInformation(PaddedCountSeries paddedCountSeries){
		return paddedCountSeries.getStartMs().equals(startMs) && paddedCountSeries.getEndMs().equals(endMs)
				&& numPeriods== paddedCountSeries.getNumPeriods() && paddedCountSeries.getPeriodMs().equals(periodMs);
	}

	public Long getStartTimesFromIndex(int index){
		return startMs + index*periodMs;
	}
	

	public Long getEndMs(){
		return endMs;
	}

	public void setEndMs(Long endMs){
		this.endMs = endMs;
	}

	public int getNumPeriods(){
		return numPeriods;
	}

	public void setNumPeriods(int numPeriods){
		this.numPeriods = numPeriods;
	}

	public Long getPeriodMs(){
		return periodMs;
	}

	public void setPeriodMs(Long periodMs){
		this.periodMs = periodMs;
	}

	public List<Long> getAggregatedCountSeries(){
		return aggregatedCountSeries;
	}

	public void setAggregatedCountSeries(List<Long> aggregatedCountSeries){
		this.aggregatedCountSeries = aggregatedCountSeries;
	}

	/**
	 * @return the startMs
	 */
	public Long getStartMs(){
		return startMs;
	}

	/**
	 * @param startMs the startMs to set
	 */
	public void setStartMs(Long startMs){
		this.startMs = startMs;
	}


}
