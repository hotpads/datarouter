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
package io.datarouter.bytes.blockfile.test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.blockfile.index.BlockfilePointSearchTool;
import io.datarouter.scanner.Scanner;

public class BlockfilePointSearchToolTests{

	/*--------- list of integers ----------*/

	@Test
	public void testFindIntegers(){
		List<Integer> input = List.of(2, 5, 5, 5, 25, 30);
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, Integer::compare, 0),
				Optional.empty());
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, Integer::compare, 3),
				Optional.empty());
		Assert.assertEquals(
				BlockfilePointSearchTool.findFirstInList(input, Integer::compare, 3),
				Optional.empty());
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(input, Integer::compare, 3),
				Optional.empty());
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, Integer::compare, 5),
				Optional.of(5));
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, Integer::compare, 100),
				Optional.empty());
	}

	@Test
	public void testFindStrings(){
		List<String> input = List.of("crayon", "crowd", "fritter", "lake", "land", "lark", "lemon", "lift", "noodle");
		BiFunction<String,String,Integer> compareFirstLetter = (a, b) -> a.substring(0, 1).compareTo(b.substring(0, 1));
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, compareFirstLetter, "a"),
				Optional.empty());
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, compareFirstLetter, "f"),
				Optional.of("fritter"));
		Assert.assertEquals(
				BlockfilePointSearchTool.findFirstInList(input, compareFirstLetter, "c"),
				Optional.of("crayon"));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(input, compareFirstLetter, "c"),
				Optional.of("crowd"));
		Assert.assertEquals(
				BlockfilePointSearchTool.findFirstInList(input, compareFirstLetter, "l"),
				Optional.of("lake"));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(input, compareFirstLetter, "l"),
				Optional.of("lift"));
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(input, compareFirstLetter, "z"),
				Optional.empty());
	}

	/*--------- person by firstName ----------*/

	private record Person(
			String firstName,
			String lastName){

		public static final Comparator<Person> COMPARATOR_FIRST_NAME = Comparator.comparing(Person::firstName);
		public static final BiFunction<Person,String,Integer> COMPARE_FIRST_NAME = (person, firstName) ->
				person.firstName().compareTo(firstName);
	}

	private static final List<Person> PEOPLE = Scanner.of(
			new Person("Bob", "Barker"),
			new Person("Carl", "Sagan"),
			new Person("Carl", "Weathers"),
			new Person("Matthew", "Broderick"),
			new Person("Matthew", "Damon"),
			new Person("Matthew", "Dillon"),
			new Person("Matthew", "LeBlanc"),
			new Person("Matthew", "McConaughey"),
			new Person("Owen", "Wilson"),
			new Person("Paul", "Newman"),
			new Person("Paul", "Rudd"),
			new Person("Paul", "Walker"))
			.sort(Person.COMPARATOR_FIRST_NAME)
			.list();

	@Test
	public void testFindAnyWithFirstName(){
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Bob"),
				Optional.of(new Person("Bob", "Barker")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Owen"),
				Optional.of(new Person("Owen", "Wilson")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findAnyInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Woody"),
				Optional.empty());
	}
	@Test
	public void testFindLastWithFirstName(){
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Bob"),
				Optional.of(new Person("Bob", "Barker")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Carl"),
				Optional.of(new Person("Carl", "Weathers")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Matthew"),
				Optional.of(new Person("Matthew", "McConaughey")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Owen"),
				Optional.of(new Person("Owen", "Wilson")));
		Assert.assertEquals(
				BlockfilePointSearchTool.findLastInList(PEOPLE, Person.COMPARE_FIRST_NAME, "Woody"),
				Optional.empty());
	}
}
