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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DatarouterEnumTool{

	/*--------------- comparator that compares the persistent values --------*/

	public static class IntegerEnumComparator<T extends IntegerEnum<T>> implements Comparator<T>{
		@Override
		public int compare(T valueA, T valueB){
			if(bothNull(valueA, valueB)){
				return 0;
			}
			if(isOneNullButNotTheOther(valueA, valueB)){
				return valueA == null ? -1 : 1;
			}
			return nullFirstCompareTo(valueA.getPersistentInteger(), valueB.getPersistentInteger());
		}
	}

	public static <T extends IntegerEnum<T>> int compareIntegerEnums(T valueA, T valueB){
		if(bothNull(valueA, valueB)){
			return 0;
		}
		if(isOneNullButNotTheOther(valueA, valueB)){
			return valueA == null ? -1 : 1;
		}
		return nullFirstCompareTo(valueA.getPersistentInteger(), valueB.getPersistentInteger());
	}

	public static <T extends StringEnum<T>> int compareStringEnums(T valueA, T valueB){
		if(bothNull(valueA, valueB)){
			return 0;
		}
		if(isOneNullButNotTheOther(valueA, valueB)){
			return valueA == null ? -1 : 1;
		}
		return nullFirstCompareTo(valueA.getPersistentString(), valueB.getPersistentString());
	}

	/*------------------------- methods -------------------------------------*/

	public static <T extends IntegerEnum<T>> T getEnumFromInteger(T[] values, Integer value, T defaultEnum){
		return findEnumFromInteger(values, value).orElse(defaultEnum);
	}

	public static <T extends IntegerEnum<T>> Optional<T> findEnumFromInteger(T[] values, Integer value){
		if(value == null){
			return Optional.empty();
		}
		return Arrays.stream(values)
				.filter(type -> type.getPersistentInteger().equals(value))
				.findFirst();
	}

	public static <T extends PersistentString> T getEnumFromString(T[] values, String value, T defaultEnum,
			boolean caseSensitive){
		return findEnumFromString(values, value, caseSensitive).orElse(defaultEnum);
	}

	public static <T extends PersistentString> T getEnumFromString(T[] values, String value, T defaultEnum){
		return findEnumFromString(values, value, true).orElse(defaultEnum);
	}

	public static <T extends PersistentString> Optional<T> findEnumFromString(T[] values, String value){
		return findEnumFromString(values, value, true);
	}

	public static <T extends PersistentString> Optional<T> findEnumFromString(T[] values, String value,
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

	/*------------------------- multiple values -----------------------------*/

	public static <E extends StringEnum<E>> List<String> getPersistentStrings(Collection<E> enums){
		if(enums == null){
			return Java9.listOf();
		}
		return enums.stream()
				.map(E::getPersistentString)
				.collect(Collectors.toList());
	}

	public static <E extends StringEnum<E>> List<E> fromPersistentStrings(E enumInstance,
			Collection<String> persistentStrings){
		if(persistentStrings == null){
			return Java9.listOf();
		}
		return persistentStrings.stream()
				.map(enumInstance::fromPersistentString)
				.collect(Collectors.toList());
	}

	public static <E extends StringEnum<E>> Validated<List<E>> uniqueListFromCsvNames(E[] values, String csvNames,
			boolean defaultAll){
		Set<E> result = new LinkedHashSet<>();
		Validated<List<E>> validated = new Validated<>();

		if(notEmpty(csvNames)){
			String[] types = csvNames.split("[,\\s]+");
			for(String name : types){
				if(isEmpty(name)){
					continue;
				}
				E type = getEnumFromString(values, name, null, false);
				if(type == null){
					validated.addError(name);
				}else{
					result.add(type);
				}
			}
		}
		if(result.isEmpty()){
			if(defaultAll){
				for(E e : values){
					result.add(e);
				}
			}else{
				validated.addError("No value found");
			}
		}
		List<E> listResult = new ArrayList<>();
		listResult.addAll(result);
		validated.set(listResult);
		return validated;
	}

	// StringTool.notEmpty
	private static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	// StringTool.isEmpty
	private static boolean isEmpty(String input){
		return input == null || input.isEmpty();
	}

	// ObjectTool.bothNull
	private static boolean bothNull(Object first, Object second){
		return first == null && second == null;
	}

	// ObjectTool.isOneNullButNotTheOther
	private static boolean isOneNullButNotTheOther(Object first, Object second){
		return first == null ^ second == null;
	}

	// ComparableTool.nullFirstCompareTo
	private static <T extends Comparable<? super T>> int nullFirstCompareTo(T object1, T object2){
		if(object1 == null && object2 == null){
			return 0;
		}else if(object1 == null){
			return -1;
		}else if(object2 == null){
			return 1;
		}else{
			return object1.compareTo(object2);
		}
	}

}
