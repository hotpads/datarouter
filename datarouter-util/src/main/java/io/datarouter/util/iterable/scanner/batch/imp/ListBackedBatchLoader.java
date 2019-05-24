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
package io.datarouter.util.iterable.scanner.batch.imp;

import java.util.List;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.batch.BaseBatchLoader;
import io.datarouter.util.iterable.scanner.batch.BatchLoader;

public class ListBackedBatchLoader<T> extends BaseBatchLoader<T>{

	private List<T> list;
	private int firstIndex;
	private int batchSize;

	public ListBackedBatchLoader(List<T> list, int firstIndex, int batchSize){
		this.list = list;
		this.firstIndex = firstIndex;
		this.batchSize = batchSize;
	}

	@Override
	public boolean isLastBatch(){
		 return firstIndex + batchSize >= CollectionTool.size(list);
	}

	@Override
	public BatchLoader<T> getNextLoader(){
		int nextBatchStartIndex = firstIndex + batchSize;
		return new ListBackedBatchLoader<>(list, nextBatchStartIndex, batchSize);
	}

	@Override
	public BatchLoader<T> call(){
		int endIndexExclusive = firstIndex + batchSize;
		List<T> newBatch = ListTool.copyOfRange(list, firstIndex, endIndexExclusive);
		updateBatch(newBatch);
		return this;
	}

}
