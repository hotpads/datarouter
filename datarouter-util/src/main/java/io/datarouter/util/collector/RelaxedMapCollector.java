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
package io.datarouter.util.collector;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RelaxedMapCollector{

	public static <V,K> Collector<V,?,Map<K,V>> of(Function<V,K> keyMapper){
		return of(keyMapper, Function.identity());
	}

	/**
	 * This creates a LinkedHashMap, not HashMap <br>
	 */
	public static <T,K,U> Collector<T,?,Map<K,U>> of(
			Function<? super T,? extends K> keyMapper,
			Function<? super T,? extends U> valueMapper){
		return of(keyMapper, valueMapper, LinkedHashMap::new);
	}

	/**
	 * similar to {@link Collectors#toMap(Function, Function)} but: <br>
	 * - in case of duplicate mappings, old value is overwritten with new value (instead of throwing exception) <br>
	 * - allows null values (instead of throwing exception)
	 */
	public static <T,K,U,M extends Map<K,U>> Collector<T,M,M> of(
			Function<? super T,? extends K> keyMapper,
			Function<? super T,? extends U> valueMapper,
			Supplier<M> supplier){
		return new Collector<T,M,M>(){

			@Override
			public Supplier<M> supplier(){
				return supplier;
			}

			@Override
			public BiConsumer<M,T> accumulator(){
				return (map, element) -> {
					K key = keyMapper.apply(element);
					U value = valueMapper.apply(element);
					map.put(key, value);
				};
			}

			@Override
			public BinaryOperator<M> combiner(){
				return (map1, map2) -> {
					map1.putAll(map2);
					return map1;
				};
			}

			@Override
			public Function<M,M> finisher(){
				return Function.identity();
			}

			@Override
			public Set<Collector.Characteristics> characteristics(){
				return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
			}

		};
	}

}
