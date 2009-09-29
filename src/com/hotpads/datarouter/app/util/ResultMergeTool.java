package com.hotpads.datarouter.app.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class ResultMergeTool {

	public static Integer sum(Integer a, Collection<Integer> c){
		boolean foundValue = false;
		int sum = 0;
		if(a != null){ foundValue = true; sum += a; }
		for(Integer i : CollectionTool.nullSafe(c)){
			if(i != null){ foundValue = true; sum += i; }
		}
		return foundValue==true? sum : null;
	}

	public static <T,C extends Collection<T>> T first(T a, C bs){
		if(a != null){ return a; }
		for(T t : CollectionTool.nullSafe(bs)){
			if(t != null){ return t; }
		}
		return null;
	}
	
	public static <T,C extends Collection<T>> List<T> append(C a, Collection<? extends C> bs){
		int size = CollectionTool.sizeNullSafe(a) + CollectionTool.getTotalSizeOfCollectionOfCollections(bs);
		List<T> out = ListTool.createArrayList(size);
		out.addAll(CollectionTool.nullSafe(a));
		for(C b : CollectionTool.nullSafe(bs)){
			out.addAll(CollectionTool.nullSafe(b));
		}
		return out;
	}
	
	public static <T extends Comparable<T>,C extends Collection<T>> List<T> appendAndSort(C a, Collection<? extends C> bs){
		List<T> appended = append(a, bs);
		Collections.sort(appended);
		return appended;
	}
	
	public static <K,V> Map<K,V> mergeMaps(Map<K,V> fromOnce, Collection<Map<K,V>> fromEach){
		Map<K,V> result = new HashMap<K,V>();
		if(MapTool.notEmpty(fromOnce)){
			result.putAll(fromOnce);
		}
		for(Map<K,V> m : CollectionTool.nullSafe(fromEach)){
			result.putAll(m);
		}
		return result;
	}
}
