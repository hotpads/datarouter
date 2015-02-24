package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class IterableTool {

	public static <T> Iterable<T> nullSafe(Iterable<T> in){
		if(in==null){ return new ArrayList<T>(); }
		return in;
	}

	public static <T> T next(Iterator<T> i){
		if(i==null){ return null; }
		return i.hasNext()?i.next():null;
	}
	
	public static <T> T first(Iterable<T> i){
		if(i==null){ return null; }
		return next(i.iterator());
	}
	
	public static <T> Long count(Iterable<T> i){
		if(i==null){ return 0L; }
		if(i instanceof Collection){
			return (long)((Collection<T>)i).size();
		}
		long count = 0;
		for(T t : i){
			++count;
		}
		return count;
	}

	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins){
		return createArrayListFromIterable(ins, Integer.MAX_VALUE);
	}
	
	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins, int limit){
		ArrayList<T> outs = ListTool.createArrayList();
		int numAdded = 0;
		for(T in : nullSafe(ins)){
			outs.add(in);
			++numAdded;
			if(numAdded >= limit){ break; }
		}
		return outs;
	}
	
	public static <E>List<E> asList(Iterable<E> iterable){
		if(iterable instanceof List){ return (List<E>)iterable; }
		ArrayList<E> list = new ArrayList<E>();
		if(iterable != null){
			for(E e : iterable){
				list.add(e);
			}
		}
		return list;
	}
}
