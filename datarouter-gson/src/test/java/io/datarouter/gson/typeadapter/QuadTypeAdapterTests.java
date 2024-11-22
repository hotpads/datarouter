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

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.types.Quad;

public class QuadTypeAdapterTests{

	private static final Gson GSON = DatarouterGsons.withoutEnums();
	private static final Quad QUAD = new Quad("3210123");

	@Test
	public void testRoundTrip(){
		String json = GSON.toJson(QUAD);
		String expected = "\"3210123\"";
		Assert.assertEquals(json, expected);
		Quad output = GSON.fromJson(json, Quad.class);
		Assert.assertEquals(output, QUAD);
	}

	@Test
	public void testWrappedRoundTrip(){
		TestDto input = new TestDto(QUAD);
		String json = GSON.toJson(input);
		String expected = """
				{"quad":"3210123"}""";
		Assert.assertEquals(json, expected);
		TestDto output = GSON.fromJson(json, TestDto.class);
		Assert.assertEquals(output.quad, QUAD);
	}

	private record TestDto(Quad quad){
	}

}