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
package io.datarouter.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

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

	public static <T> void equals(T first, T second){
		if(ObjectTool.notEquals(first, second)){
			throw new IllegalArgumentException(first + " does not equal " + second);
		}
	}

	public static <T> void notEquals(T first, T second){
		if(Objects.equals(first, second)){
			throw new IllegalArgumentException(first + " equals " + second);
		}
	}

	public static void isTrue(boolean argument){
		isTrue(argument, null);
	}

	public static void isTrue(boolean argument, String message){
		if(!argument){
			throw new IllegalArgumentException(message);
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

	public static <T,C extends Collection<T>> C notEmpty(C items){
		return notEmpty(items, null);
	}

	public static <T,C extends Collection<T>> C notEmpty(C items, String message){
		if(items == null || items.isEmpty()){
			throw new IllegalArgumentException(message);
		}
		return items;
	}

	public static class RequireTests{

		@Test
		public void testIsNull(){
			Require.isNull(null);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testIsNullThrowsException(){
			Require.isNull("");
		}

		@Test
		public void testEquals(){
			Require.equals("apple", "apple");
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testEqualsThrowsException(){
			Require.equals("apple", "orange");
		}

		@Test
		public void testNotEquals1(){
			Require.notEquals("apple", "orange");
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testNotEqualsThrowsException(){
			Require.notEquals("apple", "apple");
		}

		@Test
		public void testIsTrue(){
			Require.isTrue(true);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testIsTrueThrowsException(){
			Require.isTrue(false);
		}

		@Test
		public void testGreaterThan(){
			int validated = Require.greaterThan(3, 1);
			Assert.assertEquals(validated, 3);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testGreaterThanThrowsException(){
			Require.greaterThan(5, 5);
		}

		@Test
		public void testLessThan(){
			int validated = Require.lessThan(5, 7);
			Assert.assertEquals(validated, 5);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testLessThanThrowsException(){
			Require.lessThan(5, 5);
		}

		@Test
		public void testContains(){
			Require.contains(Arrays.asList("a", "b"), "a");
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testContainsThrowsException(){
			Require.contains(Arrays.asList("a", "b"), "c");
		}

		@Test
		public void testNotContains(){
			Require.notContains(Arrays.asList("a", "b"), "c");
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testNotContainsThrowsException(){
			Require.notContains(Arrays.asList("a", "b"), "a");
		}

		@Test
		public void testNotEmpty(){
			Require.notEmpty(Arrays.asList("a", "b"));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void testNotEmptyThrowsException(){
			Require.notEmpty(Arrays.asList(), "empty list");
		}

	}

}
