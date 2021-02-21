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
package io.datarouter.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScannerToMap<T,K,V,M extends Map<K,V>> implements Function<Scanner<T>,M>{

	public enum Replace{
		ALWAYS, // put
		NULL_VALUES, // putIfAbsent
		NULL_KEYS, // put if !containsKey, requiring additional Map lookup
		NEVER; // throw if containsKey, requiring additional Map lookup
	}

	private interface PutFunction<K,V,M>{
	    void apply(K key, V value, M map);
	}

	private final Function<T,K> keyFunction;
	private final Function<T,V> valueFunction;
	private final PutFunction<K,V,M> putFunction;
	private final Supplier<M> mapSupplier;

	private ScannerToMap(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Replace replacePolicy,
			BinaryOperator<V> mergeFunction,
			Supplier<M> mapSupplier){
		this.keyFunction = keyFunction;
		this.valueFunction = valueFunction;
		if(replacePolicy == Replace.ALWAYS){
			this.putFunction = this::replaceAlways;
		}else if(replacePolicy == Replace.NULL_VALUES){
			this.putFunction = this::replaceNullValues;
		}else if(replacePolicy == Replace.NULL_KEYS){
			this.putFunction = this::replaceNullKeys;
		}else if(replacePolicy == Replace.NEVER){
			this.putFunction = this::replaceNever;
		}else{
			Objects.requireNonNull(mergeFunction);
			this.putFunction = (key, value, map) -> map.merge(key, value, mergeFunction);
		}
		this.mapSupplier = mapSupplier;
	}

	@Override
	public M apply(Scanner<T> scanner){
		M map = mapSupplier.get();
		scanner.forEach(item -> putFunction.apply(keyFunction.apply(item), valueFunction.apply(item), map));
		return map;
	}

	/*------------ for stack trace visibility -----------*/

	private void replaceAlways(K key, V value, M map){
		map.put(key, value);
	}

	private void replaceNullValues(K key, V value, M map){
		map.putIfAbsent(key, value);
	}

	private void replaceNullKeys(K key, V value, M map){
		if(!map.containsKey(key)){
			map.put(key, value);
		}
	}

	private void replaceNever(K key, V value, M map){
		if(map.containsKey(key)){
			throw new IllegalStateException(String.format("key %s already exists", key));
		}
		map.put(key, value);
	}

	/*--------------- Factories ----------------*/

	public static <T>
	Function<Scanner<T>,Map<T,T>> of(){
		return new ScannerToMap<>(Function.identity(), Function.identity(), Replace.ALWAYS, null, HashMap::new);
	}

	public static <T,K>
	Function<Scanner<T>,Map<K,T>> of(
			Function<T,K> keyFunction){
		return new ScannerToMap<>(keyFunction, Function.identity(), Replace.ALWAYS, null, HashMap::new);
	}

	public static <T,K,V>
	Function<Scanner<T>,Map<K,V>> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction){
		return new ScannerToMap<>(keyFunction, valueFunction, Replace.ALWAYS, null, HashMap::new);
	}

	public static <T,K,V>
	Function<Scanner<T>,Map<K,V>> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Replace replacePolicy){
		return new ScannerToMap<>(keyFunction, valueFunction, replacePolicy, null, HashMap::new);
	}

	public static <T,K,V>
	Function<Scanner<T>,Map<K,V>> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			BinaryOperator<V> mergeFunction){
		return new ScannerToMap<>(keyFunction, valueFunction, null, mergeFunction, HashMap::new);
	}

	/*---------------- Factories with mapSupplier -----------------*/

	public static <T,K,M extends Map<K,T>>
	Function<Scanner<T>,M> ofSupplied(
			Function<T,K> keyFunction,
			Supplier<M> mapSupplier){
		return new ScannerToMap<>(keyFunction, Function.identity(), Replace.ALWAYS, null, mapSupplier);
	}

	public static <T,K,V,M extends Map<K,V>>
	Function<Scanner<T>,M> ofSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier){
		return new ScannerToMap<>(keyFunction, valueFunction, Replace.ALWAYS, null, mapSupplier);
	}

	public static <T,K,V,M extends Map<K,V>>
	Function<Scanner<T>,M> ofSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Replace replacePolicy,
			Supplier<M> mapSupplier){
		return new ScannerToMap<>(keyFunction, valueFunction, replacePolicy, null, mapSupplier);
	}

	public static <T,K,V,M extends Map<K,V>>
	Function<Scanner<T>,M> ofSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			BinaryOperator<V> mergeFunction,
			Supplier<M> mapSupplier){
		return new ScannerToMap<>(keyFunction, valueFunction, null, mergeFunction, mapSupplier);
	}

}
