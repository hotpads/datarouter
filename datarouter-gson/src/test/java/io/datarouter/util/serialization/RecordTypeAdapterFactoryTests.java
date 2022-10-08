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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.annotations.SerializedName;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.gson.serialization.RecordTypeAdapterFactory;

public class RecordTypeAdapterFactoryTests{

	public record Person(
			@SerializedName("firstname")
			String firstName,
			String lastName,
			int age,
			Optional<String> nickname){
	}

	public record Dto(
			Person person,
			Integer ageBoxed,
			List<String> list,
			boolean bool,
			Instant instant,
			Duration duration,
			LocalDate localDate,
			String nullCheck){

		public Dto(
				boolean bool,
				Instant instant,
				Duration duration,
				LocalDate localDate,
				String nullCheck){
			this(null, null, null, bool, instant, duration, localDate, nullCheck);
		}

	}

	private static final String JSON_STRING = """
		{
		  "person": {
		    "firstname": "Bob",
		    "lastName": "Smith",
		    "age": 5,
		    "nickname": {
		      "value": "Bobby"
		    }
		  },
		  "ageBoxed": 5,
		  "list": [
		    "a",
		    "b",
		    "c"
		  ],
		  "bool": false,
		  "instant": {
		    "seconds": -31557014167219200,
		    "nanos": 0
		  },
		  "duration": {
		    "seconds": 7200,
		    "nanos": 0
		  },
		  "localDate": {
		    "year": 2022,
		    "month": 1,
		    "day": 1
		  }
		}""";

	@Test
	public void testSerialization(){
		var person = new Person(
				"Bob",
				"Smith",
				5,
				Optional.of("Bobby"));
		var dto = new Dto(
				person,
				5,
				List.of("a", "b", "c"),
				false,
				Instant.MIN,
				Duration.ofHours(2),
				LocalDate.of(2022, 1, 1),
				null);
		String json = GsonTool.GSON_PRETTY_PRINT.toJson(dto);
		Assert.assertEquals(json, JSON_STRING);
	}

	@Test
	public void testDeserialization(){
		Dto dto = GsonTool.GSON_PRETTY_PRINT.fromJson(JSON_STRING, Dto.class);
		Assert.assertEquals(dto.person.firstName, "Bob");
		Assert.assertEquals(dto.person.lastName, "Smith");
		Assert.assertEquals(dto.person.age, 5);
		Assert.assertEquals(dto.person.nickname.get(), "Bobby");
		Assert.assertEquals(dto.ageBoxed, 5);
		Assert.assertEquals(dto.list, List.of("a", "b", "c"));
		Assert.assertEquals(dto.bool, false);
		Assert.assertEquals(dto.instant, Instant.MIN);
		Assert.assertEquals(dto.duration, Duration.ofHours(2));
		Assert.assertNull(dto.nullCheck);
	}

	@Test
	public void testClassLocation(){
		Assert.assertEquals(RecordTypeAdapterFactory.class.getCanonicalName(),
				"io.datarouter.gson.serialization.RecordTypeAdapterFactory",
				"This file is referenced in the datarouter-parent/pom.xml");
	}

}
