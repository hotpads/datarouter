package com.hotpads.profile.count.collection.archive.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountCollectorPeriod;
import com.hotpads.profile.count.collection.archive.BaseCountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.bytes.StringByteTool;

public class MemoryCountArchive extends BaseCountArchive{

	private final Long startTimeMs;
	private final Integer numToRetain;
	private final Long retainForMs;

	//storage.  caution - currently nothing to make sure there are no old values in the holes
	private final CountCollectorPeriod[] archive;


	public MemoryCountArchive(
			String sourceType,
			String source,
			Long periodMs,
			Integer numToRetain){
		super(sourceType, source, periodMs);
		this.startTimeMs = System.currentTimeMillis();
		this.numToRetain = numToRetain;
		this.retainForMs = periodMs * numToRetain;
		this.archive = new CountCollectorPeriod[this.numToRetain];
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
	public List<AvailableCounter> getAvailableCounters(String namePrefix, String nameWildcard, int limit){
		Set<AvailableCounter> unsorted = new HashSet<>();
		for (CountCollectorPeriod element : archive){
			if(element!=null){
				Map<String,AtomicLong> map = DrMapTool.nullSafe(element.getCountByKey());
				for(Map.Entry<String,AtomicLong> entry : map.entrySet()){
					if(DrStringTool.notEmpty(namePrefix) && ! entry.getKey().startsWith(namePrefix)){
						continue;
					}
					unsorted.add(new AvailableCounter(webApp, periodMs, entry.getKey(), source, element
							.getStartTimeMs()));
				}
			}
		}
		List<AvailableCounter> sorted = DrListTool.createArrayList(unsorted, limit);
		Collections.sort(sorted);
		return sorted;
	}

	@Override
	public Collection<AvailableCounter> getAvailableCountersForWebApp(String namePrefix, String nameWildcard, 
			String webApp, int limit){
		return getAvailableCounters(namePrefix, nameWildcard, limit);
	}
	
	@Override
	@Deprecated
	public Collection<AvailableCounter> getAvailableCountersStartingAt(String startingAt, String namePrefix,
			String nameWildcard, String webApp, int limit){
		return null;
	}
	
	@Override
	public List<Count> getCountsForAllSources(String name, Long startMs, Long endMs){
		return getCountsForWebApp(name, webApp, startMs, endMs);
	}

	@Override
	public List<Count> getCountsForWebApp(String name, String webApp, Long startMs, Long endMs){
		int startIndex = getIndexForMs(startMs);
		if(getEarliestAvailableTime() > startMs){
			startIndex = getIndexForMs(getEarliestAvailableTime());
		}
		List<Count> counts = new ArrayList<>();
		int index = startIndex;
		while(true){
			CountCollectorPeriod period = archive[index];
			if(period==null
					|| period.getStartTimeMs() < startMs
					|| period.getStartTimeMs() > endMs
					|| period.getStartTimeMs() < getEarliestAvailableTime()){//old values that haven't been overwritten
				//do nothing
			}else{
				AtomicLong atomicLong = period.getCountByKey().get(name);
				if(atomicLong!=null){
					Count count = new Count(name, webApp,
							periodMs, period.getStartTimeMs(), source,
							System.currentTimeMillis(), atomicLong.longValue());
					counts.add(count);
				}
			}
			index = getIndexAfter(index);
			if(index==startIndex){
				break;//looped all the way around
			}
		}
		return counts;
	}

	@Override
	@Deprecated
	public List<Count> getCountsForWebAppWithGapsFilled(String name, String webApp, long startMs, long endMs){
		return null;
	}

	@Override
	public void saveCounts(CountCollectorPeriod countMap){
		int index = getIndexForMs(countMap.getStartTimeMs());
		CountCollectorPeriod existingPeriod = archive[index];
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

	private long getWindowStartMs(long ms){
		long toTruncate = ms % periodMs;
		return ms - toTruncate;
	}

	private int getIndexForMs(long ms){
		long periodNumSinceEpoch = getWindowStartMs(ms) / periodMs;
		return (int)(periodNumSinceEpoch % numToRetain);
	}

	private long getEarliestAvailableTime(){
		long now = System.currentTimeMillis();
		long startedAgo = now - startTimeMs;
		if(startedAgo > retainForMs){
			return now - retainForMs;
		}
		return startTimeMs;
	}

	private int getIndexAfter(int index){
		if(index >= archive.length - 1){
			return 0;
		}
		return index+1;
	}

	@Override
	public String getPeriodAbbreviation(){
		return CountPartitionedNode.getSuffix(getPeriodMs());
	}

	@Override
	public Long getNumCounters(){
		long numCounters=0;
		for (CountCollectorPeriod element : archive){
			if(element!=null){
				++numCounters;
			}
		}
		return numCounters;
	}

	@Override
	public Long getNumCounts(){
		long numCounts=0;
		for (CountCollectorPeriod element : archive){
			if(element!=null){
				numCounts+=DrMapTool.size(element.getCountByKey());
			}
		}
		return numCounts;
	}

	@Override
	public Long getNumBytes(){
		long numBytes=0;
		for (CountCollectorPeriod element : archive){
			if(element==null){
				continue;
			}
			Map<String,AtomicLong> countByKey = element.getCountByKey();
			if(countByKey==null){
				continue;
			}
			//should add capacity * BYTES_PER_POINTER, but can't access capacity
			numBytes += AtomicCounter.INITIAL_CAPACITY;
			for(Map.Entry<String,AtomicLong> entry : element.getCountByKey().entrySet()){
				numBytes += DrByteTool.BYTES_PER_HASH_MAP_ENTRY;
				numBytes += StringByteTool.getNumBytesInMemoryWithPointers(entry.getKey());
				numBytes += DrByteTool.BYTES_PER_LONG_WITH_POINTER;
			}
		}
		return numBytes;
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
