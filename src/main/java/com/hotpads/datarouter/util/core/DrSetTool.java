package com.hotpads.datarouter.util.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;



public class DrSetTool {

	@Deprecated
	public static <T> HashSet<T> create(){
		return createHashSet();
	}

	@Deprecated
	public static <T> TreeSet<T> createTreeSet(){
		return new TreeSet<T>();
	}

	@Deprecated
	public static <T> HashSet<T> createHashSet(){
		return new HashSet<T>();
	}

	public static <T> Set<T> create(Collection<T> in){
		return createHashSet(in);
	}
	
	public static <T> SortedSet<T> createTreeSet(Collection<T> in){
		SortedSet<T> out = new TreeSet<T>();
		if(DrCollectionTool.notEmpty(in)){
			out.addAll(in);
		}
		return out;
	}
	
	public static <T> Set<T> createHashSet(Collection<T> in){
		Set<T> out = new HashSet<T>();
		if(DrCollectionTool.notEmpty(in)){
			out.addAll(in);
		}
		return out;
	}
	
	public static <T> HashSet<T> create(T... in){
		return createHashSet(in);
	}
	
	public static <T> HashSet<T> createHashSet(T... in){
		HashSet<T> out = new HashSet<T>(DrArrayTool.nullSafeLength(in));
		if(DrArrayTool.isEmpty(in)){
			return out;
		}
		for(int i=0; i < in.length; ++i){
			out.add(in[i]);
		}
		return out;
	}
	
	public static <T> TreeSet<T> createTreeSet(T... in){
		TreeSet<T> out = new TreeSet<T>();
		if(DrArrayTool.isEmpty(in)){
			return out;
		}
		for(int i=0; i < in.length; ++i){
			out.add(in[i]);
		}
		return out;
	}
	
	public static <T> Set<T> wrap(T t){
		Set<T> set = new HashSet<T>();
		if(t!=null){
			set.add(t);
		}
		return set;
	}

	public static <T> SortedSet<T> nullSafeTreeSet(SortedSet<T> in){
		if(in==null){ return new TreeSet<T>(); }
		return in;
	}
	
	public static <T> Set<T> nullSafeHashSet(Set<T> in){
		if(in==null){ return new HashSet<T>(); }
		return in;
	}

	public static <T> SortedSet<T> nullSafeSortedAddAll(SortedSet<T> set, Collection<T> newItems){
		set = nullSafeTreeSet(set);
		set.addAll(DrCollectionTool.nullSafe(newItems));
		return set;
	}
	
	public static <T> boolean containsSameKeys(Set<T> as, Set<T> bs){
		if(DrCollectionTool.differentSize(as, bs)){ return false; }
		if(DrCollectionTool.isEmpty(as)){ return true; }
		for(T a : as) {
			if( ! bs.contains(a)) { return false; }
		}
		return true;
	}
	
	

	public static class Tests {
		@Test
		public void testNullSafeAddAllWithEmptySet() {
			SortedSet<String> set = DrSetTool.createTreeSet();
			set.add("b");
			Set<String> toAdd = DrSetTool.create();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertArrayEquals(set.toArray(), new String[]{"a","b","c"});
		}
		@Test
		public void testNullSafeAddAllWithNullSet() {
			SortedSet<String> set = null;
			Set<String> toAdd = DrSetTool.create();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertArrayEquals(set.toArray(), new String[]{"a","c"});
		}
	}
}
