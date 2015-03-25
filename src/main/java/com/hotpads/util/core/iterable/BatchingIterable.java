package com.hotpads.util.core.iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class BatchingIterable<T> implements Iterable<List<T>>{
	
	private Iterable<T> iterable;
	private int batchSize;
	
	public BatchingIterable(Iterable<T> iterable, int batchSize){
		this.iterable = iterable;
		if(batchSize < 1){ throw new IllegalArgumentException("illegal batch size:"+batchSize); }
		this.batchSize = batchSize;
	}
	
	//helper method so callers don't have to specify T
	public static <T> BatchingIterable<T> create(Iterable<T> iterable, int batchSize){
		return new BatchingIterable<T>(iterable, batchSize);
	}
	
	@Override
	public Iterator<List<T>> iterator(){
		return new BatchingIterator<T>(iterable.iterator(), batchSize);
	}
	
	
	public static class BatchingIterator<T> implements Iterator<List<T>>{
		
		private Iterator<T> iter;
		private List<T> batch;
		private int batchSize;
		
		private BatchingIterator(Iterator<T> iter, int batchSize){
			this.iter = iter;
			this.batch = new ArrayList<>();
			this.batchSize = batchSize;
		}
		
		@Override
		public boolean hasNext(){
			return iter.hasNext();
		}
		
		@Override
		public List<T> next(){
			while(true){
				if(!iter.hasNext() || batch.size() >= batchSize){
					List<T> result = batch;
					batch = new ArrayList<>();
					return result;
				}
				batch.add(iter.next());
			}
		}
		
		@Override
		public void remove(){
			throw new UnsupportedOperationException();
		}
	}
	
}