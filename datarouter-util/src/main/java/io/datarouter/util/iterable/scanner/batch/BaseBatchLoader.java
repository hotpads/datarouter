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
package io.datarouter.util.iterable.scanner.batch;

import java.util.List;

import io.datarouter.util.collection.CollectionTool;

/*
 * this loader also maintains the state of the scanner.  could separate if needed
 */
public abstract class BaseBatchLoader<T>
implements BatchLoader<T>{

	private boolean batchHasBeenLoaded;
	private List<T> batch;
	private int currentIndex;

	protected BaseBatchLoader(){
		batchHasBeenLoaded = false;
		currentIndex = -1;//must call advance() before getCurrent()
	}

	@Override
	public boolean advance(){
		++currentIndex;
		return isCurrentValid();
	}

	@Override
	public T getCurrent(){
		if(!isCurrentValid()){//prevent AIOOB exception (could remove this check)
			return null;
		}
		return batch.get(currentIndex);
	}

	private boolean isCurrentValid(){
		return batchHasBeenLoaded
				&& batch != null
				&& currentIndex >= 0
				&& currentIndex < CollectionTool.size(batch);
	}

	protected boolean isBatchSmallerThan(int size){
		return CollectionTool.size(batch) < size;
	}

	protected void updateBatch(List<T> batch){
		this.batch = batch;
		batchHasBeenLoaded = true;//thread safe by lack of other writers
	}

	public boolean isBatchHasBeenLoaded(){
		return batchHasBeenLoaded;
	}

	protected T getLast(){
		return CollectionTool.getLast(batch);
	}
}
