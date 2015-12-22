package com.hotpads.util.datastructs;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import com.hotpads.util.core.collections.DefaultableMap;

public class DefaultableHashMap<K,V> extends DefaultableMapAbs<K, V> implements DefaultableMap<K, V> {

	protected HashMap<K,V> backingMap = Maps.newHashMap();
	
	public DefaultableHashMap() {
		// TODO Auto-generated constructor stub
	}
	
	public DefaultableHashMap(Map<K, V> copyMap){
		backingMap = new HashMap<K, V>(copyMap);
	}
	
	@Override
	protected Map<K, V> delegate() {
		return this.backingMap;
	}
	
	public static class Tests {
		DefaultableHashMap<String, String> map;
		@Before public void setup() {
			map = new DefaultableHashMap<String,String>();
			map.put("str", "str");
			map.put("bool", "true");
			map.put("double", "1.234");
			map.put("int", "6");
		}
		
		@Test public void test() {
			assertTrue(map.getBoolean("bool", false));
			assertTrue(map.getBoolean("boola", true));
			assertTrue(map.getDouble("double", 0.1).equals(1.234));
			assertTrue(map.getInteger("int", 1).equals(6));
			assertTrue(map.getInteger("inta", 1).equals(1));
		}
	}
}
