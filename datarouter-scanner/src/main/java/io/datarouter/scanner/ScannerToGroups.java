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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScannerToGroups<T,K,V,C extends Collection<V>,M extends Map<K,C>> implements Function<Scanner<T>,M>{

	private final Function<T,K> keyFunction;
	private final Function<T,V> valueFunction;
	private final Supplier<M> mapSupplier;
	private final Supplier<C> collectionSupplier;

	private ScannerToGroups(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier,
			Supplier<C> collectionSupplier){
		this.keyFunction = keyFunction;
		this.valueFunction = valueFunction;
		this.mapSupplier = mapSupplier;
		this.collectionSupplier = collectionSupplier;
	}

	@Override
	public M apply(Scanner<T> scanner){
		M map = mapSupplier.get();
		scanner.forEach(item -> {
			K key = keyFunction.apply(item);
			C collection = map.computeIfAbsent(key, $ -> collectionSupplier.get());
			V value = valueFunction.apply(item);
			collection.add(value);
		});
		return map;
	}

	/*--------------- Factories ----------------*/

	public static <T,K>
	Function<Scanner<T>,Map<K,List<T>>> of(
			Function<T,K> keyFunction){
		return new ScannerToGroups<>(keyFunction, Function.identity(), HashMap::new, ArrayList::new);
	}

	public static <T,K,V>
	Function<Scanner<T>,Map<K,List<V>>> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction){
		return new ScannerToGroups<>(keyFunction, valueFunction, HashMap::new, ArrayList::new);
	}

	public static <T,K,V,M extends Map<K,List<V>>>
	Function<Scanner<T>,M> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier){
		return new ScannerToGroups<>(keyFunction, valueFunction, mapSupplier, ArrayList::new);
	}

	public static <T,K,V,C extends Collection<V>,M extends Map<K,C>>
	Function<Scanner<T>,M> of(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier,
			Supplier<C> collectionSupplier){
		return new ScannerToGroups<>(keyFunction, valueFunction, mapSupplier, collectionSupplier);
	}

}
