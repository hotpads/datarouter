package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.BaseHoldingScanner;


public class DrIterableTool {

	public static <T> Iterable<T> nullSafe(Iterable<T> in){
		if(in==null){
			return new ArrayList<>();
		}
		return in;
	}

	public static <T> T next(Iterator<T> iterator){
		if(iterator==null){
			return null;
		}
		return iterator.hasNext()?iterator.next():null;
	}

	public static <T> T first(Iterable<T> iterable){
		if(iterable==null){
			return null;
		}
		return next(iterable.iterator());
	}

	public static <T> Long count(Iterable<T> iterable){
		if(iterable==null){
			return 0L;
		}
		if(iterable instanceof Collection){
			return (long)((Collection<T>)iterable).size();
		}
		long count = 0;
		Iterator<T> iterator = iterable.iterator();
		while(iterator.hasNext()){
			++count;
			iterator.next();
		}
		return count;
	}

	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins){
		return createArrayListFromIterable(ins, Integer.MAX_VALUE);
	}

	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins, int limit){
		ArrayList<T> outs = new ArrayList<>();
		int numAdded = 0;
		for(T in : nullSafe(ins)){
			outs.add(in);
			++numAdded;
			if(numAdded >= limit){
				break;
			}
		}
		return outs;
	}

	public static <T> Iterable<T> skip(Iterable<T> ins, Long nullableSkip){
		long skip = Optional.ofNullable(nullableSkip).orElse(0L).longValue();
		return () -> {
			Iterator<T> iterator = ins.iterator();
			for(int i = 0; i < skip && iterator.hasNext(); i++){
				iterator.next();
			}
			return iterator;
		};
	}

	public static <E> List<E> asList(Iterable<E> iterable){
		if(iterable instanceof List){
			return (List<E>)iterable;
		}
		ArrayList<E> list = new ArrayList<>();
		if(iterable != null){
			for(E e : iterable){
				list.add(e);
			}
		}
		return list;
	}

	public static <T> Iterable<T> dedupeSortedIterator(final Iterator<T> iterator){
		return new SingleUseScannerIterable<>(new BaseHoldingScanner<T>(){
			@Override
			public boolean advance(){
				T candidate = current;
				while(iterator.hasNext() && DrObjectTool.equals(candidate, current)){
					candidate = iterator.next();
				}
				if(DrObjectTool.equals(candidate, current)){
					return false;
				}
				current = candidate;
				return true;
			}
		});
	}

	public static class DrIterableToolTests{

		@Test
		public void testDedupeSortedIterator(){
			List<Integer> sortedIntsWithDupes = Arrays.asList(0, 0, 1, 2, 3, 3, 4, 5, 9, 20, 20);
			Iterator<Integer> sortedIntsDeduped = new TreeSet<>(sortedIntsWithDupes).iterator();
			for(Integer integer : dedupeSortedIterator(sortedIntsWithDupes.iterator())){
				Assert.assertEquals(integer, sortedIntsDeduped.next());
			}
		}

		@Test
		public void testSkip(){
			List<Integer> original = IntStream.range(0, 10).boxed().collect(Collectors.toList());
			Assert.assertEquals(skip(original, null), original);
			Assert.assertEquals(skip(original, 0L), original);
			Assert.assertEquals(skip(original, 3L), original.subList(3, 10));
			Assert.assertEquals(skip(original, 15L), new ArrayList<>());
		}

	}
}
