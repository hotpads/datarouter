package com.hotpads.datarouter.app.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

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
}
