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
package io.datarouter.httpclient.json;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class GsonJsonSeralizerTests{

	private static final GsonJsonSerializer GSON_JSON_SERIALIZER = new GsonJsonSerializer(HttpClientGsonTool.GSON);

	@Test(expectedExceptions = JsonSyntaxException.class)
	public void deserializeExpectJsonSyntaxExceptionTest(){
		GSON_JSON_SERIALIZER.deserialize("{\"integer:", Dto.class);
	}

	@Test(expectedExceptions = JsonParseException.class)
	public void deserializeJsonParseExceptionTest(){
		GSON_JSON_SERIALIZER.deserialize("{\"integer\":0.3,\"string\":\"bla\"}", Dto.class);
	}

	@Test
	public void deserializeTest(){
		Assert.assertNotNull(GSON_JSON_SERIALIZER.deserialize("{\"integer\":1,\"string\":\"bla\"}", Dto.class));
	}

	private static class Dto{

		@SuppressWarnings("unused")
		public Integer integer;
		@SuppressWarnings("unused")
		public String string;
	}

}
