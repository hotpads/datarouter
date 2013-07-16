package com.hotpads.profile.count.collection.archive.imp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.collection.archive.BaseCountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class MemoryCountArchive extends BaseCountArchive{
	static Logger logger = Logger.getLogger(MemoryCountArchive.class);
	
	protected Long startTimeMs;
	protected Integer numToRetain;
	protected Long retainForMs;
	
	//storage
	protected CountMapPeriod[] archive;//caution - currently nothing to make sure there are no old values in the holes
	
	
	public MemoryCountArchive(
			String sourceType,
			String source,
			Long periodMs,
			Integer numToRetain){
		super(sourceType, source, periodMs);
		this.startTimeMs = System.currentTimeMillis();
		this.numToRetain = numToRetain;
		this.retainForMs = periodMs * numToRetain;
		this.archive = new CountMapPeriod[this.numToRetain];
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < archive.length; ++i){
			sb.append(i+":");
			if(archive[i]!=null){
				sb.append(archive[i]);
			}
			sb.append("\n");
		}
		return sb.toString();
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
					unsorted.add(new AvailableCounter(sourceType, 
							periodMs, entry.getKey(), source, archive[i].getStartTimeMs()));
				}
			}
		}
		List<AvailableCounter> sorted = ListTool.createArrayList(unsorted);
		Collections.sort(sorted);
		return sorted;
	}
	
	@Override
	public Collection<? extends AvailableCounter> getAvailableCounters(String nameLike, String webApp){
		return getAvailableCounters(nameLike);
	}
		@Override
	public List<Count> getCountsForAllSources(String name, Long startMs, Long endMs){
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
					|| period.getStartTimeMs() > endMs
					|| period.getStartTimeMs() < getEarliestAvailableTime()){//old values that haven't been overwritten
				//do nothing
			}else{
				AtomicLong atomicLong = period.getCountByKey().get(name);
				if(atomicLong!=null){
					Count count = new Count(name, sourceType, 
							periodMs, period.getStartTimeMs(), source, 
							System.currentTimeMillis(), atomicLong.longValue());
					counts.add(count);
				}
			}
			i = getIndexAfter(i);
			if(i==startIndex){ break; }//looped all the way around
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
		long earliestTime = getEarliestAvailableTime();
		int index = getIndexForMs(earliestTime);
		return index;
//		int latestIndex = getIndexForMs(System.currentTimeMillis());
//		return getIndexAfter(latestIndex);//prob need a check here
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
	
	@Override
	public Long getNumCounters(){
		long n=0;
		for(int i=0; i < archive.length; ++i){
			if(archive[i]!=null){
				++n;
			}
		}
		return n;
	}
	
	@Override
	public Long getNumCounts(){
		long n=0;
		for(int i=0; i < archive.length; ++i){
			if(archive[i]!=null){
				n+=MapTool.size(archive[i].getCountByKey());
			}
		}
		return n;
	}
	
	@Override
	public Long getNumBytes(){
		long n=0;
		for(int i=0; i < archive.length; ++i){
			if(archive[i]==null){ continue; }
			Map<String,AtomicLong> countByKey = archive[i].getCountByKey();
			if(countByKey==null){ continue; }
			//should add capacity * BYTES_PER_POINTER, but can't access capacity
			n += AtomicCounter.INITIAL_CAPACITY;
			for(Map.Entry<String,AtomicLong> entry : archive[i].getCountByKey().entrySet()){
				n += ByteTool.BYTES_PER_HASH_MAP_ENTRY;
				n += StringByteTool.getNumBytesInMemoryWithPointers(entry.getKey());
				n += ByteTool.BYTES_PER_LONG_WITH_POINTER;
			}
		}
		return n;
	}

	/******************************** getters ******************************************/
	
	
	@Override
	public String getName(){
		return "memory "+periodMs;
	}
	
	@Override
	public Integer getNumToRetain(){
		return this.numToRetain;
	}
}
