package com.hotpads.profile.count.collection.archive.imp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.collection.archive.CountArchive;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class DatabeanCountArchive implements CountArchive{
	static Logger logger = Logger.getLogger(DatabeanCountArchive.class);
	
	protected String sourceType;
	protected String source;
	protected Long periodMs;
	protected AtomicCounter aggregator;
	
	protected DataRouter router;
	protected SortedStorageNode<CountKey,Count> countNode;
	protected SortedStorageNode<AvailableCounterKey,AvailableCounter> availableCounterNode;
	
	public DatabeanCountArchive(
			DataRouter router,
			SortedStorageNode<CountKey,Count> countNode,
			SortedStorageNode<AvailableCounterKey,AvailableCounter> availableCounterNode,
			String sourceType,
			String source,
			Long periodMs){
		this.router = router;
		this.countNode = countNode;
		this.availableCounterNode = availableCounterNode;
		this.sourceType = sourceType;
		this.source = source;
		this.periodMs = periodMs;
		this.aggregator = new AtomicCounter(DateTool.getPeriodStart(periodMs), periodMs);
	}

	@Override
	public String getSourceType(){
		return sourceType;
	}

	@Override
	public String getSource(){
		return source;
	}
	
	@Override
	public String getName(){
		return "databean "+periodMs;
	}

	@Override
	public long getPeriodMs(){
		return this.periodMs;
	}

	@Override
	public List<AvailableCounter> getAvailableCounters(String nameLike){
		AvailableCounterKey prefix = new AvailableCounterKey(sourceType, source, periodMs, nameLike);
		List<AvailableCounter> counters = availableCounterNode.getWithPrefix(prefix, true, null);
		Collections.sort(counters);
		return counters;
	}
	
	@Override
	public List<Count> getCounts(String name, Long startMs, Long endMs){
		CountKey start = new CountKey(name, sourceType, source, periodMs, startMs);
		CountKey end = new CountKey(name, sourceType, source, periodMs, System.currentTimeMillis());
		List<Count> counts = countNode.getRange(start, true, end, true, null);
		return counts;
	}
	
	public static final int DISCARD_IF_OLDER_THAN = 300 * 1000;

	@Override
	public void saveCounts(CountMapPeriod countMap){
		if(countMap.getStartTimeMs() < (System.currentTimeMillis() - DISCARD_IF_OLDER_THAN)){
			//don't let them build up in memory for too long (datastore may hiccup)
			logger.warn("databean count archive flushing too slowly, discarding countMap older than:"+
					DISCARD_IF_OLDER_THAN+" ms");
			return;
		}
		if(countMap.getStartTimeMs() >= (aggregator.getStartTimeMs() + periodMs)){//flush the aggregator
//			logger.warn("flushing "+getName());
			AtomicCounter oldAggregator = aggregator;
			long periodStart = DateTool.getPeriodStart(countMap.getStartTimeMs(), periodMs);
			aggregator = new AtomicCounter(periodStart, periodMs);
			List<Count> toSave = ListTool.create();
			for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(oldAggregator.getCountByKey()).entrySet()){
				if(entry.getValue()==null || entry.getValue().equals(0L)){ continue; }
				toSave.add(new Count(entry.getKey(), sourceType, source, 
						periodMs, periodStart, entry.getValue().get()));
			}
			countNode.putMulti(toSave, null);
			flushAvailableCounters(countMap.getCountByKey());
		}
//		logger.warn("merging new counts into "+getName());
		aggregator.merge(countMap);
	}

	

	
	protected void flushAvailableCounters(Map<String,AtomicLong> countByKey){
		List<AvailableCounter> toSave = ListTool.createLinkedList();
		for(Map.Entry<String,AtomicLong> entry : MapTool.nullSafe(countByKey).entrySet()){
			toSave.add(new AvailableCounter(
					entry.getKey(), sourceType, source, 
					periodMs, System.currentTimeMillis()));
		}
		availableCounterNode.putMulti(toSave, null);
	}
	
	
}
