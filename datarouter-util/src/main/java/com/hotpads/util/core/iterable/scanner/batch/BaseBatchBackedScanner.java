package com.hotpads.util.core.iterable.scanner.batch;

import java.util.List;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseHoldingScanner;

//can store anything in currentBatch.  subclass will translate B -> T later
public abstract class BaseBatchBackedScanner<T,B> 
extends	BaseHoldingScanner<T>{
	
	public static final Integer BATCH_SIZE_DEFAULT = 100;

	//used during iteration
	protected List<B> currentBatch;
	protected int currentBatchIndex = -1;//advance() will immediately increment this
	protected boolean noMoreBatches = false;//optimization to track if the previous fetch didn't get a full batch

	/**************** abstract methods *******************************/
	
	protected abstract void loadNextBatch();
	protected abstract void setCurrentFromResult(B result);
	
	/****************** methods *************************************/

	@Override
	public boolean advance() {
		++currentBatchIndex;
		if(currentBatchIndex >= DrCollectionTool.size(currentBatch)){//finished this batch
			if(noMoreBatches){
				return false;
			}
			loadNextBatch();//let the loadNextBatch method worry about hitting the end
		}
		if(DrCollectionTool.isEmpty(currentBatch)){//currentBatch is now the new batch
			return false;
		}
		B currentResult = currentBatch.get(currentBatchIndex);
		setCurrentFromResult(currentResult);
		return true;
	}
}
