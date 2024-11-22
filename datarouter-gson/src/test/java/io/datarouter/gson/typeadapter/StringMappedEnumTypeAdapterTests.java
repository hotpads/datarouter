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
package io.datarouter.gson.typeadapter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.gson.DatarouterGsons;
import io.datarouter.gson.typeadapterfactory.EnumTypeAdapterFactory;

public class StringMappedEnumTypeAdapterTests{

	private static final Gson GSON = DatarouterGsons.withEnums(new AnimalColorEnumTypeAdapterFactory());

	private enum AnimalColor{
		BLACK("black"),
		WHITE("white");

		private static final StringMappedEnum<AnimalColor> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		private final String persistentString;

		AnimalColor(String persistentString){
			this.persistentString = persistentString;
		}

	}

	private record Animal(
			String name,
			AnimalColor color){
	}

	private static class AnimalColorEnumTypeAdapterFactory extends EnumTypeAdapterFactory{

		public AnimalColorEnumTypeAdapterFactory(){
			optionalValuesSilent(AnimalColor.BY_PERSISTENT_STRING, null);
		}
	}

	@Test
	public void testDeserialization(){
		String json = """
				{"name": "cat", "color": "white"}""";
		Animal animal = GSON.fromJson(json, Animal.class);
		Assert.assertNotNull(animal);
		Assert.assertNotNull(animal.color());
		Assert.assertEquals(animal.color(), AnimalColor.WHITE);

		json = """
				{"name": "cat", "color": "black"}""";
		animal = GSON.fromJson(json, Animal.class);
		Assert.assertNotNull(animal);
		Assert.assertNotNull(animal.color());
		Assert.assertEquals(animal.color(), AnimalColor.BLACK);

		json = """
				{"name": "cat", "color": "red"}""";
		animal = GSON.fromJson(json, Animal.class);
		Assert.assertNotNull(animal);
		Assert.assertNull(animal.color());

		json = """
				{"name": "cat"}""";
		animal = GSON.fromJson(json, Animal.class);
		Assert.assertNotNull(animal);
		Assert.assertNull(animal.color());

		json = """
				{"name": "cat", "color": null}""";
		animal = GSON.fromJson(json, Animal.class);
		Assert.assertNotNull(animal);
		Assert.assertNull(animal.color());

		json = """
				[{"name": "cat", "color": null}, {"name": "dog", "color": "white"}]""";
		Animal[] animals = GSON.fromJson(json, Animal[].class);
		Assert.assertNotNull(animal);
		Assert.assertNotEquals(animals.length, 0);
	}
}
