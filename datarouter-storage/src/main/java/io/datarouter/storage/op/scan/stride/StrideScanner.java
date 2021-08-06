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
package io.datarouter.storage.op.scan.stride;

import java.util.Iterator;
import java.util.function.Supplier;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.op.scan.stride.internal.InternalStrideSample;
import io.datarouter.storage.op.scan.stride.internal.InternalStrideScanner;
import io.datarouter.util.tuple.Range;

public class StrideScanner<PK extends PrimaryKey<PK>>
extends BaseScanner<StrideSample<PK>>{

	private final Iterator<InternalStrideSample<PK>> internalSampleIterator; // iterator for hasNext
	private long totalCount;

	public StrideScanner(Scanner<InternalStrideSample<PK>> internalSampleScanner){
		this.internalSampleIterator = internalSampleScanner.iterator();
	}

	@Override
	public boolean advance(){
		if(!internalSampleIterator.hasNext()){
			return false;
		}
		InternalStrideSample<PK> sample = internalSampleIterator.next();
		totalCount += sample.sampleCount;
		boolean isLast = !internalSampleIterator.hasNext();
		current = sample.toStrideSample(totalCount, isLast);
		return true;
	}

	public static class StrideScannerBuilder<PK extends PrimaryKey<PK>>{

		private final SortedStorageReader<PK,?> node;
		private Supplier<Boolean> shouldStop;
		private Range<PK> range;
		private int stride;
		private int batchSize;
		private boolean log;

		public StrideScannerBuilder(SortedStorageReader<PK,?> node){
			this.node = node;
			this.shouldStop = () -> false;
			this.range = Range.everything();
			this.stride = 10_000;
			this.batchSize = 1_000;
			this.log = false;
		}

		public StrideScannerBuilder<PK> withShouldStop(Supplier<Boolean> shouldStop){
			this.shouldStop = shouldStop;
			return this;
		}

		public StrideScannerBuilder<PK> withRange(Range<PK> range){
			this.range = range;
			return this;
		}

		public StrideScannerBuilder<PK> withStride(int stride){
			this.stride = stride;
			return this;
		}

		public StrideScannerBuilder<PK> withBatchSize(int batchSize){
			this.batchSize = batchSize;
			return this;
		}

		public StrideScannerBuilder<PK> withLog(boolean log){
			this.log = log;
			return this;
		}

		public StrideScanner<PK> build(){
			var internalScanner = new InternalStrideScanner<>(node, shouldStop, range, stride, batchSize, log);
			return new StrideScanner<>(internalScanner);
		}

	}

}
