package com.hotpads.profile.count.viewing;

import java.util.List;

import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.viewing.PaddedCountSeriesManipulationException.PaddedCountSeriesTypeException;
import com.hotpads.util.core.ListTool;

public class PaddedCountSeries{

	private Long startMs;
	private Long endMs;
	private int numPeriods;
	private Long periodMs;
	private List<Long> paddedCountSeries;

	public PaddedCountSeries(Long startMs, int numPeriods, Long periodMs, List<Count> counts)
			throws PaddedCountSeriesManipulationException{
		super();
		this.startMs = startMs;
		this.numPeriods = numPeriods;
		this.periodMs = periodMs;
		this.endMs = startMs + numPeriods * periodMs;
		this.paddedCountSeries = initializePaddedCountSeries(counts);
	}

	private List<Long> initializePaddedCountSeries(List<Count> counts) throws PaddedCountSeriesManipulationException{
		List<Long> toReturn;
		toReturn = ListTool.createArrayList(counts.size());
		int index;
		for(Count count : counts){
			if(shouldBeInclude(count)){
				index = calculateIndexAccordingToCountStartTime(count.getStartTimeMs());
				toReturn.add(index, count.getValue());
			}
		}
		return toReturn;
	}

	private boolean shouldBeInclude(Count count){
		return count.getStartTimeMs()>=startMs && count.getStartTimeMs()<= endMs && count.getStartTimeMs()%periodMs==0;
	}

	private int calculateIndexAccordingToCountStartTime(long countStartTime)
			throws PaddedCountSeriesManipulationException{
		if(countStartTime % periodMs != 0){ throw new PaddedCountSeriesManipulationException(
				PaddedCountSeriesTypeException.WRONG_START_TIME); }
		return (int)((countStartTime - startMs) / periodMs);
	}

	public Long getStartMs(){
		return startMs;
	}

	public void setStartMs(Long startMs){
		this.startMs = startMs;
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

	public List<Long> getPaddedCountSeries(){
		return paddedCountSeries;
	}

	public void setPaddedCountSeries(List<Long> paddedCountSeries){
		this.paddedCountSeries = paddedCountSeries;
	}

	public Long get(int index){
		return getPaddedCountSeries().get(index);
	}

}
