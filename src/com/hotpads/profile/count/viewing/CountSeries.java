package com.hotpads.profile.count.viewing;

import java.util.List;

import com.google.common.collect.Lists;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.XMLStringTool;
import com.hotpads.util.core.collections.Pair;

public class CountSeries{

	private Long startMs;
	private Long endMs;
	private Long periodMs;
	private List<Count> counts;
	private String name;
	private String sourceType;
	private String source;
	private List<Pair<Long,Double>> valuesPairs;

	public CountSeries(Long startMs, Long endMs, Long periodMs,
			String name, String sourceType, String source, List<Count> counts){
		this.startMs = startMs;
		this.endMs = endMs;
		this.periodMs = periodMs;
		this.counts = counts;
		this.name = name;
		this.sourceType = sourceType;
		this.source = source;
	}

	public CountSeries(List<Pair<Long,Double>> valuesPairs, Long startMs, Long endMs, Long periodMs,
			String name, String sourceType, String source){
		this.startMs = startMs;
		this.endMs = endMs;
		this.periodMs = periodMs;
		this.name = name;
		this.sourceType = sourceType;
		this.source = source;
		this.valuesPairs = valuesPairs;
		this.counts = initializeCounts();

	}
	
private List<Count> initializeCounts(){
		List<Count> counts = ListTool.create();
		for(Pair<Long,Double> pair : valuesPairs){
			//TODO check value of created
			counts.add(new Count(name, sourceType, periodMs, pair.getLeft(), source,pair.getLeft(), new Long(pair.getRight().toString())));
		}
		
		return counts;
	}

//	public CountSeries(Long startMs, Long endMs, Long periodMs, List<Count> counts){
//		this.startMs = startMs;
//		this.endMs = endMs;
//		this.periodMs = periodMs;
//		this.counts = counts;
//		for(Count count : IterableTool.nullSafe(counts)){
//			if(count.getName()!=null){ this.name = count.getName(); }
//			if(count.getSourceType()!=null){ this.sourceType = count.getSourceType(); }
//			if(count.getSource()!=null){ this.source = count.getSource(); }
//		}
//	}

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
//		if(CollectionTool.isEmpty(counts)){ return null; }
//		return CollectionTool.getFirst(counts).getName();
	}
	
	public String getNameHtmlEscaped(){
		return XMLStringTool.escapeXml(getName());
	}
	
	public String getSourceType(){
		return sourceType;
//		if(CollectionTool.isEmpty(counts)){ return null; }
//		return CollectionTool.getFirst(counts).getSourceType();
	}
	
	public String getSource(){
		return source;
//		if(CollectionTool.isEmpty(counts)){ return null; }
//		return CollectionTool.getFirst(counts).getSource();
	}
	
	public Long getPeriodMs(){
		return periodMs;
//		if(CollectionTool.isEmpty(counts)){ return null; }
//		return CollectionTool.getFirst(counts).getPeriodMs();
	}
	
	public List<Count> getPaddedCounts(){
		return Count.getListWithGapsFilled(name, sourceType, source, periodMs, counts, startMs, endMs);
	}
	
	
	
	/*************** get/set ***********************************************/
	
	public List<Count> getCounts(){
		return counts;
	}

	public void setCounts(List<Count> counts){
		this.counts = counts;
	}

	public List<Double> getValues(){
		List<Double> toReturnValues = Lists.newLinkedList();
		for(Pair pair : getValuesPairs()){
			toReturnValues.add((Double)pair.getRight());
		}
		return toReturnValues;
	}

	public List<Long> getStartTimes(){
		List<Long> toReturnTimes = Lists.newLinkedList();
		for(Pair pair : getValuesPairs()){
			toReturnTimes.add((Long)pair.getLeft());
		}
		return toReturnTimes;
	}
	
	public List<Pair<Long,Double>> getValuesPairs(){
		return valuesPairs;
	}

	public void setValuesPairs(List<Pair<Long,Double>> valuesPairs){
		this.valuesPairs = valuesPairs;
	}
	
	
}
