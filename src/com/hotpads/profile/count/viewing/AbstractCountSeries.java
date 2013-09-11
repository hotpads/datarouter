package com.hotpads.profile.count.viewing;

import java.util.List;

import com.hotpads.profile.count.viewing.PaddedCountSeriesManipulationException.PaddedCountSeriesTypeException;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.XMLStringTool;

public abstract class AbstractCountSeries{
	protected String name;
	protected Long startMs;
	protected Long endMs;
	protected int numPeriods;
	protected Long periodMs;
	protected List<Long> countSeries;

	public long getMax(){
		int max = 0;
		for(int i = 0; i < numPeriods; i++){
			if(countSeries.get(i) > max){
				max = i;
			}
		}
		return get(max);
	}

	public double getMaxPerSecond(){
		long max = getMax();
		return ((double)max) / (periodMs / 1000);
	}

	public long getStartTimeFromIndex(int index){
		return startMs + index * periodMs;
	}

	public static long getStartTimeFromIndex(int index, long startMs, long periodMs){
		return startMs + index * periodMs;
	}

	public Long get(int index){
		return getCountSeries().get(index);
	}
	
	public int getIndexFromStartTime(long countStartTime)
			throws PaddedCountSeriesManipulationException{
		if(countStartTime % periodMs != 0){ throw new PaddedCountSeriesManipulationException(
				PaddedCountSeriesTypeException.WRONG_START_TIME); }
		int toReturn = (int)((countStartTime - startMs) / periodMs);
		return toReturn;
	}
	
	public String getNameHtmlEscaped(){
		return XMLStringTool.escapeXml(getName());
	}
	

	protected List<Long> fillWithZero(){
		List<Long> toReturn = ListTool.createArrayList(numPeriods+1);
		for(int index = 0; index <= numPeriods; index++){
			toReturn.add(new Long(0));
		}

		return toReturn;
	}

	/****************Getter and setter *************************/
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

	public List<Long> getCountSeries(){
		return countSeries;
	}

	public void setCountSeries(List<Long> countSeries){
		this.countSeries = countSeries;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	
}
