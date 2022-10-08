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
package io.datarouter.httpclient.endpoint;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.gson.serialization.EnumTypeAdapterFactory;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.json.JsonSerializer;
import io.datarouter.pathnode.PathNode;

public class EndpointGsonSerializationTests{

	private enum TestEnum1{
		DOG("dog"),
		PUPPY("puppy"),
		;

		public static final StringMappedEnum<TestEnum1> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		public final String persistentString;

		TestEnum1(String persistentString){
			this.persistentString = persistentString;
		}

	}

	private enum TestEnum2{
		CAT("cat"),
		KITTEN("kitten"),
		;

		public static final StringMappedEnum<TestEnum2> BY_NAME
				= new StringMappedEnum<>(values(), TestEnum2::name);

		@SuppressWarnings("unused")
		public final String persistentString;

		TestEnum2(String persistentString){
			this.persistentString = persistentString;
		}

	}

	private enum TestEnum3{
		RAT,
		MOUSE,
		;
	}

	private static class TestEnumTypeAdapterFactory extends EnumTypeAdapterFactory{

		public TestEnumTypeAdapterFactory(){
			allowUnregistered();
			registerStringMappedEnumRequired(TestEnum1.BY_PERSISTENT_STRING);
			registerStringMappedEnumRequired(TestEnum2.BY_NAME);
		}

	}

	private static class TestSerializer extends GsonJsonSerializer{

		public static final Gson GSON = GsonTool.GSON.newBuilder()
				.registerTypeAdapterFactory(new TestEnumTypeAdapterFactory())
				.create();

		public TestSerializer(){
			super(GSON);
		}

	}

	public static class TestEndpoint extends EndpointToolTestEndpoint<Void>{

		public final String string;
		public final Integer number;
		public final Boolean bool;

		public final TestEnum1 enum1;
		public final TestEnum2 enum2;
		public final TestEnum3 enum3;

//		public final Date date;
		public final LocalDate localDate;
		public final Duration duration;
		public final Instant instant;

		public Optional<String> optString = Optional.empty();
		public Optional<Integer> optNumber = Optional.empty();
		public Optional<Boolean> optBool = Optional.empty();

		public Optional<TestEnum1> optEnum1 = Optional.empty();
		public Optional<TestEnum2> optEnum2 = Optional.empty();
		public Optional<TestEnum3> optEnum3 = Optional.empty();

		public Optional<Date> optDate = Optional.empty();
		public Optional<LocalDate> optLocalDate = Optional.empty();
		public Optional<Duration> optDuration = Optional.empty();
		public Optional<Instant> optInstant = Optional.empty();

		public TestEndpoint(
				String string,
				Integer number,
				Boolean bool,

				TestEnum1 enum1,
				TestEnum2 enum2,
				TestEnum3 enum3,

//				Date date,
				LocalDate localDate,
				Duration duration,
				Instant instant){
			super(GET, new PathNode().leaf("test"));

			this.string = string;
			this.number = number;
			this.bool = bool;

			this.enum1 = enum1;
			this.enum2 = enum2;
			this.enum3 = enum3;

//			this.date = date;
			this.localDate = localDate;
			this.duration = duration;
			this.instant = instant;
		}

		public TestEndpoint withOptString(String optString){
			this.optString = Optional.of(optString);
			return this;
		}

		public TestEndpoint withOptInteger(Integer optNumber){
			this.optNumber = Optional.of(optNumber);
			return this;
		}

		public TestEndpoint withOptBoolean(boolean optBool){
			this.optBool = Optional.of(optBool);
			return this;
		}

		public TestEndpoint withOptEnum1(TestEnum1 optEnum1){
			this.optEnum1 = Optional.of(optEnum1);
			return this;
		}

		public TestEndpoint withOptEnum2(TestEnum2 optEnum2){
			this.optEnum2 = Optional.of(optEnum2);
			return this;
		}

		public TestEndpoint withOptEnum3(TestEnum3 optEnum3){
			this.optEnum3 = Optional.of(optEnum3);
			return this;
		}

		public TestEndpoint withOptDate(Date optDate){
			this.optDate = Optional.of(optDate);
			return this;
		}

		public TestEndpoint withOptLocalDate(LocalDate optLocalDate){
			this.optLocalDate = Optional.of(optLocalDate);
			return this;
		}

		public TestEndpoint withOptDuration(Duration optDuration){
			this.optDuration = Optional.of(optDuration);
			return this;
		}

		public TestEndpoint withOptInstant(Instant optInstant){
			this.optInstant = Optional.of(optInstant);
			return this;
		}

	}

	private static final JsonSerializer SERIALIZER = new TestSerializer();

	@Test
	public void testSerialization(){
		LocalDate localDate = LocalDate.of(2020, 1, 1);
		Date date = Date.from(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant());

		var endpoint = new TestEndpoint(
				"hello world",
				1,
				true,

				TestEnum1.DOG,
				TestEnum2.CAT,
				TestEnum3.RAT,

//				date,
				localDate,
				Duration.ofMillis(1),
				Instant.MIN)

				.withOptString("foo bar")
				.withOptInteger(2)
				.withOptBoolean(true)

				.withOptEnum1(TestEnum1.PUPPY)
				.withOptEnum2(TestEnum2.KITTEN)
				.withOptEnum3(TestEnum3.MOUSE)

//				.withOptDate(date)
				.withOptLocalDate(localDate)
				.withOptDuration(Duration.ofMillis(1))
				.withOptInstant(Instant.MIN)

				;

		Map<String,String> expected = Map.ofEntries(
				Map.entry("string", "hello world"),
				Map.entry("number", "1"),
				Map.entry("bool", "true"),

				Map.entry("enum1", "\"dog\""),
				Map.entry("enum2", "\"CAT\""),
				Map.entry("enum3", "\"RAT\""),

//				Map.entry("date", "\"Dec 31, 2019, 4:00:00 PM\""),
				Map.entry("localDate", "{\"year\":2020,\"month\":1,\"day\":1}"),
				Map.entry("duration", "{\"seconds\":0,\"nanos\":1000000}"),
				Map.entry("instant", "{\"seconds\":-31557014167219200,\"nanos\":0}"),

				Map.entry("optString", "foo bar"),
				Map.entry("optNumber", "2"),
				Map.entry("optBool", "true"),

				Map.entry("optEnum1", "\"puppy\""),
				Map.entry("optEnum2", "\"KITTEN\""),
				Map.entry("optEnum3", "\"MOUSE\""),

//				Map.entry("optDate", "\"Dec 31, 2019, 4:00:00 PM\""),
				Map.entry("optLocalDate", "{\"year\":2020,\"month\":1,\"day\":1}"),
				Map.entry("optDuration", "{\"seconds\":0,\"nanos\":1000000}"),
				Map.entry("optInstant", "{\"seconds\":-31557014167219200,\"nanos\":0}"));

		Map<String,String> actual = EndpointTool.getParamFields(endpoint, SERIALIZER).getParams;
		actual.forEach((key, actualValue) -> {
			String expectedValue = expected.get(key);
			Assert.assertEquals(actualValue, expectedValue, key);
		});
	}

	@Test
	public void testDeserialization(){
		SERIALIZER.deserialize("\"dog\"", TestEnum1.class);
		SERIALIZER.deserialize("dog", TestEnum1.class);

		SERIALIZER.deserialize("CAT", TestEnum2.class);
		SERIALIZER.deserialize("\"CAT\"", TestEnum2.class);

		SERIALIZER.deserialize("RAT", TestEnum3.class);
		SERIALIZER.deserialize("\"RAT\"", TestEnum3.class);
	}


}
