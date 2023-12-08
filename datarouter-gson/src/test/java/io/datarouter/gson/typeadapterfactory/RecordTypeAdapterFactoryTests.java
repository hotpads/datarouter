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
package io.datarouter.gson.typeadapterfactory;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import io.datarouter.gson.GsonTool;

public class RecordTypeAdapterFactoryTests{

	private static final Gson GSON = GsonTool.builderWithoutEnums()
			.setPrettyPrinting()
			.create();

	public record Person(
			@SerializedName("firstname")
			String firstName,
			String lastName,
			int age,
			Optional<String> nickname){
	}

	public record Dto<T>(
			T person,
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
		Person person = new Person(
				"Bob",
				"Smith",
				5,
				Optional.of("Bobby"));
		Dto<Person> dto = new Dto<>(
				person,
				5,
				List.of("a", "b", "c"),
				false,
				Instant.MIN,
				Duration.ofHours(2),
				LocalDate.of(2022, 1, 1),
				null);
		String json = GSON.toJson(dto);
		Assert.assertEquals(json, JSON_STRING);
	}

	@Test
	public void testDeserialization(){
		Type type = new TypeToken<Dto<Person>>(){}.getType();
		Dto<Person> dto = GSON.fromJson(JSON_STRING, type);
		Assert.assertEquals(dto.person.firstName, "Bob");
		Assert.assertEquals(dto.person.lastName, "Smith");
		Assert.assertEquals(dto.person.age, 5);
		Assert.assertEquals(dto.person.nickname.get(), "Bobby");
		Assert.assertEquals(dto.ageBoxed, 5);
		Assert.assertEquals(dto.list, List.of("a", "b", "c"));
		Assert.assertFalse(dto.bool);
		Assert.assertEquals(dto.instant, Instant.MIN);
		Assert.assertEquals(dto.duration, Duration.ofHours(2));
		Assert.assertNull(dto.nullCheck);
	}

}
