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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.op.scan.SortedStorageSamplingScanner.SortedStorageSample;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

public class SortedStorageSamplingScanner<PK extends PrimaryKey<PK>>
implements Scanner<SortedStorageSample<PK>>{
	private static final Logger logger = LoggerFactory.getLogger(SortedStorageSamplingScanner.class);

	private final SortedStorageReader<PK,?> node;
	private final Range<PK> range;
	private final int stride;
	private final boolean log;

	private PK startKey;
	private boolean startInclusive;
	private long total;
	private final Config strideConfig;
	private final Config scanKeysConfig;
	private PK nextStartKey;

	private SortedStorageSample<PK> current;
	private boolean finished = false;

	public SortedStorageSamplingScanner(
			SortedStorageReader<PK,?> node,
			Range<PK> range,
			int stride,
			int batchSize,
			boolean log){
		this.node = node;
		this.range = range;
		this.stride = stride;
		this.log = log;

		startKey = range.getStart();
		startInclusive = range.getStartInclusive();
		total = 0;
		strideConfig = new Config().setLimit(1).setOffset(stride - 1);
		scanKeysConfig = new Config().setOutputBatchSize(batchSize);
	}

	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		var strideRange = new Range<>(startKey, startInclusive, range.getEnd(), range.getEndInclusive());
		nextStartKey = node.scanKeys(strideRange, strideConfig).findFirst().orElse(null);
		if(nextStartKey != null){
			total += stride;
			current = new SortedStorageSample<>("stride", strideRange, stride, total);
			logCurrent();
			startKey = nextStartKey;
			startInclusive = false;
			return true;
		}

		//revert to scanKeys for the final span
		var scanKeysRange = new Range<>(startKey, startInclusive, range.getEnd(), range.getEndInclusive());
		long scanKeysCount = node.scanKeys(scanKeysRange, scanKeysConfig).count();
		finished = true;
		if(scanKeysCount == 0){
			return false;
		}
		total += scanKeysCount;
		current = new SortedStorageSample<>("scanKeys", scanKeysRange, scanKeysCount, total);
		logCurrent();
		return true;
	}

	@Override
	public SortedStorageSample<PK> current(){
		return current;
	}

	public void logCurrent(){
		if(log){
			logger.warn("{}", current);
		}
	}

	public static class SortedStorageSample<PK extends PrimaryKey<PK>>{

		public final String strategy;
		public final Range<PK> range;
		public final long sampleCount;
		public final long totalCount;

		public SortedStorageSample(String strategy, Range<PK> range, long sampleCount, long totalCount){
			this.strategy = strategy;
			this.range = range;
			this.sampleCount = sampleCount;
			this.totalCount = totalCount;
		}

		@Override
		public String toString(){
			return String.format("%s counted=%s total=%s range=%s",
					strategy,
					NumberFormatter.addCommas(sampleCount),
					NumberFormatter.addCommas(totalCount),
					range);
		}

	}

	public static class SortedStorageSamplingScannerBuilder<PK extends PrimaryKey<PK>>{

		private final SortedStorageReader<PK,?> node;
		private Range<PK> range;
		private int stride;
		private int batchSize;
		private boolean log;

		public SortedStorageSamplingScannerBuilder(SortedStorageReader<PK,?> node){
			this.node = node;
			this.range = Range.everything();
			this.stride = 100_000;
			this.batchSize = 1_000;
			this.log = false;
		}

		public SortedStorageSamplingScannerBuilder<PK> withRange(Range<PK> range){
			this.range = range;
			return this;
		}

		public SortedStorageSamplingScannerBuilder<PK> withStride(int stride){
			this.stride = stride;
			return this;
		}

		public SortedStorageSamplingScannerBuilder<PK> withBatchSize(int batchSize){
			this.batchSize = batchSize;
			return this;
		}

		public SortedStorageSamplingScannerBuilder<PK> withLog(boolean log){
			this.log = log;
			return this;
		}

		public SortedStorageSamplingScanner<PK> build(){
			return new SortedStorageSamplingScanner<>(node, range, stride, batchSize, log);
		}

	}

}
