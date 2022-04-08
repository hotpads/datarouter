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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DeduplicatingConsecutiveScannerTests{

	@Test
	public void simpleTest(){
		List<Integer> duplicates = List.of(0, 0, 1, 2, 3, 3, 4, 5, 9, 20, 20);
		List<Integer> expected = new ArrayList<>(new TreeSet<>(duplicates));
		List<Integer> actual = Scanner.of(duplicates)
				.deduplicateConsecutive()
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void nullTest(){
		List<Integer> duplicates = Arrays.asList(null, null, 1, null, null, 2, 2, null, null, 3, 3, 3);
		List<Integer> expected = Arrays.asList(null, 1, null, 2, null, 3);
		List<Integer> actual = Scanner.of(duplicates)
				.deduplicateConsecutive()
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testDeduplicateConsecutiveBy(){
		List<Person> people = List.of(
				new Person("Bob", 1),
				new Person("Jane", 2),
				new Person("Jane", 3),
				new Person("Sam", 4),
				new Person("Sam", 5));
		List<Person> expected = List.of(
				new Person("Bob", 1),
				new Person("Jane", 2),
				new Person("Sam", 4));
		List<Person> actual = Scanner.of(people)
				.deduplicateConsecutiveBy(Person::getFirstName)
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testDeduplicateConsecutiveByWithNull(){
		List<Person> people = Arrays.asList(
				new Person("Bob", 1),
				null,
				null,
				new Person("Sam", 4),
				new Person("Sam", 5));
		List<Person> expected = Arrays.asList(
				new Person("Bob", 1),
				null,
				new Person("Sam", 4));
		List<Person> actual = Scanner.of(people)
				.deduplicateConsecutiveBy(person -> person == null ? null : person.getFirstName())
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testDeduplicateConsecutiveArray(){
		List<byte[]> arrays = Arrays.asList(
				new byte[]{0},
				new byte[]{0},
				new byte[]{1});

		List<byte[]> expectedWithoutEqualsFunction = Arrays.asList(
				new byte[]{0},
				new byte[]{0},
				new byte[]{1});
		int expectedSizeWithoutEqualsFunction = 3;
		List<byte[]> actualWithoutEqualsFunction = Scanner.of(arrays)
				.deduplicateConsecutive()
				.list();
		Assert.assertEquals(actualWithoutEqualsFunction.size(), expectedSizeWithoutEqualsFunction);
		for(int i = 0; i < expectedSizeWithoutEqualsFunction; ++i){
			Assert.assertEquals(expectedWithoutEqualsFunction.get(i), actualWithoutEqualsFunction.get(i));
		}

		List<byte[]> expectedWithEqualsFunction = Arrays.asList(
				new byte[]{0},
				new byte[]{1});
		int expectedSizeWithEqualsFunction = 2;
		List<byte[]> actualWithEqualsFunction = Scanner.of(arrays)
				.deduplicateConsecutiveBy(Function.identity(), Arrays::equals)
				.list();
		Assert.assertEquals(actualWithEqualsFunction.size(), expectedSizeWithEqualsFunction);
		for(int i = 0; i < expectedSizeWithEqualsFunction; ++i){
			Assert.assertEquals(expectedWithEqualsFunction.get(i), actualWithEqualsFunction.get(i));
		}
	}

	private static class Person{

		private final String firstName;
		private final int age;

		private Person(String firstName, int age){
			this.firstName = firstName;
			this.age = age;
		}

		private String getFirstName(){
			return firstName;
		}

		@Override
		public int hashCode(){
			return Objects.hash(firstName, age);
		}

		@Override
		public boolean equals(Object other){
			if(!(other instanceof Person)){
				return false;
			}
			Person otherPerson = (Person)other;
			return Objects.equals(firstName, otherPerson.firstName)
					&& Objects.equals(age, otherPerson.age);
		}

	}

}
