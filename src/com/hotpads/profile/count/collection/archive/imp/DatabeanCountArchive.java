package com.hotpads.profile.count.collection.archive.imp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.collection.archive.BaseCountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class DatabeanCountArchive extends BaseCountArchive{
	static Logger logger = Logger.getLogger(DatabeanCountArchive.class);
	
	protected AtomicCounter aggregator;
	protected Long flushPeriodMs;
	protected Long lastFlushMs;//need to anchor this to the period... currently floating
	
	protected SortedMapStorage<CountKey,Count> countNode;
	protected SortedMapStorage<AvailableCounterKey,AvailableCounter> availableCounterNode;
	
	public DatabeanCountArchive(
			SortedMapStorage<CountKey,Count> countNode,
			SortedMapStorage<AvailableCounterKey,AvailableCounter> availableCounterNode,
			String sourceType,
			String source,
			Long periodMs,
			Long flushPeriodMs){
		super(sourceType, source, periodMs);
		this.countNode = countNode;
		this.availableCounterNode = availableCounterNode;
		this.aggregator = new AtomicCounter(DateTool.getPeriodStart(periodMs), periodMs);
		this.flushPeriodMs = flushPeriodMs;
		this.lastFlushMs = System.currentTimeMillis();
	}

	@Override
	public List<AvailableCounter> getAvailableCounters(String nameLike){
		AvailableCounterKey prefix = new AvailableCounterKey(sourceType, periodMs, nameLike, null);
		List<AvailableCounter> counters = availableCounterNode.getWithPrefix(prefix, true, null);
		Collections.sort(counters);
		return counters;
	}
	
	/*
	 * remember that sources are interleaved, so a range query will return all sources
	 */
	@Override
	public List<Count> getCountsForAllSources(String name, Long startMs, Long endMs){
		CountKey start = new CountKey(name, sourceType, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, sourceType, periodMs, System.currentTimeMillis(), null, null);
		List<Count> counts = countNode.getRange(start, true, end, true, null);
		return counts;
	}
	
//	@Override
//	public List<Count> getCountsForSource(String name, String filterForSource, Long startMs, Long endMs){
//		CountKey start = new CountKey(name, sourceType, periodMs, startMs, null, 0L);
//		CountKey end = new CountKey(name, sourceType, periodMs, System.currentTimeMillis(), null, Long.MAX_VALUE);
//		List<Count> counts = countNode.getRange(start, true, end, true, null);
//		List<Count> countsForSource = Count.filterForSource(counts, filterForSource);
//		return countsForSource;
//	}
	
	public static final int DISCARD_IF_OLDER_THAN = 300 * 1000;

	
	@Override
	public void saveCounts(CountMapPeriod countMap){
		if(countMap.getStartTimeMs() < (System.currentTimeMillis() - DISCARD_IF_OLDER_THAN)){
			//don't let them build up in memory for too long (datastore may hiccup)
			logger.warn("databean count archive flushing too slowly, discarding countMap older than:"+
					DISCARD_IF_OLDER_THAN+" ms");
			return;
		}
		if(!shouldFlush(countMap)){
	//		logger.warn("merging new counts into "+getName());
			aggregator.merge(countMap);
			return;
		}
		
//		logger.warn("flushing "+getName());
		AtomicCounter oldAggregator = aggregator;
		long periodStart = DateTool.getPeriodStart(countMap.getStartTimeMs(), periodMs);
		aggregator = new AtomicCounter(periodStart, periodMs);
		List<Count> toSave = ListTool.create();
		for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(oldAggregator.getCountByKey()).entrySet()){
			if(entry.getValue()==null || entry.getValue().equals(0L)){ continue; }
			toSave.add(new Count(entry.getKey(), sourceType, 
					periodMs, periodStart, source, System.currentTimeMillis(), entry.getValue().get()));
		}
		if(countNode!=null){
			logger.warn("saving "+CollectionTool.size(toSave)+" counts to database from thread "
					+Thread.currentThread().getId()+" "+Thread.currentThread().getName());
			countNode.putMulti(toSave, new Config()
					.setPersistentPut(false)
					.setIgnoreNullFields(true)
					.setTimeout(10, TimeUnit.SECONDS)
					.setNumAttempts(1));
			flushAvailableCounters(countMap.getCountByKey());
		}
		lastFlushMs = System.currentTimeMillis();
	}

	
	protected void flushAvailableCounters(Map<String,AtomicLong> countByKey){
		List<AvailableCounter> toSave = ListTool.createLinkedList();
		for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(countByKey).entrySet()){
			toSave.add(new AvailableCounter(sourceType, periodMs, entry.getKey(), 
					source, System.currentTimeMillis()));
		}
		availableCounterNode.putMulti(toSave, new Config()
				.setPersistentPut(false)
				.setIgnoreNullFields(true)
				.setTimeout(10, TimeUnit.SECONDS)
				.setNumAttempts(6));
	}
	
	protected boolean shouldFlush(CountMapPeriod countMap){
		long periodEndsMs = aggregator.getStartTimeMs() + periodMs;
		boolean newPeriod = countMap.getStartTimeMs() >= periodEndsMs;
		if(newPeriod){ return true; }
		
		long nextFlushMs = lastFlushMs + flushPeriodMs;
		long now = System.currentTimeMillis();
		if(now > nextFlushMs){ 
//			logger.warn("early flush of "+this.getName());
			return true; 
		}
		
		return false;
	}

	
	@Override
	public String getName(){
		return "databean "+periodMs;
	}
	
	@Override
	public Integer getNumToRetain(){
		return Integer.MAX_VALUE;//probably should remove this method from the CountArchive interface
	}

	@Override
	public String getPeriodAbbreviation(){
		return CountPartitionedNode.getSuffix(getPeriodMs());
	}
	
	@Override
	public Long getNumCounters(){
		return null;
	}
	
	@Override
	public Long getNumCounts(){
		return null;
	}
	
	@Override
	public Long getNumBytes(){
		return null;
	}
	
}
