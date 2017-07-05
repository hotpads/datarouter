package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.imp.ListBackedSortedScanner;

import org.testng.Assert;

public class BatchingScanner<T> implements Scanner<List<T>>{

	private Scanner<T> scanner;
	private int batchSize;
	private List<T> batch;

	public BatchingScanner(Scanner<T> scanner, int batchSize){
		this.scanner = scanner;
		this.batch = new ArrayList<>();
		this.batchSize = batchSize;
	}

	@Override
	public boolean advance(){
		batch = new ArrayList<>();
		while(!fullBatch()){
			if(!scanner.advance()){
				break;
			}
			batch.add(scanner.getCurrent());
		}
		return CollectionTool.notEmpty(batch);
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
			Scanner<Integer> scanner = new ListBackedSortedScanner<>(new ArrayList<Integer>());
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<>(scanner, 3);
			Assert.assertFalse(batchingScanner.advance());
		}
		@Test
		public void testPartialBatch(){
			List<Integer> ints = ListTool.createArrayList(0,1);
			Scanner<Integer> scanner = new ListBackedSortedScanner<>(ints);
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<>(scanner, 3);
			Assert.assertTrue(batchingScanner.advance());
			List<Integer> batch = batchingScanner.getCurrent();
			Assert.assertEquals(batch.size(), 2);
			Assert.assertEquals(batch.get(0).intValue(), 0);
			Assert.assertEquals(batch.get(1).intValue(), 1);
			Assert.assertFalse(batchingScanner.advance());
		}
		@Test
		public void testMultipleBatches(){
			List<Integer> ints = ListTool.createArrayList(0,1,2,3,4,5,6,7);
			Scanner<Integer> scanner = new ListBackedSortedScanner<>(ints);
			BatchingScanner<Integer> batchingScanner = new BatchingScanner<>(scanner, 3);
			List<List<Integer>> batches = new ArrayList<>();
			while(batchingScanner.advance()){
				batches.add(batchingScanner.getCurrent());
			}
			Assert.assertEquals(batches.size(), 3);
			Assert.assertEquals(batches.get(0).get(2).intValue(), 2);
			Assert.assertEquals(batches.get(1).get(1).intValue(), 4);
			Assert.assertEquals(batches.get(2).size(), 2);
		}
	}
}
