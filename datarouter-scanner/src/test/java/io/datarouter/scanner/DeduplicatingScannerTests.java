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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DeduplicatingScannerTests{

	@Test
	public void simpleTest(){
		List<Integer> duplicates = Arrays.asList(0, 0, 1, 2, 3, 3, 4, 5, 9, 20, 20);
		List<Integer> expected = new ArrayList<>(new TreeSet<>(duplicates));
		List<Integer> actual = Scanner.of(duplicates)
				.deduplicate()
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void deduplicateByMethodTest(){
		List<Person> people = List.of(
				new Person("Bob", 1),
				new Person("Jane", 2),
				new Person("Jane", 3),
				new Person("Sam", 4),
				new Person("Sam", 5));
		Assert.assertEquals(new HashSet<>(people).size(), people.size());
		Set<String> names = new HashSet<>();
		List<Person> expected = people.stream()
				.filter(person -> names.add(person.getFirstName()))
				.collect(Collectors.toList());
		List<Person> actual = Scanner.of(people)
				.deduplicate(Person::getFirstName)
				.list();
		Assert.assertEquals(actual, expected);
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
