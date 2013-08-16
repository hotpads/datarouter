package com.hotpads.profile.count.viewing;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
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

	public CountSeries(Long startMs, Long endMs, Long periodMs, String name, String sourceType, String source,
			List<Count> counts){
		this.startMs = startMs;
		this.endMs = endMs;
		this.periodMs = periodMs;
		this.counts = counts;
		this.name = name;
		this.sourceType = sourceType;
		this.source = source;
		this.valuesPairs = iniializeValuesPair();
	}

	public CountSeries(List<Pair<Long,Double>> valuesPairs, Long startMs, Long endMs, Long periodMs, String name,
			String sourceType, String source){
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
			// TODO check value of created
			counts.add(new Count(name, sourceType, periodMs, pair.getLeft(), source, pair.getLeft(), pair.getRight()
					.longValue()));
		}

		return counts;
	}

	private List<Pair<Long,Double>> iniializeValuesPair(){
		List<Pair<Long,Double>> toReturn = ListTool.create();
		if(counts == null){ return null; }
		for(Count count : counts){
			toReturn.add(new Pair<Long,Double>(count.getStartTimeMs(), new Double(count.getValue().toString())));
		}
		return toReturn;
	}

	// public CountSeries(Long startMs, Long endMs, Long periodMs, List<Count> counts){
	// this.startMs = startMs;
	// this.endMs = endMs;
	// this.periodMs = periodMs;
	// this.counts = counts;
	// for(Count count : IterableTool.nullSafe(counts)){
	// if(count.getName()!=null){ this.name = count.getName(); }
	// if(count.getSourceType()!=null){ this.sourceType = count.getSourceType(); }
	// if(count.getSource()!=null){ this.source = count.getSource(); }
	// }
	// }

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

	public List<Pair<Long,Double>> getPaddedValues(){
		int numPoints = (int)((endMs - startMs) / periodMs);
		List<Pair<Long,Double>> outs = ListTool.createArrayList(numPoints + 1);
		long intervalStart = startMs;
		Iterator<Pair<Long,Double>> i = valuesPairs.iterator();
		Pair<Long,Double> next = IterableTool.next(i);
		int numMatches=0, numNull=0, numOutOfRange=0;
		if(endMs<=startMs){
			return null;
		}
		while(intervalStart <= endMs){
			if(next != null && ((Long)next.getLeft()).equals(intervalStart)){
				if(CollectionTool.notEmpty(outs)){
					Pair<Long,Double> last = CollectionTool.getLast(outs);
					if(((Long)last.getLeft()).equals((Long)next.getLeft())){
						last.setRight((last.getRight() + next.getRight()));// increment
					}else{
						outs.add(next);
					}
				}else{
					outs.add(next);
				}
				next = IterableTool.next(i);
				++numMatches;
			}else{
				if(next==null){ ++numNull; }
				else{ ++numOutOfRange; }
				outs.add(new Pair<Long,Double>(System.currentTimeMillis(), 0D));
			}
			if(next==null || (Long)next.getLeft() > intervalStart){
				intervalStart += periodMs;
			}
		}
		return outs;
	}

	public static void setCountSeriesWithPaddedCounts(List<CountSeries> countSeriesList){
		for(CountSeries countSeries : countSeriesList){
			List<Pair<Long,Double>> counts = countSeries.getPaddedValues();
			countSeries.setValuesPairs(counts);
		}

	}

	public static List<Pair<Long,Double>> aggregateCountOfCountSeries(List<CountSeries> countSeries){
		// Normally here, the valuesPairs have been set to be padded
		setCountSeriesWithPaddedCounts(countSeries);
		List<Pair<Long,Double>> counts = ListTool.create();
		Set<Long> listStartTimes = combineStartTimes(countSeries);
		Pair<Long,Double> pair;
		for(Long startTime : listStartTimes){
			pair = new Pair<Long,Double>(new Long(0), new Double(0));
			for(CountSeries countSerie : countSeries){
				Pair<Long,Double> pairTemp = getFirstPair(startTime, countSerie.valuesPairs);
				if(pair != null && pairTemp != null && pair.getRight() != null && pairTemp.getRight() != null){
					pair.setLeft(startTime);
					pair.setRight(pair.getRight() + pairTemp.getRight());

				}
			}
			counts.add(pair);
		}
		return counts;
	}

	private static Set<Long> combineStartTimes(List<CountSeries> countSeries){
		Set<Long> toReturn = SetTool.create();
		for(CountSeries countSerie : countSeries){
			for(Pair<Long,Double> pair : countSerie.getValuesPairs()){
				toReturn.add(pair.getLeft());
			}
		}
		return toReturn;
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
