/*
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
package io.datarouter.storage.op.scan.stride.internal;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.util.tuple.Range;

public class InternalStrideScanner<PK extends PrimaryKey<PK>>
extends BaseScanner<InternalStrideSample<PK>>{
	private static final Logger logger = LoggerFactory.getLogger(InternalStrideScanner.class);

	private final SortedStorageReader<PK,?> node;
	private final Supplier<Boolean> shouldStop;
	private final Range<PK> range;
	private final int stride;
	private final int batchSize;
	private final boolean log;

	private boolean striding;
	private boolean interrupted;
	private boolean finished;
	private Range<PK> nextRange;

	public InternalStrideScanner(
			SortedStorageReader<PK,?> node,
			Supplier<Boolean> shouldStop,
			Range<PK> range,
			int stride,
			int batchSize,
			boolean log){
		this.node = node;
		this.shouldStop = shouldStop;
		this.range = range;
		this.stride = stride;
		this.batchSize = batchSize;
		this.log = log;

		striding = true;
		interrupted = false;
		finished = false;
		nextRange = range.clone();
	}

	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		if(shouldStop.get()){
			interrupted = true;
			finished = true;
		}
		current = null;
		if(striding){
			nextOffsettingPk(node, nextRange, stride)
					.map(offsettingSample -> offsettingSample.toStrideSample(nextRange, interrupted))
					.ifPresent(this::updateCurrent);
			striding = current != null;
		}
		if(current == null){
			nextScanningPk(node, nextRange, batchSize)
					.map(scanningSample -> scanningSample.toStrideSample(nextRange, interrupted))
					.ifPresent(this::updateCurrent);
			finished = true;
		}
		return current != null;
	}

	private void updateCurrent(InternalStrideSample<PK> sample){
		current = sample;
		if(current != null){
			nextRange = new Range<>(current.lastSeenKey, false, range.getEnd(), range.getEndInclusive());
			if(log){
				logger.warn("{}", current);
			}
		}
	}

	private static <PK extends PrimaryKey<PK>>
	Optional<OffsettingStrideSample<PK>> nextOffsettingPk(
			SortedStorageReader<PK,?> node,
			Range<PK> range,
			int stride){
		var strideConfig = new Config().setLimit(1).setOffset(stride - 1);
		return node.scanKeys(range, strideConfig)
				.findFirst()
				.map(pk -> new OffsettingStrideSample<>(pk, stride));
	}

	private static <PK extends PrimaryKey<PK>>
	Optional<ScanningStrideSample<PK>> nextScanningPk(
			SortedStorageReader<PK,?> node,
			Range<PK> range,
			int batchSize){
		var scanKeysConfig = new Config().setOutputBatchSize(batchSize);
		var state = new ScanKeysState<PK>();
		node.scanKeys(range, scanKeysConfig)
				.forEach(pk -> {
					state.lastSeenKey = pk;
					state.count += 1;
				});
		if(state.count == 0){
			return Optional.empty();
		}
		long numRpcs = state.count / batchSize;
		var sample = new ScanningStrideSample<>(state.lastSeenKey, numRpcs, state.count);
		return Optional.of(sample);
	}

	private static class ScanKeysState<PK extends PrimaryKey<PK>>{
		PK lastSeenKey;
		long count;
	}

}
