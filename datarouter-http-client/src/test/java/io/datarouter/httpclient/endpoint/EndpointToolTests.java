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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.reflect.TypeToken;

import io.datarouter.httpclient.endpoint.EndpointType.NoOpEndpointType;

public class EndpointToolTests{

	public static class ExampleEndpoint1{
		@EndpointParam(serializedName = "name")
		public String firstName;
	}

	@Test
	public void testFieldName() throws Exception{
		Field field = ExampleEndpoint1.class.getField("firstName");
		Assert.assertEquals(EndpointTool.getFieldName(field), "name");
	}

	public static class Example2 extends BaseEndpoint<Map<String,float[]>,NoOpEndpointType>{
		public Example2(){
			super(null, null, false);
		}
	}

	public static class Example3 extends BaseEndpoint<String,NoOpEndpointType>{
		public Example3(){
			super(null, null, false);
		}
	}

	public static class Example4 extends BaseEndpoint<ExampleEndpoint1,NoOpEndpointType>{
		public Example4(){
			super(null, null, false);
		}
	}

	@Test
	public void testGetParameterizedTypes(){
		Type actual2 = EndpointTool.getResponseType(new Example2());
		Type expected2 = new TypeToken<Map<String,float[]>>(){}.getType();
		Assert.assertEquals(actual2.getTypeName(), expected2.getTypeName());

		Type actual3 = EndpointTool.getResponseType(new Example3());
		Type expected3 = String.class;
		Assert.assertEquals(actual3.getTypeName(), expected3.getTypeName());

		Type actual4 = EndpointTool.getResponseType(new Example4());
		Type expected4 = ExampleEndpoint1.class;
		Assert.assertEquals(actual4.getTypeName(), expected4.getTypeName());
	}

}
