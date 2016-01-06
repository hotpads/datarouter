package com.hotpads.util.datastructs;

import com.google.common.collect.ForwardingMap;
import com.hotpads.util.core.collections.DefaultableMap;

public abstract class DefaultableMapAbs<K,V> extends ForwardingMap<K, V> implements DefaultableMap<K, V> {
		
	@Override
	public V getDefault(K key, V def) {
		V val = get(key);
		if (val == null){
			return def;
		}
		return val;
	}

	@Override
	public boolean getBoolean(K key, boolean def) {
		V val = get(key);
		if(val == null){
			return def;
		}
		return Boolean.parseBoolean(val.toString());
	}

	@Override
	public Double getDouble(K key, Double def) {
		V val = get(key);
		if(val == null){
			return def;
		}
		try{
			return Double.valueOf(val.toString());
		}catch(NumberFormatException e){
			return def;
		}
	}

	@Override
	public Integer getInteger(K key, Integer def) {
		V val = get(key);
		if(val == null){
			return def;
		}
		try{
			return Integer.valueOf(val.toString());
		}catch(NumberFormatException e){
			return def;
		}
	}

	@Override
	public String getString(K key, String def) {
		V val = get(key);
		if (val == null){
			return def;
		}
		return val.toString();
	}
	
	@Override
	public Long getLong(K key, Long def) {
		V val = get(key);
		if(val == null){
			return def;
		}
		try{
			return Long.valueOf(val.toString());
		}catch(NumberFormatException e){
			return def;
		}
	}

}
