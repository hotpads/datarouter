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
package io.datarouter.graphql.client.util.query;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.graphql.client.util.type.GraphQlType;

public class GraphQlClientToolTests{

	@Test
	public void testBuildArgumentStringForList(){
		List<FooBar> fooBarList = List.of(
				new FooBar("string1", 1),
				new FooBar("string2", 2));
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(fooBarList),
				"[{firstField:\"string1\",intField:1},{firstField:\"string2\",intField:2}]");

		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of("foo", "bar")),
				"[\"foo\",\"bar\"]");
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of(1, 2, 3, 4, 5)),
				"[1,2,3,4,5]");
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of(true, true, false, false, true)),
				"[true,true,false,false,true]");
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of(1.2, 3.4, 5.6, 7.8, 9.0)),
				"[1.2,3.4,5.6,7.8,9.0]");
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of(123L, 5678L, 90123)),
				"[123,5678,90123]");
		Assert.assertEquals(GraphQlClientTool.buildArgumentStringForList(List.of(1.2f, 3.4f, 5.6f, 7.8f, 9.0f)),
				"[1.2,3.4,5.6,7.8,9.0]");
	}

	public static class FooBar implements GraphQlType{

		public final String firstField;
		public final Integer intField;

		public FooBar(String firstField, Integer intField){
			this.firstField = firstField;
			this.intField = intField;
		}

	}
}
