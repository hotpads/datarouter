package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
//import com.hotpads.util.core.collections.AppendList;
//import com.hotpads.util.core.collections.Twin;

@Deprecated
public class DrGenericsFactory {
	
	public static <K,V> HashMap<K,V> makeHashMap() {
	    return new HashMap<K,V>();
	}

	public static <K> ArrayList<K> makeArrayList() {
		return new ArrayList<K>();
	}
	
	public static <K> ArrayList<K> makeArrayListWithCapacity(int i) {
		return new ArrayList<K>(i);
	}
		
	public static <K> ArrayList<K> makeArrayList(K... k) {
		ArrayList<K> list = new ArrayList<K>();
		if(k==null || k.length<1) return list;
		list.addAll(Arrays.asList(k));
		return list;
	}
		
	public static <K> ArrayList<K> makeArrayList(Collection<K> coll) {
		ArrayList<K> list = new ArrayList<K>(coll.size());
		list.addAll(coll);
		return list;		
	}
		
	public static <K> LinkedList<K> makeLinkedList() {
		return new LinkedList<K>();
	}
	
	public static <K> LinkedList<K> makeLinkedList(K... k) {
		LinkedList<K> list = new LinkedList<K>();
		if(k==null || k.length<1) return list;
		list.addAll(Arrays.asList(k));
		return list;
	}
	
	public static <K> HashSet<K> makeHashSet() {
		return new HashSet<K>();
	}
	
	public static <K> TreeSet<K> makeTreeSet(){
		return new TreeSet<K>();
	}
}
