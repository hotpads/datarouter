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
package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.datarouter.util.iterable.cascade.CascadeIterable;
import io.datarouter.util.iterable.cascade.CascadeIterator;

public class CascadeIterableTests{

	private List<List<Integer>> twoLists;
	private AtomicInteger counter;

	@BeforeMethod
	public void beforeMethod(){
		twoLists = new ArrayList<>();
		twoLists.add(new ArrayList<>(Arrays.asList(1, 2, 3)));
		twoLists.add(new ArrayList<>(Arrays.asList(400, 500)));
		counter = new AtomicInteger(0);
	}

	@Test
	public void testCascadeIteratorOverEmpty(){
		Iterator<Integer> iter = new CascadeIterator<>(() ->
				counter.getAndIncrement() == 0 ? new ArrayList<Integer>().iterator() : null);
		Assert.assertFalse(iter.hasNext());
		Assert.assertFalse(iter.hasNext());
		Assert.assertFalse(iter.hasNext());
		Assert.assertEquals(counter.get(), 2);
		try{
			iter.next();
			Assert.fail("expecting error not caught");
		}catch(NoSuchElementException e){
			// pass
		}
	}

	@Test
	public void testRemoveFromIterator(){
		Iterator<Integer> iter = new CascadeIterator<>(() -> {
			int index = counter.getAndIncrement();
			return index < 5 ? twoLists.get(index / 3).iterator() : null;
		});
		try{
			iter.remove();
			Assert.fail("expecting exception not thrown");
		}catch(IllegalStateException e){
			// pass
		}
		while(iter.hasNext()){
			iter.next();
			iter.remove();
		}
		Assert.assertTrue(twoLists.get(0).isEmpty());
		Assert.assertTrue(twoLists.get(1).isEmpty());
	}

	@Test
	public void testCascadingIterationOverTwoIterators(){
		for(Integer value : new CascadeIterable<>(() ->
				counter.get() < 4 ? twoLists.get(counter.get() / 3).iterator() : null)){
			Assert.assertEquals(value, twoLists.get(counter.get() / 3).get(counter.get() % 3));
			counter.incrementAndGet();
		}
		Assert.assertEquals(counter.get(), 5);
	}
}
