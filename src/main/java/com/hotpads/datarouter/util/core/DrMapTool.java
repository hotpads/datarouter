package com.hotpads.datarouter.util.core;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;


public class DrMapTool {
	
	/*********************** size ******************************************************/
	
	public static <K,V> boolean isEmpty(Map<K,V> map){
		if(map==null){
			return true;
		}
		return map.isEmpty();
	}
	
	public static <K,V> boolean notEmpty(Map<K,V> map){
		return ! isEmpty(map);
	}
	
	public static <K,V> int size(Map<K,V> map){
		return map==null?0:map.size();
	}
	
	/****************************** null safe ******************************************/

	public static <K,V> Map<K,V> nullSafe(Map<K,V> in){
		if(in==null){
			return new HashMap<K,V>();
		}
		return in;
	}

	/******************************* counting ***********************************/
	
	//convenience method
	public static <T> Long increment(Map<T,Long> map, T key){
		return increment(map, key, 1L);
	}

	
	//1 level: Map<T,Long>
	private static <T> Long increment(Map<T,Long> map, T key, Long delta){
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
	
	/********************** filtering ****************************/

	public static <K,V> K getFirstKeyWhereValueEquals(Map<K,V> map, V value){
		for(Map.Entry<K,V> entry : nullSafe(map).entrySet()){
			if(DrObjectTool.equals(value, entry.getValue())){ 
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
		Map<String, String> map = new TreeMap<>();
		if (DrStringTool.isEmpty(string)) {
			return map;
		}
		String[] entries = string.split(entrySeperator);
		String[] keyVal;
		for (String entry : entries) {
			if (DrStringTool.notEmpty(entry)) {
				keyVal = entry.split(keyValueSeparator);
				map.put(keyVal[0], keyVal.length > 1 ? keyVal[1] : null);
			}
		}
		return map;
	}

	public static class MapToolTests {
		
		@Test
		public void getMapFromString() {
			String string = "key1: val1;key2: val2";
			Map<String, String> res = DrMapTool.getMapFromString(string, ";", ": ");
			Assert.assertEquals(2, res.size());
			Assert.assertEquals("val2", res.get("key2"));
			Assert.assertEquals(string, Joiner.on(";").withKeyValueSeparator(": ").join(res));
		}

	}

}
