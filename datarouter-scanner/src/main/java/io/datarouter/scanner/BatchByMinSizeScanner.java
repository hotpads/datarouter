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
package io.datarouter.scanner;

import java.util.List;
import java.util.function.Function;

import io.datarouter.scanner.BatchByMinSizeScanner.ScannerMinSizeBatch;

public class BatchByMinSizeScanner<T> extends BaseLinkedScanner<T,ScannerMinSizeBatch<T>>{

	private final long minSizePerBatch;
	private final Function<T,Number> sizeExtractor;
	private List<T> batch;
	private long size;
	private boolean finished;

	public BatchByMinSizeScanner(
			Scanner<T> input,
			long minSizePerBatch,
			Function<T,Number> sizeExtractor){
		super(input);
		this.minSizePerBatch = minSizePerBatch;
		this.sizeExtractor = sizeExtractor;
		batch = new PagedList<>();
		size = 0;
		finished = false;
	}

	@Override
	public boolean advanceInternal(){
		if(finished){
			return false;
		}
		while(input.advance()){
			T inputCurrent = input.current();
			batch.add(inputCurrent);
			size += sizeExtractor.apply(inputCurrent).longValue();
			if(size >= minSizePerBatch){
				current = new ScannerMinSizeBatch<>(batch, size);
				batch = new PagedList<>();
				size = 0;
				return true;
			}
		}
		if(batch.isEmpty()){
			return false;
		}else{
			current = new ScannerMinSizeBatch<>(batch, size);
			finished = true;
			return true;
		}
	}

	public record ScannerMinSizeBatch<T>(
			List<T> items,
			long totalSize){
	}

}
