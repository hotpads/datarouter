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
package io.datarouter.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.util.lang.ObjectTool;

/**
 * Assertion methods similar to JUnit or TestNG Assert or Guava Preconditions
 */
public class Require{

	public static void isNull(Object argument){
		isNull(argument, null);
	}

	public static void isNull(Object argument, String message){
		if(argument != null){
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> T notNull(T argument){
		return notNull(argument, null);
	}

	public static <T> T notNull(T argument, String message){
		if(argument == null){
			throw new IllegalArgumentException(message);
		}
		return argument;
	}

	public static void noNulls(Object... objects){
		noNulls(Optional.empty(), objects);
	}

	public static void noNulls(Optional<String> message, Object... objects){
		String messageStr = message.orElse(null);
		Arrays.stream(objects)
				.forEach(object -> notNull(object, messageStr));
	}

	public static <T> void equals(T first, T second){
		equals(first, second, first + " does not equal " + second);
	}

	public static <T> void equals(T first, T second, String message){
		if(ObjectTool.notEquals(first, second)){
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> void notEquals(T first, T second){
		if(Objects.equals(first, second)){
			throw new IllegalArgumentException(first + " equals " + second);
		}
	}

	public static void isTrue(boolean argument){
		isTrue(argument, (String)null);
	}

	public static void isTrue(boolean argument, String message){
		if(!argument){
			throw new IllegalArgumentException(message);
		}
	}

	public static void isTrue(boolean argument, Supplier<String> message){
		if(!argument){
			throw new IllegalArgumentException(message.get());
		}
	}

	public static void isFalse(boolean argument){
		isFalse(argument, null);
	}

	public static void isFalse(boolean argument, String message){
		if(argument){
			throw new IllegalArgumentException(message);
		}
	}

	public static <T extends Comparable<T>> T greaterThan(T item, T minimum){
		if(item.compareTo(minimum) <= 0){
			throw new IllegalArgumentException(item + " must be greater than " + minimum);
		}
		return item;
	}

	public static <T extends Comparable<T>> T lessThan(T item, T maximum){
		return lessThan(item, maximum, "");
	}

	public static <T extends Comparable<T>> T lessThan(T item, T maximum, String extraMessage){
		if(item.compareTo(maximum) >= 0){
			throw new IllegalArgumentException(item + " must be less than " + maximum + ", " + extraMessage);
		}
		return item;
	}

	public static <T> void contains(Collection<T> items, T item){
		contains(items, item, null);
	}

	public static <T> void contains(Collection<T> items, T item, String message){
		if(!items.contains(item)){
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> void notContains(Collection<T> items, T item){
		notContains(items, item, null);
	}

	public static <T> void notContains(Collection<T> items, T item, String message){
		if(items.contains(item)){
			throw new IllegalArgumentException(message);
		}
	}

	public static String notBlank(String string){
		return notBlank(string, null);
	}

	public static String notBlank(String string, String message){
		if(string == null || Java11.isBlank(string)){
			throw new IllegalArgumentException(message);
		}
		return string;
	}

	public static <T,C extends Collection<T>> C notEmpty(C items){
		return notEmpty(items, null);
	}

	public static <T,C extends Collection<T>> C notEmpty(C items, String message){
		if(items == null || items.isEmpty()){
			throw new IllegalArgumentException(message);
		}
		return items;
	}

	public static <T> Optional<T> isPresent(Optional<T> optional){
		return isPresent(optional, null);
	}

	public static <T> Optional<T> isPresent(Optional<T> optional, String message){
		if(optional == null || Java11.isEmpty(optional)){
			throw new IllegalArgumentException(message);
		}
		return optional;
	}

}
