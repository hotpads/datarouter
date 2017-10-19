/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.tuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultableMap<K,V> implements Map<K,V>{

	private final Map<K,V> delegate;

	public DefaultableMap(Map<K,V> delegate){
		this.delegate = delegate;
	}

	public boolean getBoolean(K key, boolean def){
		V val = get(key);
		if(val == null){
			return def;
		}
		return Boolean.parseBoolean(val.toString());
	}

	public Double getDouble(K key, Double def){
		return getNumber(key, def, Double::valueOf);
	}

	public Integer getInteger(K key, Integer def){
		return getNumber(key, def, Integer::valueOf);
	}

	public String getString(K key, String def){
		V val = get(key);
		if(val == null){
			return def;
		}
		return val.toString();
	}

	public Long getLong(K key, Long def){
		return getNumber(key, def, Long::valueOf);
	}

	private <T extends Number> T getNumber(K key, T def, Function<String, T> parser){
		V val = get(key);
		if(val == null){
			return def;
		}
		try{
			return parser.apply(val.toString());
		}catch(NumberFormatException e){
			return def;
		}
	}

	// ---- delegate methods

	@Override
	public int size(){
		return delegate.size();
	}

	@Override
	public boolean isEmpty(){
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key){
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value){
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key){
		return delegate.get(key);
	}

	@Override
	public V put(K key, V value){
		return delegate.put(key, value);
	}

	@Override
	public V remove(Object key){
		return delegate.remove(key);
	}

	@Override
	public void putAll(Map<? extends K,? extends V> map){
		delegate.putAll(map);
	}

	@Override
	public void clear(){
		delegate.clear();
	}

	@Override
	public Set<K> keySet(){
		return delegate.keySet();
	}

	@Override
	public Collection<V> values(){
		return delegate.values();
	}

	@Override
	public Set<Entry<K,V>> entrySet(){
		return delegate.entrySet();
	}

	@Override
	public boolean equals(Object other){
		return delegate.equals(other);
	}

	@Override
	public int hashCode(){
		return delegate.hashCode();
	}

	@Override
	public String toString(){
		return delegate.toString();
	}

	public static class DefaultableMapTests{

		private DefaultableMap<String, String> map;

		@BeforeMethod
		public void setup(){
			map = new DefaultableMap<>(new HashMap<>());
			map.put("str", "str");
			map.put("bool", "true");
			map.put("double", "1.234");
			map.put("int", "6");
		}

		@Test
		public void test(){
			Assert.assertTrue(map.getBoolean("bool", false));
			Assert.assertTrue(map.getBoolean("boola", true));
			Assert.assertTrue(map.getDouble("double", 0.1).equals(1.234));
			Assert.assertTrue(map.getInteger("int", 1).equals(6));
			Assert.assertTrue(map.getInteger("inta", 1).equals(1));
		}
	}

}
