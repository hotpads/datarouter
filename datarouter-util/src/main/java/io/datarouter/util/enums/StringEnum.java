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

public interface StringEnum<E> extends Comparable<E>, PersistentString{

	E fromPersistentString(String string);

	default Optional<E> fromPersistentStringOptional(String string){
		return Optional.ofNullable(fromPersistentString(string));
	}

	static <E extends StringEnum<E>> E fromPersistentStringSafe(E sampleValue, String persistentString){
		if(persistentString == null){
			return null;
		}
		E enumValue = sampleValue.fromPersistentString(persistentString);
		if(enumValue == null || !persistentString.equals(enumValue.getPersistentString())){
			throw new RuntimeException(sampleValue.getClass().getSimpleName() + ".fromPersistentString returned "
					+ (enumValue == null ? "null" : enumValue.getPersistentString()) + " instead of "
					+ persistentString);
		}
		return enumValue;
	}

}
