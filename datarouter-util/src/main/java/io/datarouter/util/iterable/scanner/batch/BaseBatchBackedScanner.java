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
