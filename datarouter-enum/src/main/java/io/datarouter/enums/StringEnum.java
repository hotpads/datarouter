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
import java.util.Optional;

/**
 * @deprecated  Use StringMappedEnum, StringEncodedField, StringMappedEnumFieldCodec and StringMappedEnumSerializer
 */
@Deprecated
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
			String message = String.format(
					"%s.fromPersistentString returned %s instead of %s",
					sampleValue.getClass().getSimpleName(),
					enumValue == null ? "null" : enumValue.getPersistentString(),
					persistentString);
			throw new RuntimeException(message);
		}
		return enumValue;
	}

	static <T extends PersistentString> T getEnumFromString(
			T[] values,
			String value,
			T defaultEnum){
		return internalFindEnumFromString(values, value, true).orElse(defaultEnum);
	}

	static <T extends PersistentString> T getEnumFromStringCaseInsensitive(
			T[] values,
			String value,
			T defaultEnum){
		return internalFindEnumFromString(values, value, false).orElse(defaultEnum);
	}

	static <T extends PersistentString> Optional<T> findEnumFromString(
			T[] values,
			String value){
		return internalFindEnumFromString(values, value, true);
	}

	/*-------------- internal ------------*/

	static <T extends PersistentString> Optional<T> internalFindEnumFromString(
			T[] values,
			String value,
			boolean caseSensitive){
		if(value == null){
			return Optional.empty();
		}
		return Arrays.stream(values)
				.filter(enumEntry -> enumEntry.getPersistentString() != null)
				.filter(enumEntry -> {
					String persistentString = enumEntry.getPersistentString();
					if(caseSensitive){
						return persistentString.equals(value);
					}
					return persistentString.equalsIgnoreCase(value);
				})
				.findFirst();
	}

}
