package com.hotpads.profile.count.collection.archive.imp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;

public class MemoryCountArchive implements CountArchive{
	static Logger logger = Logger.getLogger(MemoryCountArchive.class);
	
	protected Long startTimeMs;
	protected String sourceType;
	protected String source;
	protected Long periodMs;
	protected Integer numToRetain;
	protected Long retainForMs;
	
	//storage
	protected CountMapPeriod[] archive;//caution - currently nothing to make sure there are no old values in the holes
	
	
	public MemoryCountArchive(
			String sourceType,
			String source,
			Long periodMs,
			Integer numToRetain){
		this.startTimeMs = System.currentTimeMillis();
		this.sourceType = sourceType;
		this.source = source;
		this.periodMs = periodMs;
		this.numToRetain = numToRetain;
		this.retainForMs = periodMs * numToRetain;
		this.archive = new CountMapPeriod[this.numToRetain];
	}
	
	@Override
	public int compareTo(CountArchive that){
		if(ClassTool.differentClass(this, that)){ 
			return ComparableTool.nullFirstCompareTo(this.getClass().getName(), that.getClass().getName()); 
		}
		return (int)(this.getPeriodMs() - that.getPeriodMs());
	}

	@Override
	public List<AvailableCounter> getAvailableCounters(String nameLike){
		Set<AvailableCounter> unsorted = SetTool.createHashSet();
		for(int i=0; i < archive.length; ++i){
			if(archive[i]!=null){
				Map<String,AtomicLong> map = MapTool.nullSafe(archive[i].getCountByKey());
				for(Map.Entry<String,AtomicLong> entry : map.entrySet()){
					if(StringTool.notEmpty(nameLike) && ! entry.getKey().startsWith(nameLike)){
						continue; 
					}
					unsorted.add(new AvailableCounter(entry.getKey(), sourceType, source, 
							periodMs, archive[i].getStartTimeMs()));
				}
			}
		}
		List<AvailableCounter> sorted = ListTool.createArrayList(unsorted);
		Collections.sort(sorted);
		return sorted;
	}
	
	@Override
	public List<Count> getCounts(String name, Long startMs, Long endMs){
		int startIndex = getIndexForMs(startMs);
		if(getEarliestAvailableTime() > startMs){
			startIndex = getIndexForMs(getEarliestAvailableTime());
		}
		List<Count> counts = ListTool.createArrayList();
		int i = startIndex;
		while(true){
			CountMapPeriod period = archive[i];
			if(period==null
					|| period.getStartTimeMs() < startMs 
					|| period.getStartTimeMs() > endMs){
				//do nothing
			}else{
				AtomicLong atomicLong = period.getCountByKey().get(name);
				if(atomicLong!=null){
					Count count = new Count(name, sourceType, source, 
							periodMs, period.getStartTimeMs(), atomicLong.longValue());
					counts.add(count);
				}
			}
			i = getIndexAfter(i);
			if(i==startIndex){ break; }//looped all the way aroud
		}
		return counts;
	}

	@Override
	public void saveCounts(CountMapPeriod countMap){
		int index = getIndexForMs(countMap.getStartTimeMs());
		CountMapPeriod existingPeriod = archive[index];
		long countMapWindowStartMs = getWindowStartMs(countMap.getStartTimeMs());
		if(existingPeriod==null 
				|| existingPeriod.getStartTimeMs() < countMapWindowStartMs
				|| existingPeriod.getStartTimeMs() >= countMapWindowStartMs + periodMs){
//			logger.warn("flushing "+getName()+"["+countMap.getStartTimeMs()+"->"+index+"]");
			AtomicCounter newMap = new AtomicCounter(countMapWindowStartMs, periodMs);
			newMap.merge(countMap);
			archive[index] = newMap;
		}else{
//			logger.warn("merging new counts into "+getName());
			archive[index].getCounter().merge(countMap);
		}
	}

	
	/**************************** convenience **********************************/
	
	protected long getWindowStartMs(long ms){
		long toTruncate = ms % periodMs;
		return ms - toTruncate;
	}
	
	protected int getIndexForMs(long ms){
		long periodNumSinceEpoch = getWindowStartMs(ms) / periodMs;
		return (int)(periodNumSinceEpoch % numToRetain);
	}
	
	protected long getEarliestAvailableTime(){
		long now = System.currentTimeMillis();
		long startedAgo = now - startTimeMs;
		if(startedAgo > retainForMs){
			return now - retainForMs;
		}
		return startTimeMs;
	}
	
	protected int getEarliestIndex(){
		int latestIndex = getIndexForMs(System.currentTimeMillis());
		return getIndexAfter(latestIndex);//prob need a check here
	}
	
	protected int getIndexAfter(int i){
		if(i >= archive.length - 1){
			return 0;
		}
		return i+1;
	}
	
	@Override
	public String getPeriodAbbreviation(){
		return CountPartitionedNode.getSuffix(getPeriodMs());
	}

	/******************************** getters ******************************************/
	
	@Override
	public String getSourceType(){
		return sourceType;
	}

	@Override
	public String getSource(){
		return source;
	}

	@Override
	public long getPeriodMs(){
		return this.periodMs;
	}
	
	@Override
	public String getName(){
		return "memory "+periodMs;
	}
	
	@Override
	public Integer getNumToRetain(){
		return this.numToRetain;
	}
}
