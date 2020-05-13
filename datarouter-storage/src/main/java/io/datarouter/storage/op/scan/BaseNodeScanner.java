/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.op.scan;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.storage.config.Config;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;

public abstract class BaseNodeScanner<
		PK extends PrimaryKey<PK>,
		T extends Comparable<? super T>>//T should be either PK or D
extends BaseScanner<List<T>>{
	private static final Logger logger = LoggerFactory.getLogger(BaseNodeScanner.class);

	private static final int DEFAULT_RANGE_BATCH_SIZE = 10;
	private static final int DEFAULT_OUTPUT_BATCH_SIZE = 100;

	private final NavigableSet<Range<PK>> ranges;
	private final Config config;
	private final int rangeBatchSize;
	private long resultCount;
	private SortedSet<Range<PK>> currentRanges;
	private Config batchConfig;
	private boolean foundLastBatch;//optimization to track if the previous fetch didn't get a full batch
	private PK lastRowOfPreviousBatch;

	public BaseNodeScanner(Collection<Range<PK>> ranges, Config config, boolean caseInsensitive){
		warnIfCaseInsensitive(ranges, caseInsensitive);
		this.config = config;
		this.rangeBatchSize = this.config.optInputBatchSize().orElse(DEFAULT_RANGE_BATCH_SIZE);
		this.ranges = ranges.stream()
				.filter(Range::notEmpty)
				.collect(Collectors.toCollection(TreeSet::new));
		this.currentRanges = new TreeSet<>();
		fillCurrentRanges();
		this.resultCount = 0;
		this.batchConfig = this.config.getDeepCopy();
		this.foundLastBatch = false;
	}

	protected abstract PK getPrimaryKey(T fieldSet);
	protected abstract List<T> loadRanges(Collection<Range<PK>> ranges, Config config);

	@Override
	public boolean advance(){
		if(foundLastBatch){
			return false;
		}
		if(currentRanges.isEmpty()){
			return false;
		}
		if(current != null){
			Range<PK> previousRange = null;
			SortedSet<Range<PK>> remainingRanges = new TreeSet<>(currentRanges);
			for(Range<PK> range : currentRanges){
				if(previousRange != null){
					if(range.getStart() == null || range.getStart().compareTo(lastRowOfPreviousBatch) > 0){
						break;
					}
					remainingRanges.remove(previousRange);
					if(!ranges.isEmpty()){
						remainingRanges.add(ranges.pollFirst());
					}
				}
				previousRange = range;
			}
			currentRanges = remainingRanges;
			if(currentRanges.isEmpty()){
				return false;
			}
			Range<PK> firstRange = currentRanges.first().clone();
			firstRange.setStart(lastRowOfPreviousBatch);
			firstRange.setStartInclusive(false);
			currentRanges.remove(currentRanges.first());
			if(!firstRange.isEmpty()){
				currentRanges.add(firstRange);
			}else if(!ranges.isEmpty()){
				currentRanges.add(ranges.pollFirst());
			}else if(currentRanges.isEmpty()){
				return false;
			}
		}
		updateBatchConfigLimit();
		current = loadRanges(currentRanges, batchConfig);
		while(current.isEmpty() && !ranges.isEmpty()){
			currentRanges.clear();
			fillCurrentRanges();
			current = loadRanges(currentRanges, batchConfig);
		}
		batchConfig.setOffset(0);
		resultCount += current.size();
		if(ranges.size() == 0 && current.size() < batchConfig.getLimit()
				|| config.getLimit() != null && resultCount >= config.getLimit()){
			foundLastBatch = true;//tell the advance() method not to call this method again
		}
		lastRowOfPreviousBatch = ListTool.findLast(current)
				.map(this::getPrimaryKey)
				.map(FieldSetTool::clone)
				.orElse(null);
		return !current.isEmpty();
	}

	private void warnIfCaseInsensitive(Collection<Range<PK>> ranges, boolean caseInsensitive){
		if(ranges.size() > 1 && caseInsensitive){
			logger.warn("scan multi on case insensitive table " + ranges.stream()
					.filter(Range::hasStart)
					.map(Range::getStart)
					.map(PK::getClass)
					.findAny());
		}
	}

	private void fillCurrentRanges(){
		for(int i = 0; i < rangeBatchSize && !ranges.isEmpty(); i++){
			currentRanges.add(ranges.pollFirst());
		}
	}

	private void updateBatchConfigLimit(){
		int batchConfigLimit = config.optOutputBatchSize().orElse(DEFAULT_OUTPUT_BATCH_SIZE);
		if(config.getLimit() != null && config.getLimit() - resultCount < batchConfigLimit){
			batchConfigLimit = (int) (config.getLimit() - resultCount);
		}
		batchConfig.setLimit(batchConfigLimit);
	}

}
