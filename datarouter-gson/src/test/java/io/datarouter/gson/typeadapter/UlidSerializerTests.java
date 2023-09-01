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

import io.datarouter.gson.GsonTool;
import io.datarouter.types.Ulid;

public class UlidSerializerTests{

	private static final Gson GSON = GsonTool.withoutEnums();
	private static final Ulid ULID = new Ulid("01GFBY0JM7CN0E1J6WNAQHR17P");

	@Test
	public void testRoundTrip(){
		String json = GSON.toJson(ULID);
		String expected = "\"01GFBY0JM7CN0E1J6WNAQHR17P\"";
		Assert.assertEquals(json, expected);
		Ulid output = GSON.fromJson(json, Ulid.class);
		Assert.assertEquals(output, ULID);
	}

	@Test
	public void testWrappedRoundTrip(){
		var input = new TestDto(ULID);
		String json = GSON.toJson(input);
		String expected = """
				{"ulid":"01GFBY0JM7CN0E1J6WNAQHR17P"}""";
		Assert.assertEquals(json, expected);
		TestDto output = GSON.fromJson(json, TestDto.class);
		Assert.assertEquals(output.ulid, ULID);
	}

	private record TestDto(
			Ulid ulid){
	}

}
