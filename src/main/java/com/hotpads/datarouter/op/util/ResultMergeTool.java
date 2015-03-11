package com.hotpads.datarouter.op.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;

public class ResultMergeTool {

	public static Integer sum(Integer a, Collection<Integer> c){
		boolean foundValue = false;
		int sum = 0;
		if(a != null){ foundValue = true; sum += a; }
		for(Integer i : DrCollectionTool.nullSafe(c)){
			if(i != null){ foundValue = true; sum += i; }
		}
		return foundValue==true? sum : null;
	}

	public static Long sum(Long a, Collection<Long> c){
		boolean foundValue = false;
		long sum = 0;
		if(a != null){ foundValue = true; sum += a; }
		for(Long i : DrCollectionTool.nullSafe(c)){
			if(i != null){ foundValue = true; sum += i; }
		}
		return foundValue==true? sum : null;
	}

	public static <T,C extends Collection<T>> T first(T a, C bs){
		if(a != null){ return a; }
		for(T t : DrCollectionTool.nullSafe(bs)){
			if(t != null){ return t; }
		}
		return null;
	}
	
	public static <T,C extends Collection<T>> List<T> append(C a, Collection<? extends C> bs){
		int size = DrCollectionTool.sizeNullSafe(a) + DrCollectionTool.getTotalSizeOfCollectionOfCollections(bs);
		List<T> out = DrListTool.createArrayList(size);
		out.addAll(DrCollectionTool.nullSafe(a));
		for(C b : DrCollectionTool.nullSafe(bs)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}
	
	public static <T,C extends Collection<T>> Set<T> addAll(C a, Collection<? extends C> bs){
		Set<T> out = new HashSet<>();
		out.addAll(DrCollectionTool.nullSafe(a));
		for(C b : DrCollectionTool.nullSafe(bs)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}
	
	public static <T,C extends Collection<T>> SortedSet<T> addAllSorted(C a, Collection<? extends C> bs){
		SortedSet<T> out = new TreeSet<>();
		out.addAll(DrCollectionTool.nullSafe(a));
		for(C b : DrCollectionTool.nullSafe(bs)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}
	
	public static <T extends Comparable<? super T>,C extends Collection<T>> 
	ArrayList<T> mergeIntoListAndSort(C a, Collection<? extends C> bs){
		ArrayList<T> out = DrListTool.createArrayList();
		out.addAll(DrCollectionTool.nullSafe(a));
		for(C b : DrCollectionTool.nullSafe(bs)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		Collections.sort(out);
		return out;
	}
	
	public static <T extends Comparable<T>,C extends Collection<T>> 
	List<T> appendAndSort(C a, Collection<? extends C> bs){
		List<T> appended = append(a, bs);
		Collections.sort(appended);
		return appended;
	}
	
	public static <K,V> Map<K,V> mergeMaps(Map<K,V> fromOnce, Collection<Map<K,V>> fromEach){
		Map<K,V> result = new HashMap<K,V>();
		if(DrMapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,V> m : DrCollectionTool.nullSafe(fromEach)){
			result.putAll(m);
		}
		return result;
	}
	
	public static <K> Map<K,Integer> mergeIntegerValueMaps(Map<K,Integer> fromOnce, Collection<Map<K,Integer>> fromEach){
		Map<K,Integer> result = new HashMap<K,Integer>();
		if(DrMapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,Integer> m : DrCollectionTool.nullSafe(fromEach)){
			for(Entry<K,Integer> e : m.entrySet()){
				if(result.get(e.getKey())==null){
					result.put(e.getKey(), e.getValue());
				}else{
					Integer currentSum = result.get(e.getKey());
					Integer newSum = currentSum + e.getValue();
					result.put(e.getKey(), newSum);
				}
			}
		}
		return result;
	}
}
