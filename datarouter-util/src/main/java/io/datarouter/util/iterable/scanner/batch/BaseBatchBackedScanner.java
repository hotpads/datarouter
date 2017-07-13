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
import io.datarouter.util.iterable.scanner.sorted.BaseHoldingScanner;

//can store anything in currentBatch.  subclass will translate B -> T later
public abstract class BaseBatchBackedScanner<T,B> extends BaseHoldingScanner<T>{

	//used during iteration
	protected List<B> currentBatch;
	protected int currentBatchIndex = -1;//advance() will immediately increment this
	protected boolean noMoreBatches = false;//optimization to track if the previous fetch didn't get a full batch

	/**************** abstract methods *******************************/

	protected abstract void loadNextBatch();
	protected abstract void setCurrentFromResult(B result);

	/****************** methods *************************************/

	@Override
	public boolean advance(){
		++currentBatchIndex;
		if(currentBatchIndex >= CollectionTool.size(currentBatch)){//finished this batch
			if(noMoreBatches){
				return false;
			}
			loadNextBatch();//let the loadNextBatch method worry about hitting the end
		}
		if(CollectionTool.isEmpty(currentBatch)){//currentBatch is now the new batch
			return false;
		}
		B currentResult = currentBatch.get(currentBatchIndex);
		setCurrentFromResult(currentResult);
		return true;
	}
}
