package com.hotpads.profile.count.collection.archive.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.profile.count.collection.AtomicCounter;
import com.hotpads.profile.count.collection.CountCollectorPeriod;
import com.hotpads.profile.count.collection.archive.BaseCountArchive;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.collection.predicate.FilterCountByServer;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.collections.Range;

public class DatabeanCountArchive extends BaseCountArchive{
	private static Logger logger = LoggerFactory.getLogger(DatabeanCountArchive.class);

	// prevent the 5s counter from early flushing because of slight lag
	private static final long MIN_EARLY_FLUSH_PERIOD_MS = 20 * 1000;

	private AtomicCounter aggregator;
	private final Long flushPeriodMs;
	private Long lastFlushMs;// need to anchor this to the period... currently floating

	private final SortedMapStorage<CountKey,Count> countNode;
	private final SortedMapStorage<AvailableCounterKey,AvailableCounter> availableCounterNode;

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
	public List<AvailableCounter> getAvailableCounters(String nameLike, String nameWildcard, int uniqueNameLimit){
		return getAvailableCountersForWebApp(nameLike, nameWildcard, webApp, uniqueNameLimit);
	}

	@Override
	public List<AvailableCounter> getAvailableCountersForWebApp(String namePrefix, String nameWildcard, String webApp,
			int uniqueNameLimit){
		return filterAndLimitCounters(webApp, namePrefix, namePrefix, nameWildcard, uniqueNameLimit);
	}

	@Override
	public Collection<AvailableCounter> getAvailableCountersStartingAt(String startingAt, String namePrefix,
			String nameWildcard, String webApp, int uniqueNameLimit){
		return filterAndLimitCounters(webApp, startingAt, namePrefix, nameWildcard, uniqueNameLimit);
	}

	private List<AvailableCounter> filterAndLimitCounters(String webApp, String paramFirstCounterName,
			String paramNamePrefix, String paramNameWildcard, int uniqueNameLimit){
		String firstCounterName = DrStringTool.nullIfEmpty(paramFirstCounterName);
		String namePrefix = DrStringTool.nullSafe(paramNamePrefix);
		//apparently we do case-insensitive wildcard
		String lowerCaseNameWildcard = DrStringTool.nullSafe(paramNameWildcard).toLowerCase();
		AvailableCounterKey startKey = new AvailableCounterKey(webApp, periodMs, firstCounterName, null);
		Range<AvailableCounterKey> range = new Range<>(startKey, true);
		logger.info(range.toString());
		Iterable<AvailableCounter> counters = availableCounterNode.scan(range, null);
		Set<String> uniqueNames = new HashSet<>();
		List<AvailableCounter> results = new ArrayList<>();
		int numScanned = 0;
		for(AvailableCounter counter : counters){
			++numScanned;
			if(uniqueNames.size() > uniqueNameLimit){
				logger.info("breaking after {}", uniqueNames.size());
				break;
			}
			if(!counter.getName().startsWith(namePrefix)){
				logger.info("breaking on {}", counter);
				break;
			}
			String lowerCaseName = DrStringTool.nullSafe(counter.getName()).toLowerCase();
			if(DrStringTool.isEmpty(lowerCaseNameWildcard) || lowerCaseName.contains(lowerCaseNameWildcard)){
				results.add(counter);
				uniqueNames.add(counter.getName());
			}
		}
		logger.info("firstCounterName: {}, namePrefix: {}, nameWildcard: {}, webApp: {}, limit: {}, numScanned: {}"
				+ ", numResults: {}", firstCounterName, namePrefix, lowerCaseNameWildcard, webApp, uniqueNameLimit,
				numScanned, results.size());
		return results;
	}


	/* remember that sources are interleaved, so a range query will return all sources */
	@Override
	public List<Count> getCountsForAllSources(String name, Long startMs, Long endMs){
		CountKey start = new CountKey(name, webApp, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, webApp, periodMs, System.currentTimeMillis(), null, null);
		PhysicalSortedMapStorageNode<CountKey,Count> physicalSortedMapStorageNode = ((CountPartitionedNode)countNode)
				.getPhysicalNode(start);
		Iterable<Count> scanner = physicalSortedMapStorageNode.scan(Range.create(start, true, end, true), null);
		return Count.getListWithGapsFilled(name, webApp, getSource(), periodMs, scanner, startMs, endMs);

	}

	@Override
	public List<Count> getCountsForWebApp(String name, String webApp, Long startMs, Long endMs){
		CountKey start = new CountKey(name, webApp, periodMs, startMs, null, null);
		CountKey end = new CountKey(name, webApp, periodMs, System.currentTimeMillis(), null, Long.MAX_VALUE);
		PhysicalSortedMapStorageNode<CountKey,Count> physicalSortedMapStorageNode = ((CountPartitionedNode)countNode)
				.getPhysicalNode(start);
		Iterable<Count> scanner = physicalSortedMapStorageNode.scan(Range.create(start, true, end, true), null);
		Predicate<Count> predicate = new FilterCountByServer(source);
		Iterable<Count> filtered = Iterables.filter(scanner, predicate);
		return DrListTool.createArrayList(filtered);
	}

	@Override
	public List<Count> getCountsForWebAppWithGapsFilled(String name, String webApp, long startMs, long endMs){
		Iterable<Count> filtered = getCountsForWebApp(name, webApp, startMs, endMs);
		return Count.getListWithGapsFilled(name, webApp, getSource(), periodMs, filtered, startMs, endMs);

	}

	public static final int DISCARD_IF_OLDER_THAN = 300 * 1000;

	@Override
	public void saveCounts(CountCollectorPeriod countMap){
		if(countMap.getStartTimeMs() < System.currentTimeMillis() - DISCARD_IF_OLDER_THAN){
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
			if(entry.getValue() == null || entry.getValue().longValue() == 0){
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

	protected boolean shouldFlush(CountCollectorPeriod countMap){
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
