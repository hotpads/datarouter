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
package io.datarouter.batchsizeoptimizer.tool;

import java.util.Iterator;
import java.util.List;

import io.datarouter.batchsizeoptimizer.BatchSizeOptimizer;

public class DynamicBatchingIterable<T> implements Iterable<List<T>>{

	private Iterable<T> backingIterable;
	private BatchSizeOptimizer optimizer;
	private String opName;

	public DynamicBatchingIterable(Iterable<T> backingIterable, BatchSizeOptimizer optimizer, String opName){
		this.backingIterable = backingIterable;
		this.optimizer = optimizer;
		this.opName = opName;
	}

	@Override
	public Iterator<List<T>> iterator(){
		return new DynamicBatchingIterator<>(backingIterable.iterator(), optimizer, opName);
	}

}
