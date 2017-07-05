package io.datarouter.util.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BatchingIterable<T> implements Iterable<List<T>>{

	private Iterable<T> iterable;
	private int batchSize;

	// Nov 10 2016: this is causing a seg fault
	// Jun 20 2017: no seg fault in the unit test
	public BatchingIterable(Stream<T> stream, int batchSize){
		this(stream::iterator, batchSize);
	}

	public BatchingIterable(Iterable<T> iterable, int batchSize){
		this.iterable = iterable;
		if(batchSize < 1){
			throw new IllegalArgumentException("illegal batch size:" + batchSize);
		}
		this.batchSize = batchSize;
	}

	@Override
	public Iterator<List<T>> iterator(){
		return new BatchingIterator<>(iterable.iterator(), batchSize);
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

	}

	public static class Tests{

		private final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

		@Test
		public void testIteareblrBatching(){
			verifyBatches(new BatchingIterable<>(list, 2).iterator());
		}

		@Test
		public void testStreamBatching(){
			verifyBatches(new BatchingIterable<>(list.stream(), 2).iterator());
		}

		private void verifyBatches(Iterator<List<Integer>> batches){
			Assert.assertEquals(Arrays.asList(1, 2), batches.next());
			Assert.assertEquals(Arrays.asList(3, 4), batches.next());
			Assert.assertEquals(Arrays.asList(5), batches.next());
			Assert.assertFalse(batches.hasNext());
		}

	}

}