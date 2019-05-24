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
package io.datarouter.util.enums;

import java.util.Optional;

public interface IntegerEnum<E> extends Comparable<E>{

	Integer getPersistentInteger();
	E fromPersistentInteger(Integer value);

	default Optional<E> fromPersistentIntegerOptional(Integer value){
		return Optional.ofNullable(fromPersistentInteger(value));
	}

	static <E extends IntegerEnum<E>> E fromPersistentIntegerSafe(E sampleValue, Integer persistentInteger){
		if(persistentInteger == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentInteger(persistentInteger);
		if(enumValue == null || !persistentInteger.equals(enumValue.getPersistentInteger())){
			throw new RuntimeException(sampleValue.getClass().getSimpleName() + ".fromPersistentInteger returned "
					+ (enumValue == null ? "null" : enumValue.getPersistentInteger()) + " instead of "
					+ persistentInteger);
		}
		return enumValue;
	}

}
