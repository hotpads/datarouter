/*
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
package io.datarouter.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Store enum values by an extracted and optionally transformed key.
 * Ensure the transformed keys are unique between values.
 * Prevent reads from accidentally returning null values.
 */
public class MappedEnum<E,K>{

	private final E sampleValue;
	private final Class<E> enumClass;
	private final Function<E,K> keyExtractor;
	private final Function<K,K> keyTransformer;
	private final Map<K,E> valueByKey;

	public MappedEnum(E[] values, Function<E,K> keyExtractor){
		this(values, keyExtractor, Function.identity());
	}

	public MappedEnum(E[] values, Function<E,K> keyExtractor, Function<K,K> keyTransformer){
		Objects.requireNonNull(values);
		if(values.length == 0){
			throw new IllegalArgumentException("Must have 1 or more values");
		}
		sampleValue = values[0];
		enumClass = extractEnumClass(sampleValue);
		this.keyExtractor = Objects.requireNonNull(keyExtractor);
		this.keyTransformer = Objects.requireNonNull(keyTransformer);
		valueByKey = byUniqueTransformedKey(values, keyExtractor, keyTransformer);
	}

	/*------------- validate -------------*/

	public MappedEnum<E,K> requireAllExist(@SuppressWarnings("unchecked") K... keys){
		return requireAllExist(Arrays.asList(keys));
	}

	public MappedEnum<E,K> requireAllExist(Collection<K> keys){
		keys.forEach(this::fromOrThrow);
		return this;
	}

	/*------------- encode -------------*/

	public K toKey(E enumValue){
		return keyExtractor.apply(enumValue);
	}

	/*------------- decode -------------*/

	public E fromOrNull(K key){
		K transformedKey = keyTransformer.apply(key);
		return valueByKey.get(transformedKey);
	}

	public E fromOrElse(K key, E defaultValue){
		Objects.requireNonNull(defaultValue, "Use getOrNull for null default value");
		K transformedKey = keyTransformer.apply(key);
		return valueByKey.getOrDefault(transformedKey, defaultValue);
	}

	public E fromOrThrow(K key){
		K transformedKey = keyTransformer.apply(key);
		E value = valueByKey.get(transformedKey);
		if(value == null){
			String message = String.format(
					"key=%s, transformedKey=%s does not exist for enum=%s",
					key,
					transformedKey,
					sampleValue.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public Optional<E> from(K key){
		K transformedKey = keyTransformer.apply(key);
		return Optional.ofNullable(valueByKey.get(transformedKey));
	}

	/*----------- get -------------*/

	public E getSampleValue(){
		return this.sampleValue;
	}

	public Class<E> getEnumClass(){
		return enumClass;
	}

	/*------------- private -------------*/

	@SuppressWarnings("unchecked")
	private static <E> Class<E> extractEnumClass(E sampleValue){
		return (Class<E>)sampleValue.getClass();
	}

	private static <E,K> Map<K,E> byUniqueTransformedKey(
			E[] values,
			Function<E,K> keyExtractor,
			Function<K,K> keyTransformer){
		Map<K,E> map = new HashMap<>();
		for(E value : values){
			K key = keyExtractor.apply(value);
			K transformedKey = keyTransformer.apply(key);
			if(map.containsKey(key)){
				String message = String.format("transformedKey=%s already exists with value=%s", transformedKey, value);
				throw new IllegalArgumentException(message);
			}
			map.put(transformedKey, value);
		}
		return Collections.unmodifiableMap(map);
	}

}
