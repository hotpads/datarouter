package com.hotpads.datarouter.util.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;



public class DrSetTool {
	
	public static <T> Set<T> wrap(T element){
		Set<T> set = new HashSet<T>();
		if(element!=null){
			set.add(element);
		}
		return set;
	}

	private static <T> SortedSet<T> nullSafeTreeSet(SortedSet<T> in){
		if(in==null){
			return new TreeSet<T>();
		}
		return in;
	}

	public static <T> SortedSet<T> nullSafeSortedAddAll(SortedSet<T> set, Collection<T> newItems){
		set = nullSafeTreeSet(set);
		set.addAll(DrCollectionTool.nullSafe(newItems));
		return set;
	}
	
	public static <T> boolean containsSameKeys(Set<T> as, Set<T> bs){
		if(DrCollectionTool.differentSize(as, bs)){
			return false;
		}
		if(DrCollectionTool.isEmpty(as)){
			return true;
		}
		for(T a : as) {
			if( ! bs.contains(a)) {
				return false;
			}
		}
		return true;
	}
	
	

	public static class Tests {
		@Test
		public void testNullSafeAddAllWithEmptySet() {
			SortedSet<String> set = new TreeSet<>();
			set.add("b");
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertArrayEquals(set.toArray(), new String[]{"a","b","c"});
		}
		@Test
		public void testNullSafeAddAllWithNullSet() {
			SortedSet<String> set = null;
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertArrayEquals(set.toArray(), new String[]{"a","c"});
		}
	}
}
