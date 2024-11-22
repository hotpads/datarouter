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
import io.datarouter.types.MilliTimeReversed;

public class MilliTimeReversedTypeAdapterTests{

	private static final Gson GSON = DatarouterGsons.withoutEnums();
	private static final MilliTimeReversed TIME = MilliTimeReversed.ofReversedEpochMilli(10L);

	@Test
	public void testRoundTrip(){
		String json = GSON.toJson(TIME);
		String expected = "10";
		Assert.assertEquals(json, expected);
		MilliTimeReversed output = GSON.fromJson(json, MilliTimeReversed.class);
		Assert.assertEquals(output, TIME);
	}

	@Test
	public void testWrappedRoundTrip(){
		var input = new TestDto(TIME);
		String json = GSON.toJson(input);
		String expected = """
				{"time":10}""";
		Assert.assertEquals(json, expected);
		TestDto output = GSON.fromJson(json, TestDto.class);
		Assert.assertEquals(output.time, TIME);
	}

	private record TestDto(
			MilliTimeReversed time){
	}

}
