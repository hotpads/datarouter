package com.hotpads.util.core.iterable.scanner.batch;

import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseHoldingSortedScanner;

//can store anything in currentBatch.  subclass will translate B -> T later
public abstract class BaseBatchingSortedScanner<T extends Comparable<? super T>,B extends Object> 
extends	BaseHoldingSortedScanner<T>{
	
	public static final Integer BATCH_SIZE_DEFAULT = 100;

	//inputs
//	protected int batchSize = BATCH_SIZE_DEFAULT;//not needed yet... introduce when needed
	
	//used during iteration
	protected List<B> currentBatch;
	protected int currentBatchIndex = -1;//advance() will immediately increment this
	protected boolean noMoreBatches = false;//optimization to track if the previous fetch didn't get a full batch
	

	/**************** constructors *******************************/
	
	public BaseBatchingSortedScanner(){
	}
	
//	public BaseBatchingSortedScanner(int batchSize){
//		this.batchSize = batchSize;
//	}

	
	/**************** abstract methods *******************************/
	
	protected abstract void loadNextBatch();
	protected abstract void setCurrentFromResult(B result);
	
	
	/****************** methods *************************************/

	@Override
	public boolean advance() {
		++currentBatchIndex;
		if(currentBatchIndex >= CollectionTool.size(currentBatch)){//finished this batch
			if(noMoreBatches){ return false; }
			loadNextBatch();//let the loadNextBatch method worry about hitting the end
		}
		if(CollectionTool.isEmpty(currentBatch)){ return false; }//currentBatch is now the new batch
		B currentResult = currentBatch.get(currentBatchIndex);
		setCurrentFromResult(currentResult);
		return true;
	}
	
	
	/******************* get/set ****************************************/
	
//	public BaseBatchingSortedScanner<T,B> setBatchSize(int batchSize){
//		this.batchSize = batchSize;
//		return this;
//	}

	
}
