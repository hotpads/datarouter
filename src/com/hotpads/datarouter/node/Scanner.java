package com.hotpads.datarouter.node;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.SortedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

/*
 * Iterator that maintains state between calls to get the next batch of Databeans in the range
 */
public class Scanner<PK extends PrimaryKey<PK>,D extends Databean<PK>> implements Iterator<D>{
	SortedStorageReadOps<PK,D> node;
	
	boolean startInclusive; 
	PK end;
	boolean endInclusive;
	Config config;
	
	int rowsPerBatch;
	PK lastKey;
	Iterator<D> currentBatchIterator;
	int numBatchesLoaded = 0;
	
	public Scanner(SortedStorageReadOps<PK,D> node, 
			PK start, boolean startInclusive, PK end, boolean endInclusive, 
			Config config, int defaultRowsPerBatch){
		this.node = node;
		this.lastKey = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
		if(config.getLimit()==null){ config.setLimit(defaultRowsPerBatch); }
		loadNextBatch();
	}
	
	@Override
	public boolean hasNext() {
		if(!currentBatchIterator.hasNext()){
			loadNextBatch();
		}
		return currentBatchIterator.hasNext();
	}
	
	@Override
	public D next() {
		if(this.hasNext()){
			return currentBatchIterator.next();
		}
		return null;
	}

	@Override
	public void remove() {
		throw new DataAccessException("cannot modify a scanner");
	}

	protected void loadNextBatch(){
		List<D> currentBatch = node.getRange(lastKey, startInclusive, end, endInclusive, config);
		currentBatchIterator = currentBatch.iterator();
		++numBatchesLoaded;
		if(CollectionTool.isEmpty(currentBatch)){ return; }
		lastKey = CollectionTool.getLast(currentBatch).getKey();
		startInclusive = false;//always false after first batch
	}
}