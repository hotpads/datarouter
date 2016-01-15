package com.hotpads.util.core.collections;

import java.util.Map;


public interface DefaultableMap<K,V> extends Map<K,V> {
	/**
	 * get the value mapped by key, if there is no value, return the default given
	 * @param key
	 * @param def the default value
	 * @return the value, or if it doesn't exist the default value given
	 */
	public V getDefault(K key, V def);
	
	/**
	 * get the value mapped by key as a Boolean, or def if there is no value
	 * @param key
	 * @param def
	 * @return
	 */
	public boolean getBoolean(K key, boolean def);
	/**
	 * get the value mapped by key as a Double, or def if there is no value
	 * @param key
	 * @param def
	 * @return
	 */
	public Double getDouble(K key, Double def);
	/**
	 * get the value mapped by key as a String, or def if there is no value
	 * @param key
	 * @param def
	 * @return
	 */
	public String getString(K key, String def);
	/**
	 * get the value mapped by key as a Integer, or def if there is no value
	 * @param key
	 * @param def
	 * @return
	 */
	public Integer getInteger(K key, Integer def);

	/**
	 * get the value mapped by key as a Long, or def if there is no value
	 * @param key
	 * @param def
	 * @return
	 */
	public Long getLong(K key, Long def);
}
