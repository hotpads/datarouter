package com.hotpads.util.core.iterable.scanner;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.iterable.scanner.imp.ListBackedSortedScanner;

public class BatchingScanner<T> implements Scanner<List<T>>{

	private Scanner<T> scanner;
	private int batchSize;
	private List<T> batch;
	
	public BatchingScanner(Scanner<T> scanner, int batchSize){
		this.scanner = scanner;
		this.batch = DrListTool.createArrayList();
		this.batchSize = batchSize;
	}
	
	@Override
	public boolean advance(){
		batch = DrListTool.createArrayList();
		while( ! fullBatch()){
			if(!scanner.advance()){
				break;
			}
			batch.add(scanner.getCurrent());
		}
		return DrCollectionTool.notEmpty(batch);
	}
	
	@Override
	public List<T> getCurrent(){
		return batch;
	}
	
	private boolean fullBatch(){
		return batch.size() >= batchSize;
	}
	
	
	/**************** tests *************************/
	
	public static class BatchingScannerTests{
		@Test
		public void testEmptyInputScanner(){
			Scanner<Integer> scanner = new ListBackedSortedScanner<Integer>(new ArrayList<Integer>());
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<Integer>(scanner, 3);
			Assert.assertFalse(batchingScanner.advance());
		}
		@Test
		public void testPartialBatch(){
			List<Integer> ints = DrListTool.createArrayList(0,1);
			Scanner<Integer> scanner = new ListBackedSortedScanner<Integer>(ints);
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<Integer>(scanner, 3);
			Assert.assertTrue(batchingScanner.advance());
			List<Integer> batch = batchingScanner.getCurrent();
			Assert.assertEquals(2, batch.size());
			Assert.assertEquals(0, batch.get(0).intValue());
			Assert.assertEquals(1, batch.get(1).intValue());
			Assert.assertFalse(batchingScanner.advance());
		}
		@Test
		public void testMultipleBatches(){
			List<Integer> ints = DrListTool.createArrayList(0,1,2,3,4,5,6,7);
			Scanner<Integer> scanner = new ListBackedSortedScanner<Integer>(ints);
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<Integer>(scanner, 3);
			List<List<Integer>> batches = DrListTool.create();
			while(batchingScanner.advance()){
				batches.add(batchingScanner.getCurrent());
			}
			Assert.assertEquals(3, batches.size());
			Assert.assertEquals(2, batches.get(0).get(2).intValue());
			Assert.assertEquals(4, batches.get(1).get(1).intValue());
			Assert.assertEquals(2, batches.get(2).size());
		}
	}
}
