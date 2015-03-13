package com.hotpads.profile.count.collection.archive.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.profile.count.collection.archive.BaseCountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.collection.predicate.FilterCountByServer;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class DatabeanCountArchive extends BaseCountArchive{
	static Logger logger = LoggerFactory.getLogger(DatabeanCountArchive.class);

	static long MIN_EARLY_FLUSH_PERIOD_MS = 20 * 1000;// prevent the 5s counter from early flushing because of slight
														// lag

	protected AtomicCounter aggregator;
	protected Long flushPeriodMs;
	protected Long lastFlushMs;// need to anchor this to the period... currently floating

	protected SortedMapStorage<CountKey,Count> countNode;
	protected SortedMapStorage<AvailableCounterKey,AvailableCounter> availableCounterNode;

	public DatabeanCountArchive(SortedMapStorage<CountKey,Count> countNode,
			SortedMapStorage<AvailableCounterKey,AvailableCounter> availableCounterNode, String sourceType,
			String source, Long periodMs, Long flushPeriodMs){
		super(sourceType, source, periodMs);
		this.countNode = countNode;
		this.availableCounterNode = availableCounterNode;
		this.aggregator = new AtomicCounter(DrDateTool.getPeriodStart(periodMs), periodMs);
		this.flushPeriodMs = flushPeriodMs;
		this.lastFlushMs = System.currentTimeMillis();
	}

	@Override
	public List<AvailableCounter> getAvailableCounters(String nameLike){
		AvailableCounterKey prefix = new AvailableCounterKey(webApp, periodMs, nameLike, null);
		Config configLongTimeout = new Config().setTimeout(1, TimeUnit.MINUTES);
		List<AvailableCounter> counters = availableCounterNode.getWithPrefix(prefix, true, configLongTimeout);
		return counters;
	}

	@Override
	public Collection<AvailableCounter> getAvailableCounters(String nameLike, String webApp){
		AvailableCounterKey prefix = new AvailableCounterKey(webApp, periodMs, nameLike, null);
		Config configLongTimeout = new Config().setTimeout(1, TimeUnit.MINUTES);
		List<AvailableCounter> counters = availableCounterNode.getWithPrefix(prefix, true, configLongTimeout);
		return counters;
	}
	@Override
	public Collection<AvailableCounter> getAvailableCountersStartingAt(String startingAt, String webApp){
		Config configLongTimeout = new Config().setTimeout(1, TimeUnit.MINUTES);
		AvailableCounterKey start = new AvailableCounterKey(webApp, this.periodMs, startingAt, null);
		AvailableCounterKey end = new AvailableCounterKey(webApp, this.periodMs, null, null);
		SortedScannerIterable<AvailableCounter> counters = availableCounterNode.scan(Range.create(start, true, end, true),
				configLongTimeout);
		return DrListTool.createArrayList(counters.iterator());
	}

	
	/* remember that sources are interleaved, so a range query will return all sources */
	@Override
	public List<Count> getCountsForAllSources(String name, Long startMs, Long endMs){
		CountKey start = new CountKey(name, webApp, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, webApp, periodMs, System.currentTimeMillis(), null, null);
		PhysicalSortedMapStorageNode<CountKey,Count> physicalSortedMapStorageNode = ((CountPartitionedNode)countNode)
				.getPhysicalNode(start);
		SortedScannerIterable<Count> scanner = physicalSortedMapStorageNode.scan(Range.create(start, true, end, true), null);
		return Count.getListWithGapsFilled(name, webApp, getSource(), periodMs, scanner, startMs, endMs);

	}

	@Override
	public List<Count> getCountsForWebApp(String name, String WebApp, Long startMs, Long endMs){
		CountKey start = new CountKey(name, WebApp, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, WebApp, periodMs, System.currentTimeMillis(), null, Long.MAX_VALUE);
		PhysicalSortedMapStorageNode<CountKey,Count> physicalSortedMapStorageNode = ((CountPartitionedNode)countNode)
				.getPhysicalNode(start);
		SortedScannerIterable<Count> scanner = physicalSortedMapStorageNode.scan(Range.create(start, true, end, true), null);
		Predicate<Count> predicate = new FilterCountByServer(source);
		Iterable<Count> filtered = Iterables.filter(scanner, predicate);
		return DrListTool.createArrayList(filtered);
	}

	@Override
	public List<Count> getCountsForWebAppWithGapsFilled(String name, String WebApp, long startMs, long endMs){
		CountKey start = new CountKey(name, WebApp, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, WebApp, periodMs, System.currentTimeMillis(), null, Long.MAX_VALUE);
		PhysicalSortedMapStorageNode<CountKey,Count> physicalSortedMapStorageNode = ((CountPartitionedNode)countNode)
				.getPhysicalNode(start);
		SortedScannerIterable<Count> scanner = physicalSortedMapStorageNode.scan(Range.create(start, true, end, true), null);
		Predicate<Count> predicate = new FilterCountByServer(source);
		Iterable<Count> filtered = Iterables.filter(scanner, predicate);
		return Count.getListWithGapsFilled(name, WebApp, getSource(), periodMs, filtered, startMs, endMs);

	}

	public static final int DISCARD_IF_OLDER_THAN = 300 * 1000;

	@Override
	public void saveCounts(CountMapPeriod countMap){
		if(countMap.getStartTimeMs() < (System.currentTimeMillis() - DISCARD_IF_OLDER_THAN)){
			// don't let them build up in memory for too long (datastore may hiccup)
			logger.warn("databean count archive flushing too slowly, discarding countMap older than:"
					+ DISCARD_IF_OLDER_THAN + " ms");
			return;
		}
		if(!shouldFlush(countMap)){
			// logger.warn("merging new counts into "+getName());
			aggregator.merge(countMap);
			return;
		}

		// logger.warn("flushing "+getName());
		AtomicCounter oldAggregator = aggregator;
		long periodStart = DrDateTool.getPeriodStart(countMap.getStartTimeMs(), periodMs);
		aggregator = new AtomicCounter(periodStart, periodMs);
		List<Count> toSave = new ArrayList<>();
		for(Map.Entry<String,AtomicLong> entry : DrMapTool.nullSafe(oldAggregator.getCountByKey()).entrySet()){
			if(entry.getValue() == null || entry.getValue().equals(0L)){
				continue;
			}
			toSave.add(new Count(entry.getKey(), webApp, periodMs, periodStart, source, System.currentTimeMillis(),
					entry.getValue().get()));
		}
		if(countNode != null){
			// String tableName = "Count"+CountPartitionedNode.getSuffix(flushPeriodMs);
			// logger.warn("saving "+CollectionTool.size(toSave)+" counts to "+tableName);
			countNode.putMulti(toSave, new Config().setPersistentPut(false).setIgnoreNullFields(true).setTimeout(10,
					TimeUnit.SECONDS).setNumAttempts(2));
			flushAvailableCounters(countMap.getCountByKey());
		}
		lastFlushMs = System.currentTimeMillis();
	}

	protected void flushAvailableCounters(Map<String,AtomicLong> countByKey){
		List<AvailableCounter> toSave = new LinkedList<>();
		for(Map.Entry<String,AtomicLong> entry : DrMapTool.nullSafe(countByKey).entrySet()){
			toSave.add(new AvailableCounter(webApp, periodMs, entry.getKey(), source, System.currentTimeMillis()));
		}
		availableCounterNode.putMulti(toSave, new Config().setPersistentPut(false).setIgnoreNullFields(true)
				.setTimeout(10, TimeUnit.SECONDS).setNumAttempts(6));
	}

	protected boolean shouldFlush(CountMapPeriod countMap){
		long periodEndsMs = aggregator.getStartTimeMs() + periodMs;
		boolean newPeriod = countMap.getStartTimeMs() >= periodEndsMs;
		if(newPeriod){
			// logger.warn("new period flush of "+getName());
			return true;
		}

		long flushWindowPaddingMs = 10 * 1000;// give the normal flusher a chance to trigger it
		long nextFlushMs = lastFlushMs + flushPeriodMs + flushWindowPaddingMs;
		long now = System.currentTimeMillis();
		if(periodMs > MIN_EARLY_FLUSH_PERIOD_MS && now > nextFlushMs){
			logger.warn("early flush of " + getName() + " nextFlush was "
					+ DrDateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(nextFlushMs));
			return true;
		}

		return false;
	}

	@Override
	public String getName(){
		return "databean " + periodMs;
	}

	@Override
	public Integer getNumToRetain(){
		return Integer.MAX_VALUE;// probably should remove this method from the CountArchive interface
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
