package com.hotpads.util.core.iterable.scanner.batch;

import java.util.List;

import com.hotpads.datarouter.util.core.DrCollectionTool;

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
				&& currentIndex < DrCollectionTool.size(batch);
	}
	
	protected boolean isBatchSmallerThan(int i){
		return DrCollectionTool.size(batch) < i;
	}
	
	protected void updateBatch(List<T> batch){
		this.batch = batch;
		batchHasBeenLoaded = true;//thread safe by lack of other writers
	}
	
	public boolean isBatchHasBeenLoaded(){
		return batchHasBeenLoaded;
	}
	
	protected T getLast(){
		return DrCollectionTool.getLast(batch);
	}
	
}
