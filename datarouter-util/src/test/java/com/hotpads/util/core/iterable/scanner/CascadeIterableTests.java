package com.hotpads.util.core.iterable.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotpads.util.core.iterable.CascadeIterable;
import com.hotpads.util.core.iterable.CascadeIterator;

public class CascadeIterableTests{

	private List<List<Integer>> twoLists;
	private AtomicInteger counter;

	@BeforeMethod
	public void setupData(){
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
