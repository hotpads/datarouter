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
import com.hotpads.datarouter.util.core.DrMapTool;

public class ResultMergeTool{

	public static Integer sum(Integer intA, Collection<Integer> ints){
		boolean foundValue = false;
		int sum = 0;
		if(intA != null){
			foundValue = true;
			sum += intA;
		}
		for(Integer i : DrCollectionTool.nullSafe(ints)){
			if(i != null){
				foundValue = true;
				sum += i;
			}
		}
		return foundValue ? sum : null;
	}

	public static Long sum(Long varA, Collection<Long> varC){
		boolean foundValue = false;
		long sum = 0;
		if(varA != null){
			foundValue = true;
			sum += varA;
		}
		for(Long i : DrCollectionTool.nullSafe(varC)){
			if(i != null){
				foundValue = true;
				sum += i;
			}
		}
		return foundValue ? sum : null;
	}

	//TODO rename?
	public static <T,C extends Collection<T>> T first(T objA, C otherObjects){
		if(objA != null){
			return objA;
		}
		for(T t : DrCollectionTool.nullSafe(otherObjects)){
			if(t != null){
				return t;
			}
		}
		return null;
	}

	public static <T,C extends Collection<T>> List<T> append(C objA, Collection<? extends C> otherObjects){
		int size = DrCollectionTool.sizeNullSafe(objA) + DrCollectionTool.getTotalSizeOfCollectionOfCollections(
				otherObjects);
		List<T> out = new ArrayList<>(size);
		out.addAll(DrCollectionTool.nullSafe(objA));
		for(C b : DrCollectionTool.nullSafe(otherObjects)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T,C extends Collection<T>> Set<T> addAll(C src, Collection<? extends C> dest){
		Set<T> out = new HashSet<>();
		out.addAll(DrCollectionTool.nullSafe(src));
		for(C b : DrCollectionTool.nullSafe(dest)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T,C extends Collection<T>> SortedSet<T> addAllSorted(C objA, Collection<? extends C> otherObjects){
		SortedSet<T> out = new TreeSet<>();
		out.addAll(DrCollectionTool.nullSafe(objA));
		for(C b : DrCollectionTool.nullSafe(otherObjects)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		return out;
	}

	public static <T extends Comparable<? super T>,C extends Collection<T>>
	ArrayList<T> mergeIntoListAndSort(C objA, Collection<? extends C> bs){
		ArrayList<T> out = new ArrayList<>();
		out.addAll(DrCollectionTool.nullSafe(objA));
		for(C b : DrCollectionTool.nullSafe(bs)){
			out.addAll(DrCollectionTool.nullSafe(b));
		}
		Collections.sort(out);
		return out;
	}

	public static <T extends Comparable<T>,C extends Collection<T>>
	List<T> appendAndSort(C objA, Collection<? extends C> otherObjects){
		List<T> appended = append(objA, otherObjects);
		Collections.sort(appended);
		return appended;
	}

	public static <K,V> Map<K,V> mergeMaps(Map<K,V> fromOnce, Collection<Map<K,V>> fromEach){
		Map<K,V> result = new HashMap<>();
		if(DrMapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,V> m : DrCollectionTool.nullSafe(fromEach)){
			result.putAll(m);
		}
		return result;
	}

	public static <K> Map<K,Integer> mergeIntegerValueMaps(Map<K,Integer> fromOnce,
			Collection<Map<K,Integer>> fromEach){
		Map<K,Integer> result = new HashMap<>();
		if(DrMapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,Integer> m : DrCollectionTool.nullSafe(fromEach)){
			for(Entry<K,Integer> e : m.entrySet()){
				if(result.get(e.getKey()) == null){
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
