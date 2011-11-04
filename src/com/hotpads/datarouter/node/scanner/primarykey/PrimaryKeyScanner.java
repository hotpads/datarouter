package com.hotpads.datarouter.node.scanner.primarykey;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;

/*
 * Iterator that maintains state between calls to get the next batch of Databeans in the range
 */
//TODO should share a base class with Scanner?
public class PrimaryKeyScanner<PK extends PrimaryKey<PK>> 
implements PeekableIterable<PK>, PeekableIterator<PK>{
	
	SortedStorageReader<PK,?> node;
	
	boolean startInclusive; 
	PK end;
	boolean endInclusive;
	Config config;
	
	int rowsPerBatch;
	PK lastKey;
	Iterator<PK> currentBatchIterator;
	int numBatchesLoaded = 0;
	boolean foundEndOfData;
	
	PK peeked;
	
	public PrimaryKeyScanner(SortedStorageReader<PK,?> node, 
			PK start, boolean startInclusive, PK end, boolean endInclusive, 
			Config config, int defaultRowsPerBatch){
		this.node = node;
		this.lastKey = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
		this.config = Config.nullSafe(config);
		if(this.config.getIterateBatchSize()==null){ 
			this.config.setIterateBatchSize(defaultRowsPerBatch); 
		}
		foundEndOfData = false;
		loadNextBatch();
	}
		
	@Override
	public PeekableIterator<PK> iterator(){
		return this;
	}

	@Override
	public PK peek(){
		if(peeked!=null){
			return peeked;
		}
		if(hasNext()){
			peeked = next();
		}
		return peeked;
	}
	
	@Override
	public boolean hasNext() {
		if(peeked!=null){ return true; }
		if(!currentBatchIterator.hasNext()){
			loadNextBatch();
		}
		return currentBatchIterator.hasNext();
	}
	
	@Override
	public PK next() {
		if(peeked!=null){
			PK next = peeked;
			peeked = null;
			return next;
		}
		if(hasNext()){
			return currentBatchIterator.next();
		}
		return null;
	}

	@Override
	public void remove() {
		throw new DataAccessException("cannot modify a scanner");
	}

	protected void loadNextBatch(){
		if(foundEndOfData){ return; }
		config.setLimit(config.getIterateBatchSize());
		List<PK> currentBatch = node.getKeysInRange(lastKey, startInclusive, end, endInclusive, config);
		currentBatchIterator = currentBatch.iterator();
		++numBatchesLoaded;
		if(CollectionTool.size(currentBatch) < config.getLimit()){
			foundEndOfData = true;
			return; 
		}
		lastKey = CollectionTool.getLast(currentBatch);
		startInclusive = false;//always false after first batch
	}
}