package io.datarouter.util.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.testng.Assert;

public class SetTool{

	public static <T> Set<T> wrap(T element){
		Set<T> set = new HashSet<>();
		if(element != null){
			set.add(element);
		}
		return set;
	}

	private static <T> SortedSet<T> nullSafeTreeSet(SortedSet<T> in){
		if(in == null){
			return new TreeSet<>();
		}
		return in;
	}

	public static <T> SortedSet<T> nullSafeSortedAddAll(SortedSet<T> set, Collection<T> newItems){
		set = nullSafeTreeSet(set);
		set.addAll(CollectionTool.nullSafe(newItems));
		return set;
	}

	public static class Tests{
		@Test
		public void testNullSafeAddAllWithEmptySet(){
			SortedSet<String> set = new TreeSet<>();
			set.add("b");
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertEquals(new String[]{"a", "b", "c"}, set.toArray());
		}

		@Test
		public void testNullSafeAddAllWithNullSet(){
			SortedSet<String> set = null;
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertEquals(new String[]{"a", "c"}, set.toArray());
		}
	}

}
