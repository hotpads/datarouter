package com.hotpads.util.core.collections;

import java.util.Map;

public interface DefaultableMap<K,V> extends Map<K,V>{

	boolean getBoolean(K key, boolean def);
	Double getDouble(K key, Double def);
	String getString(K key, String def);
	Integer getInteger(K key, Integer def);
	Long getLong(K key, Long def);
}
