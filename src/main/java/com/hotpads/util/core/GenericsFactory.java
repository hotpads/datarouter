package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

//import com.hotpads.util.core.collections.AppendList;
import com.hotpads.util.core.collections.Pair;
//import com.hotpads.util.core.collections.Twin;

@Deprecated
public class GenericsFactory {
	public static <K,V> TreeMap<K, V> makeTreeMap() {
		return new TreeMap<K,V>();
	}
	
//	public static <K,V> DefaultableHashMap<K, V> makeDefaultableHashMap() {
//		return new DefaultableHashMap<K, V>();
//	}
	
	public static <K,V> HashMap<K,V> makeHashMap() {
	    return new HashMap<K,V>();
	}
	
	public static <K,V> LinkedHashMap<K,V> makeLinkedHashMap(){
		return new LinkedHashMap<K,V>();
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
	
	public static ArrayList<Short> makeArrayList(short[] shortArray) {
		if(shortArray == null || shortArray.length==0){ return new ArrayList<Short>(); }
		ArrayList<Short> list = new ArrayList<Short>();
		for(int i=0; i < shortArray.length; ++i){
			list.add(shortArray[i]);
		}
		return list;
	}
	
	public static <K> HashSet<K> makeHashSet(K... k) {
		HashSet<K> set = new HashSet<K>();
		if(k==null || k.length<1) return set;
		set.addAll(Arrays.asList(k));
		return set;
	}
		
	public static <K> ArrayList<K> makeArrayList(Collection<K> coll) {
		ArrayList<K> list = new ArrayList<K>(coll.size());
		list.addAll(coll);
		return list;		
	}
		
	public static <K> HashSet<K> HashSet(Collection<K> coll) {
		HashSet<K> set = new HashSet<K>(coll.size());
		set.addAll(coll);
		return set;		
	}
		
	public static <K> SortedSet<K> makeTreeSet(Collection<K> startWith) {
		return SetTool.nullSafeSortedAddAll(new TreeSet<K>(), startWith);
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
	
	
//	public static <T> AppendList<T> makeAppendList() {
//		return new AppendList<T>();
//	}

	public static <K, V> Pair<K, V> makePair(K a, V b) {
		return new Pair<K,V>(a,b);
	}
//	public static <K> Twin<K> makeTwin(K a, K b){
//		return new Twin<K>(a,b);
//	}
	
	public static <K> Stack<K> makeStack(){
		return new Stack<K>();
	}
	
	public static <K> HashSet<K> makeHashSet() {
		return new HashSet<K>();
	}
	
	public static <K> TreeSet<K> makeTreeSet(){
		return new TreeSet<K>();
	}
}
