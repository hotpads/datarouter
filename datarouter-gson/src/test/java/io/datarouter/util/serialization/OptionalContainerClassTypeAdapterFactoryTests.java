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
package io.datarouter.util.serialization;

import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import io.datarouter.gson.Java9;
import io.datarouter.gson.serialization.OptionalContainerClassTypeAdapterFactory;
import io.datarouter.gson.serialization.OptionalTypeAdapterFactory;

public class OptionalContainerClassTypeAdapterFactoryTests{

	private Gson gsonNotNull = new GsonBuilder()
			.registerTypeAdapterFactory(new OptionalContainerClassTypeAdapterFactory())
			.registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
			.create();
	private final Gson gsonNullable = new GsonBuilder()
			.serializeNulls() // matter of taste, just for output anyway
			.registerTypeAdapterFactory(new OptionalContainerClassTypeAdapterFactory())
			.registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
			.create();

	@Test
	public void serializeNotNullTest(){
		Assert.assertEquals(gsonNotNull.toJson(new BasicOptionalDto(true)),
				"{\"someKey\":\"someValue\",\"someNumber\":42}");
		Assert.assertEquals(gsonNotNull.toJson(new BasicOptionalDto()), "{}");
		Assert.assertEquals(gsonNotNull.toJson(new ComplexOptionalDto(true)),
				"{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"World\"],\"theObject\":"
						+ "{\"someKey\":\"someValue\",\"someNumber\":42}}");
		Assert.assertEquals(gsonNotNull.toJson(new ComplexOptionalDto()), "{}");
		Assert.assertEquals(gsonNotNull.toJson(new ExtensionDto(true)),
				"{\"combo\":\"Large fries\",\"someKey\":\"someValue\",\"someNumber\":42}");
		Assert.assertEquals(gsonNotNull.toJson(new ExtensionDto()), "{}");

	}

	@Test
	public void serializeTestNull(){
		Assert.assertEquals(gsonNullable.toJson(new BasicOptionalDto(true)),
				"{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}");
		Assert.assertEquals(gsonNullable.toJson(new BasicOptionalDto()), "{}");
		Assert.assertEquals(gsonNullable.toJson(new ComplexOptionalDto(true)),
				"{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"World\"],\"theObject\":"
						+ "{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}}");
		Assert.assertEquals(gsonNullable.toJson(new ExtensionDto(true)),
				"{\"extra\":null,\"combo\":\"Large fries\",\"someKey\":\"someValue\",\"someNumber\":42,"
						+ "\"mayBeNull\":null}");
		Assert.assertEquals(gsonNullable.toJson(new ExtensionDto()), "{}");
		Assert.assertEquals(gsonNullable.toJson(new AnotatedClass(true)), "{\"field1\":\"field1 value\","
				+ "\"apiName\":\"This should be apiName\"}");
		Assert.assertEquals(gsonNullable.toJson(new AnotatedClass()), "{}");
	}

	@Test
	public void deserializeBasicTest(){
		BasicOptionalDto basicOptionalDto = gsonNullable.fromJson("{}", BasicOptionalDto.class);
		Assert.assertNull(basicOptionalDto.mayBeNull);
		Assert.assertNull(basicOptionalDto.someKey);
		Assert.assertNull(basicOptionalDto.someNumber);

		String basic = "{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}";
		basicOptionalDto = gsonNullable.fromJson(basic, BasicOptionalDto.class);
		Assert.assertEquals(basicOptionalDto.mayBeNull, Optional.empty());
		Assert.assertEquals(basicOptionalDto.someKey.get(), "someValue");
		Assert.assertEquals(basicOptionalDto.someNumber.get(), Integer.valueOf(42));
	}

	@Test
	public void deserializeComplexTest(){
		ComplexOptionalDto complexOptionalDto = gsonNullable.fromJson("{}", ComplexOptionalDto.class);
		Assert.assertNull(complexOptionalDto.stringArray);
		Assert.assertNull(complexOptionalDto.theObject);
		Assert.assertNull(complexOptionalDto.theTitle);

		String complex = "{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"world\"],"
				+ "\"theObject\":{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}}";
		complexOptionalDto = gsonNullable.fromJson(complex, ComplexOptionalDto.class);
		Assert.assertNotNull(complexOptionalDto.stringArray);
		Assert.assertNotNull(complexOptionalDto.theObject);
		Assert.assertEquals(complexOptionalDto.theTitle.get(), "Complex Object");

		String complexMissing = "{\"theTitle\":\"Complex Object\",\"theObject\":null}";
		complexOptionalDto = gsonNullable.fromJson(complexMissing, ComplexOptionalDto.class);
		Assert.assertNull(complexOptionalDto.stringArray);
		Assert.assertEquals(complexOptionalDto.theObject, Optional.empty());
		Assert.assertEquals(complexOptionalDto.theTitle.get(), "Complex Object");
	}

	private static class BasicOptionalDto{

		@SuppressWarnings("unused")
		private static Optional<String> shouldNotSerialize;
		private Optional<String> someKey;
		private Optional<Integer> someNumber;
		private Optional<String> mayBeNull;

		public BasicOptionalDto(){
		}

		public BasicOptionalDto(boolean initialize){
			if(initialize){
				someKey = Optional.ofNullable("someValue");
				someNumber = Optional.ofNullable(42);
				mayBeNull = Optional.ofNullable(null);
				shouldNotSerialize = Optional.ofNullable("invisible");
			}
		}
	}

	private static class ComplexOptionalDto{

		private Optional<String> theTitle;
		private Optional<List<Optional<String>>> stringArray;
		private Optional<BasicOptionalDto> theObject;

		public ComplexOptionalDto(){
		}

		public ComplexOptionalDto(boolean initialize){
			if(initialize){
				theTitle = Optional.ofNullable("Complex Object");
				stringArray = Optional.ofNullable(Java9.listOf(
						Optional.ofNullable("Hello"),
						Optional.ofNullable("World")));
				theObject = Optional.ofNullable(new BasicOptionalDto(true));
			}
		}
	}

	private static class ExtensionDto extends BasicOptionalDto{

		@SuppressWarnings("unused")
		private Optional<String> extra;
		@SuppressWarnings("unused")
		private Optional<String> combo;

		public ExtensionDto(){
		}

		public ExtensionDto(boolean initialize){
			super(initialize);
			if(initialize){
				extra = Optional.ofNullable(null);
				combo = Optional.ofNullable("Large fries");
			}
		}
	}

	private static class AnotatedClass{
		@SuppressWarnings("unused")
		public Optional<String> field1;
		@SerializedName("apiName")
		public Optional<String> codeName;

		public AnotatedClass(){
		}

		public AnotatedClass(boolean initialize){
			if(initialize){
				field1 = Optional.ofNullable("field1 value");
				codeName = Optional.ofNullable("This should be apiName");
			}
		}
	}
}