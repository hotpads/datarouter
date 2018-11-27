/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

		private final Iterator<T> iter;
		private final int batchSize;

		private BatchingIterator(Iterator<T> iterator, int batchSize){
			this.iter = iterator;
			this.batchSize = batchSize;
		}

		@Override
		public boolean hasNext(){
			return iter.hasNext();
		}

		@Override
		public List<T> next(){
			List<T> batch = new ArrayList<>(batchSize);
			while(iter.hasNext() && batch.size() < batchSize){
				batch.add(iter.next());
			}
			return batch;
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
			Assert.assertEquals(batches.next(), Arrays.asList(1, 2));
			Assert.assertEquals(batches.next(), Arrays.asList(3, 4));
			Assert.assertEquals(batches.next(), Arrays.asList(5));
			Assert.assertFalse(batches.hasNext());
		}

	}

}