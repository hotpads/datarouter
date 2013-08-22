package com.hotpads.profile.count.viewing;

import java.util.List;

import com.google.common.collect.Lists;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.XMLStringTool;
import com.hotpads.util.core.collections.Pair;
@Deprecated
public class CountSeries{

	private Long startMs;
	private Long endMs;
	private Long periodMs;
	private List<Count> counts;
	private String name;
	private String sourceType;
	private String source;

	public CountSeries(Long startMs, Long endMs, Long periodMs, String name, String sourceType, String source,
			List<Count> counts){
		this.startMs = startMs;
		this.endMs = endMs;
		this.periodMs = periodMs;
		this.counts = counts;
		this.name = name;
		this.sourceType = sourceType;
		this.source = source;
	}

	public long getMax(){
		long max = 0L;
		for(Count count : IterableTool.nullSafe(counts)){
			if(count.getValue() > max){
				max = count.getValue();
			}
		}
		return max;
	}

	public double getMaxPerSecond(){
		long max = getMax();
		return ((double)max) / (periodMs / 1000);
	}

	public String getName(){
		return name;
		// if(CollectionTool.isEmpty(counts)){ return null; }
		// return CollectionTool.getFirst(counts).getName();
	}

	public String getNameHtmlEscaped(){
		return XMLStringTool.escapeXml(getName());
	}

	public String getSourceType(){
		return sourceType;
		// if(CollectionTool.isEmpty(counts)){ return null; }
		// return CollectionTool.getFirst(counts).getSourceType();
	}

	public String getSource(){
		return source;
		// if(CollectionTool.isEmpty(counts)){ return null; }
		// return CollectionTool.getFirst(counts).getSource();
	}

	public Long getPeriodMs(){
		return periodMs;
		// if(CollectionTool.isEmpty(counts)){ return null; }
		// return CollectionTool.getFirst(counts).getPeriodMs();
	}

	public List<Count> getPaddedCounts(){
		return Count.getListWithGapsFilled(name, sourceType, source, periodMs, counts, startMs, endMs);
	}

//	public static List<Count> aggregateCountOfCountSeries(List<CountSeries> countSeriesList, String name,
//			String webApp, String server, long periodMs){
//		// Normally here, the valuesPairs have been set to be padded
//		List<Count> counts = ListTool.create();
//		Count aggregatedCount;
//		long value = new Long(0);
//		// Initialization of the variables
//		if(countSeriesList == null || countSeriesList.size() <= 0){ return null; }
//		List<List<Long>> listStartTimesList = getStartTimesListOfCountSeries(countSeriesList);
//		Set<Long> listStartTimes = SetTool.create(ListTool.intersection(listStartTimesList));
//
//		for(Long startTime : listStartTimes){
//			value = new Long(0);
//			for(CountSeries countSeries : countSeriesList){
//				value += countSeries.getCount(startTime).getValue();
//			}
//			aggregatedCount = new Count(name, webApp, periodMs, startTime, server, startTime, value);
//			counts.add(aggregatedCount);
//		}
//		return counts;
//	}

	public Count getCount(Long startTime){
		for(Count count : getCounts()){
			if(count.getStartTimeMs().equals(startTime)){ return count; }
		}
		return null;
	}

	public static List<List<Long>> getStartTimesListOfCountSeries(List<CountSeries> countSeriesList){

		List<List<Long>> toreturn = ListTool.create();
		for(CountSeries countSeries : countSeriesList){
			toreturn.add(countSeries.getStartTimes());
		}

		return toreturn;
	}

	public static Pair<Long,Double> getFirstPair(Long left, List<Pair<Long,Double>> pairs){
		for(Pair<Long,Double> pair : pairs){
			if(pair.getLeft().equals(left)){ return pair; }
		}
		return null;
	}

	/*************** get/set ****************************E *******************/

	public List<Count> getCounts(){
		return counts;
	}

	public void setCounts(List<Count> counts){
		this.counts = counts;
	}

	public List<Long> getStartTimes(){
		List<Long> toReturnTimes = Lists.newLinkedList();
		for(Count count : counts){
			toReturnTimes.add(count.getStartTimeMs());
		}
		return toReturnTimes;
	}

	public Long getFirstStartTimes(){
		if(getStartTimes() == null || getStartTimes().size() == 0){
			return null;
		}else{
			return getStartTimes().get(0);
		}
	}

	public Long getLastStartTimes(){
		if(getStartTimes() == null || getStartTimes().size() == 0){
			return null;
		}else{
			return getStartTimes().get(getStartTimes().size() - 1);
		}
	}

}
