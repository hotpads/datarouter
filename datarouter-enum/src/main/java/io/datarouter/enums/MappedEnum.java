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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Store enum values by an extracted key.
 * Ensure the extracted keys are unique between values.
 * Prevent reads from accidentally returning null values.
 */
public class MappedEnum<K,E>{

	private final E sampleValue;
	private final Map<K,E> valueByKey;

	public MappedEnum(E[] values, Function<E,K> keyExtractor){
		Objects.requireNonNull(values);
		if(values.length == 0){
			throw new IllegalArgumentException("Must have 1 or more values");
		}
		sampleValue = values[0];

		Objects.requireNonNull(keyExtractor);
		valueByKey = byUniqueKey(values, keyExtractor);
	}

	public E getOrDefault(K key, E defaultValue){
		return valueByKey.getOrDefault(key, defaultValue);
	}

	public E getOrThrow(K key){
		E value = valueByKey.get(key);
		if(value == null){
			String message = String.format(
					"key=%s does not exist for enum=%s",
					key,
					sampleValue.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public Optional<E> find(K key){
		return Optional.ofNullable(valueByKey.get(key));
	}

	private static <E,K> Map<K,E> byUniqueKey(E[] values, Function<E,K> keyExtractor){
		Map<K,E> map = new HashMap<>();
		for(E value : values){
			K key = keyExtractor.apply(value);
			if(map.containsKey(key)){
				String message = String.format("key=%s already exists with value=%s", key, value);
				throw new IllegalArgumentException(message);
			}
			map.put(key, value);
		}
		return Collections.unmodifiableMap(map);
	}

}
