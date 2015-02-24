package com.hotpads.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;


public class MapTool {
	
	protected static final Map<?,?> EMPTY_HASH_MAP = Collections.unmodifiableMap(createHashMap());
	protected static final Map<?,?> EMPTY_TREE_MAP = Collections.unmodifiableMap(createTreeMap());
	
	/*********************** size ******************************************************/
	
	public static <K,V> boolean isEmpty(Map<K,V> map){
		if(map==null){ return true; }
		return map.isEmpty();
	}
	
	public static <K,V> boolean notEmpty(Map<K,V> map){
		return ! isEmpty(map);
	}
	
	public static <K,V> int size(Map<K,V> map){
		return map==null?0:map.size();
	}
	
	public static boolean hasMultiple(Map<?,?> map) {
		return size(map) > 1;
	}
	
	
	/**************************** create **********************************************/

	@Deprecated
	public static <K,V> Map<K,V> create(){
		return createHashMap();
	}

	@Deprecated
	public static <K,V> HashMap<K,V> createHashMap(){
		return new HashMap<K,V>();
	}

	@Deprecated
	public static <K,V> LinkedHashMap<K,V> createLinkedHashMap(){
		return new LinkedHashMap<K,V>();
	}

	@Deprecated
	public static <K,V> ConcurrentHashMap<K,V> createConcurrentHashMap(){
		return new ConcurrentHashMap<K,V>();
	}

	@Deprecated
	public static <K,V> NavigableMap<K,V> createTreeMap(){
		return new TreeMap<K,V>();
	}

	@Deprecated
	public static <K,V> NavigableMap<K,V> createNavigableTreeMap(){
		return new TreeMap<K,V>();
	}
	
	public static <K,V> Map<K,V> create(K key, V value){
		return createHashMap(key, value);
	}
	
	public static <K,V> Map<K,V> createHashMap(K key, V value){
		Map<K,V> map = new HashMap<K,V>();
		map.put(key, value);
		return map;
	}
	
	public static <K,V> SortedMap<K,V> createTreeMap(K key, V value){
		SortedMap<K,V> map = new TreeMap<K,V>();
		map.put(key, value);
		return map;
	}
	
	public static <K,V>Map<K,V> filterAccordingToKeyOrValue(Map<K,V> aCounterByName, Predicate<K> predicateKey,
			Predicate<V> predcateValue){
		Map<K,V> toReturn = MapTool.create();
		for(K key : aCounterByName.keySet()){
			if(predicateKey.apply(key) || predcateValue.apply(aCounterByName.get(key))){
				toReturn.put(key, aCounterByName.get(key));
			}
		}
		return toReturn;
	}

	public static <K,V>Map<K,V> createSubMap(Map<K,V> aCounterByName, int offsetResults, int numberResultsToDisplay){
		Map<K,V> toReturn = MapTool.create();

		List<K> keys = ListTool.createArrayList(aCounterByName.keySet());
		offsetResults = offsetResults >= 0 && offsetResults < keys.size() ? offsetResults : 0;
		int endIndex = numberResultsToDisplay >= 0 && numberResultsToDisplay < keys.size() ? numberResultsToDisplay
				: keys.size();
		keys = keys.subList(offsetResults, endIndex);
		for(K k : keys){
			toReturn.put(k, aCounterByName.get(k));
		}

		return toReturn;
	}
	
	public static HashMap<String,String> createHashMapFromList(List<String> list){
		HashMap<String,String> toReturn = MapTool.createHashMap();
		for(String item : list){
			toReturn.put(item, item);
		}
		return toReturn;
	}
	
	/****************************** null safe ******************************************/

	public static <K,V> Map<K,V> nullSafe(Map<K,V> in){
		if(in==null){ return new HashMap<K,V>(); }
		return in;
	}

	public static <K,V> Map<K,V> nullSafeTreeMap(Map<K,V> in){
		if(in==null){ return new TreeMap<K,V>(); }
		return in;
	}

	public static <K,V> SortedMap<K,V> nullSafeTreeMap(SortedMap<K,V> in){
		if(in==null){ return new TreeMap<K,V>(); }
		return in;
	}
	
	public static <K,V> Map<K,V> nullSafeHashMap(Map<K,V> in){
		if(in==null){ return new HashMap<K,V>(); }
		return in;
	}
	
	public static <K,V> Map<K,V> nullSafeHashPut(Map<K,V> map, K key, V value){
		map = nullSafeHashMap(map);
		map.put(key, value);
		return map;
	}
	
	public static <K,V> Map<K,V> nullSafeTreePut(Map<K,V> map, K key, V value){
		map = nullSafeTreeMap(map);
		map.put(key, value);
		return map;
	}

	public static <K,V> SortedMap<K,V> nullSafeTreePutAll(SortedMap<K,V> map, Map<K,V> newItems){
		map = nullSafeTreeMap(map);
		map.putAll(MapTool.nullSafeHashMap(newItems));
		return map;
	}
	
	public static <K,V> V putIfAbsentAndGet(Map<K,V> map, K key, V value){
		if(!map.containsKey(key)){ map.put(key, value); }
		return map.get(key);
	}
	
	
	/******************************** empty **********************************/
	
	@SuppressWarnings("unchecked") 
	public static <K,V> Map<K,V> emptyHashMap(){
		//can't cast unmodifiableMap back to HashMap, only Map interface
		return (Map<K,V>)EMPTY_HASH_MAP;
	}

	@SuppressWarnings("unchecked") 
	public static <K,V> Map<K,V> emptyTreeMap(){
		return (Map<K,V>)EMPTY_TREE_MAP;
	}

	@SuppressWarnings("unchecked") 
	public static <K,V> Map<K,V> emptyNavigableMap(){
		return (Map<K,V>)EMPTY_TREE_MAP;
	}
	
	/******************************** complex maps *************************/
	
	public static <K,V,C extends Collection<V>,M extends Map<K,C>> List<V> getAllValues(M in){
		List<V> outs = ListTool.createArrayList();
		for(K k : nullSafe(in).keySet()) {
			outs.addAll(CollectionTool.nullSafe(in.get(k)));
		}
		return outs;
	}
	
	/********************************** printing ****************************/

	public static <K, V>  String toXml(Map<K, V> map, String rootName) {
		StringBuilder sb = new StringBuilder("<" + rootName + ">");
		
		for(Map.Entry<K, V> entry : map.entrySet()){
			if(Map.class.isInstance(entry.getValue())){
				sb.append(MapTool.toXml((Map<?,?>)entry.getValue(), entry.getKey().toString()));
				continue;
			}
			sb.append("<" + entry.getKey().toString() + ">");

			if(Collection.class.isInstance(entry.getValue())){
				for(Object item : ((Collection<?>)entry.getValue())){
					sb.append(item.toString());
				}
			}
			else{
				sb.append(entry.getValue().toString());
			}
			sb.append("</" + entry.getKey().toString() + ">");
		}
		
		sb.append("</" + rootName + ">");
		return sb.toString();
	}


	public static <K, V>  String toCsv(Map<K, V> map, String rootName) {
		StringBuilder sb = new StringBuilder();
		sb.append(rootName + "[");
		int numFinished = 0;
		for(Map.Entry<K, V> entry : map.entrySet()){
			if(numFinished>0){ sb.append(","); }
			sb.append(entry.getKey().toString() + "=");
			sb.append(entry.getValue().toString());
			++numFinished;
		}
		sb.append("]");
		return sb.toString();
	}

	public static <K, V> void addAll(Map<K, V> from, Map<K, V> to) {
		for(Map.Entry<K, V> entry : from.entrySet()){
			to.put(entry.getKey(), entry.getValue());
		}
	}
	
	/******************************* counting ***********************************/
	
	//convenience method
	public static <T> Long increment(Map<T,Long> map, T key){
		return increment(map, key, 1L);
	}
	//convenience method
	public static <T,U> Long increment(Map<T,Map<U,Long>> tMap, T t, U u){
		return increment(tMap, t, u, 1L);
	}
	//convenience method
	public static <T,U,V> Long increment(Map<T,Map<U,Map<V,Long>>> tMap, T t, U u, V v){
		return increment(tMap, t, u, v, 1L);
	}

	
	//1 level: Map<T,Long>
	public static <T> Long increment(Map<T,Long> map, T key, Long delta){
		if(!map.containsKey(key)){ 
			map.put(key, delta); 
			return delta;
		}
		map.put(key, map.get(key) + delta);
		return map.get(key);
	}

	//2 levels: Map<T,Map<U,Long>>
	public static <T,U> Long increment(Map<T,Map<U,Long>> tMap, T t, U u, Long delta){
		if(!tMap.containsKey(t)){ 
			tMap.put(t, new TreeMap<U,Long>()); 
		}
		Map<U,Long> uMap = tMap.get(t);
		if(!uMap.containsKey(u)){
			uMap.put(u, 0L);
		}
		uMap.put(u, uMap.get(u) + delta);
		return uMap.get(u);
	}

	//3 levels: Map<T,Map<U,Map<V,Long>>>
	public static <T,U,V> Long increment(Map<T,Map<U,Map<V,Long>>> tMap, T t, U u, V v, Long delta){
		if(!tMap.containsKey(t)){ 
			tMap.put(t, new TreeMap<U,Map<V,Long>>()); 
		}
		Map<U,Map<V,Long>> uMap = tMap.get(t);
		if(!uMap.containsKey(u)){
			uMap.put(u, new TreeMap<V,Long>());
		}
		Map<V,Long> vMap = uMap.get(u);
		if(!vMap.containsKey(v)){
			vMap.put(v, 0L);
		}
		vMap.put(v, vMap.get(v) + delta);
		return vMap.get(v);
	}
	
	
	/************************* string keyed counting ******************************/
	
	public static Long incrementStringKey(Map<String,Long> map, Object key){
		return incrementStringKey(map, key, 1L);
	}

	public static Long incrementStringKey(Map<String,Long> map, Object key, Long delta){
		String stringKey = ObjectTool.nullSafeToString(key, "null");
		if(!map.containsKey(stringKey)){ 
			map.put(stringKey, delta); 
			return delta;
		}
		map.put(stringKey, map.get(stringKey) + delta);
		return map.get(stringKey);
	}

	
	/********************* multi-ops *************************/
	
	public static <K,V> List<V> getValuesForKeys(Map<K,V> map, Iterable<K> keys){
		List<V> outs = ListTool.createArrayList();
		if(isEmpty(map)) {
			return outs;
		}
		for(K key : IterableTool.nullSafe(keys)){
			V value = map.get(key);
			if(value==null){ continue; }
			outs.add(value);
		}
		return outs;
	}
	
	
	/********************** filtering ****************************/

	public static <K,V> V getFirstValue(Map<K,V> map){
		if(isEmpty(map)){ return null; }
		for(V value : map.values()){
			return value;
		}
		return null;
	}

	public static <K,V> K getFirstKeyWhereValueEquals(Map<K,V> map, V value){
		for(Map.Entry<K,V> entry : nullSafe(map).entrySet()){
			if(ObjectTool.equals(value, entry.getValue())){ 
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Build a map from the string with the format "
	 * <code>key[keyValueSeparator]value[entrySeperator]key[keyValueSeparator]value...</code>"
	 * @param string The input {@link String}
	 * @param entrySeperator The separator between tow entries
	 * @param keyValueSeparator The separator between the key and the value
	 * @return a {@link Map} 
	 */
	public static Map<String, String> getMapFromString(String string, String entrySeperator, String keyValueSeparator) {
		Map<String, String> map = MapTool.createTreeMap();
		if (StringTool.isEmpty(string)) {return map;}
		String[] entries = string.split(entrySeperator);
		String[] keyVal;
		for (String entry : entries) {
			if (StringTool.notEmpty(entry)) {
				keyVal = entry.split(keyValueSeparator);
				map.put(keyVal[0], keyVal.length > 1 ? keyVal[1] : null);
			}
		}
		return map;
	}

	public static class MapToolTests {

		@Test
		public void testGetValuesForKeys() {
			Map<Integer,Integer> fullMap = MapTool.create();
			for(int i=0; i < 5; ++i) { fullMap.put(i, i % 2 == 0 ? i : null); }
			Assert.assertEquals(Collections.emptyList(), getValuesForKeys(null, ListTool.create(1,2)));
			Assert.assertEquals(Collections.emptyList(), getValuesForKeys(new HashMap<Integer,Void>(), ListTool.create(1,2)));
			Assert.assertEquals(Collections.emptyList(), getValuesForKeys(fullMap, new ArrayList<Integer>()));
			Assert.assertEquals(ListTool.create(0,4), getValuesForKeys(fullMap, ListTool.create(0,1,4)));
		}
		
		@Test
		public void getMapFromString() {
			String string = "key1: val1;key2: val2";
			Map<String, String> res = MapTool.getMapFromString(string, ";", ": ");
			Assert.assertEquals(2, res.size());
			Assert.assertEquals("val2", res.get("key2"));
			Assert.assertEquals(string, Joiner.on(";").withKeyValueSeparator(": ").join(res));
		}

	}

}
