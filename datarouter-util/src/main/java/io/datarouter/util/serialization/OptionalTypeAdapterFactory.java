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
package io.datarouter.util.serialization;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This factory creates an adaptor that correctly serialize Optionals.
 * @see OptionalContainerClassTypeAdapterFactory
 */
public class OptionalTypeAdapterFactory implements TypeAdapterFactory{

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken){
		if(typeToken.getRawType() != Optional.class){
			return null;
		}
		Type actualType = ((ParameterizedType)typeToken.getType()).getActualTypeArguments()[0];
		TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(actualType));
		return new OptionalTypeAdapter(typeAdapter);
	}

	private static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>>{

		private final TypeAdapter<E> typeAdapter;

		public OptionalTypeAdapter(TypeAdapter<E> typeAdapter){
			this.typeAdapter = typeAdapter;
		}

		@Override
		public void write(JsonWriter out, Optional<E> value) throws IOException{
			if(value == null || !value.isPresent()){
				out.nullValue();
			}else{
				typeAdapter.write(out, value.get());
			}
		}

		@Override
		public Optional<E> read(JsonReader in) throws IOException{
			if(in.peek() != JsonToken.NULL){
				return Optional.ofNullable(typeAdapter.read(in));
			}
			in.nextNull();
			return Optional.empty();
		}
	}

	public static class Tests{
		private Gson gsonNotNull = new GsonBuilder()
				.registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
				.create();
		private final Gson gsonNullable = new GsonBuilder()
				.serializeNulls() // matter of taste, just for output anyway
				.registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
				.create();

		@Test
		public void serializeNotNullTest(){
			Assert.assertEquals(gsonNotNull.toJson(new BasicOptionalDto(true)),
					"{\"someKey\":\"someValue\",\"someNumber\":42,\"notOptional\":\"117 is prime\"}");
			Assert.assertEquals(gsonNotNull.toJson(new BasicOptionalDto()), "{}");
			Assert.assertEquals(gsonNotNull.toJson(new ComplexOptionalDto(true)),
					"{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"World\"],"
					+ "\"theObject\":{\"someKey\":\"someValue\",\"someNumber\":42,\"notOptional\":\"117 is prime\"}}");
			Assert.assertEquals(gsonNotNull.toJson(new ComplexOptionalDto()), "{}");
			Assert.assertEquals(gsonNotNull.toJson(new AnotatedClass(true)), "{\"field1\":\"field1 value\","
					+ "\"apiName\":\"This should be apiName\"}");
			Assert.assertEquals(gsonNotNull.toJson(new AnotatedClass()), "{}");
		}

		@Test
		public void serializeTestNull(){
			Assert.assertEquals(gsonNullable.toJson(new BasicOptionalDto(true)),
					"{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null,"
					+ "\"notOptional\":\"117 is prime\"}");
			Assert.assertEquals(gsonNullable.toJson(new BasicOptionalDto()),
					"{\"someKey\":null,\"someNumber\":null,\"mayBeNull\":null,\"notOptional\":null}");
			Assert.assertEquals(gsonNullable.toJson(new ComplexOptionalDto(true)),
					"{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"World\"],\"theObject\""
					+ ":{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null,\"notOptional\":"
					+ "\"117 is prime\"}}");
			Assert.assertEquals(gsonNullable.toJson(new AnotatedClass(true)), "{\"field1\":\"field1 value\","
					+ "\"apiName\":\"This should be apiName\"}");
			Assert.assertEquals(gsonNullable.toJson(new AnotatedClass()), "{\"field1\":null,"
					+ "\"apiName\":null}");
		}

		@Test
		public void deserializeBasicTest(){
			BasicOptionalDto basicOptionalDto = gsonNotNull.fromJson("{}", BasicOptionalDto.class);
			Assert.assertNull(basicOptionalDto.mayBeNull);
			Assert.assertNull(basicOptionalDto.someKey);
			Assert.assertNull(basicOptionalDto.someNumber);
			Assert.assertNull(basicOptionalDto.notOptional);

			String basic = "{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}";
			basicOptionalDto = gsonNotNull.fromJson(basic, BasicOptionalDto.class);
			Assert.assertEquals(basicOptionalDto.mayBeNull, Optional.empty());
			Assert.assertEquals(basicOptionalDto.someKey.get(), "someValue");
			Assert.assertEquals(basicOptionalDto.someNumber.get(), new Integer(42));
		}

		@Test
		public void deserializeComplexTest(){
			ComplexOptionalDto complexOptionalDto = gsonNotNull.fromJson("{}", ComplexOptionalDto.class);
			Assert.assertNull(complexOptionalDto.stringArray);
			Assert.assertNull(complexOptionalDto.theObject);
			Assert.assertNull(complexOptionalDto.theTitle);

			String complex = "{\"theTitle\":\"Complex Object\",\"stringArray\":[\"Hello\",\"world\"],"
					+ "\"theObject\":{\"someKey\":\"someValue\",\"someNumber\":42,\"mayBeNull\":null}}";
			complexOptionalDto = gsonNotNull.fromJson(complex, ComplexOptionalDto.class);
			Assert.assertNotNull(complexOptionalDto.stringArray);
			Assert.assertNotNull(complexOptionalDto.theObject);
			Assert.assertEquals(complexOptionalDto.theTitle.get(), "Complex Object");

			String complexMissing = "{\"theTitle\":\"Complex Object\",\"theObject\":null}";
			complexOptionalDto = gsonNotNull.fromJson(complexMissing, ComplexOptionalDto.class);
			Assert.assertNull(complexOptionalDto.stringArray);
			Assert.assertEquals(complexOptionalDto.theObject, Optional.empty());
			Assert.assertEquals(complexOptionalDto.theTitle.get(), "Complex Object");
		}
	}

	private static class BasicOptionalDto{

		private Optional<String> someKey;
		private Optional<Integer> someNumber;
		private Optional<String> mayBeNull;
		private String notOptional;

		public BasicOptionalDto(){
		}

		public BasicOptionalDto(boolean initialize){
			if(initialize){
				someKey = Optional.ofNullable("someValue");
				someNumber = Optional.ofNullable(42);
				mayBeNull = Optional.ofNullable(null);
				notOptional = "117 is prime";
			}
		}
	}

	private static class ComplexOptionalDto{

		Optional<String> theTitle;
		Optional<List<Optional<String>>> stringArray;
		Optional<BasicOptionalDto> theObject;

		public ComplexOptionalDto(){
		}

		public ComplexOptionalDto(boolean initialize){
			if(initialize){
				theTitle = Optional.ofNullable("Complex Object");
				stringArray = Optional.ofNullable(Arrays.asList(Optional.ofNullable("Hello"), Optional.ofNullable(
						"World")));
				theObject = Optional.ofNullable(new BasicOptionalDto(true));
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
