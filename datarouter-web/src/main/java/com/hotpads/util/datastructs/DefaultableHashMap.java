package com.hotpads.util.datastructs;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class DefaultableHashMap<K,V> extends DefaultableMapAbs<K,V>{

	private HashMap<K,V> backingMap;

	public DefaultableHashMap(){
		this.backingMap = new HashMap<>();
	}

	@Override
	protected Map<K,V> delegate(){
		return this.backingMap;
	}

	public static class Tests{

		private DefaultableHashMap<String, String> map;

		@Before
		public void setup(){
			map = new DefaultableHashMap<>();
			map.put("str", "str");
			map.put("bool", "true");
			map.put("double", "1.234");
			map.put("int", "6");
		}

		@Test
		public void test(){
			assertTrue(map.getBoolean("bool", false));
			assertTrue(map.getBoolean("boola", true));
			assertTrue(map.getDouble("double", 0.1).equals(1.234));
			assertTrue(map.getInteger("int", 1).equals(6));
			assertTrue(map.getInteger("inta", 1).equals(1));
		}
	}
}
