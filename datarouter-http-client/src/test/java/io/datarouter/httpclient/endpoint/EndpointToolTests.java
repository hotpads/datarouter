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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.reflect.TypeToken;

import io.datarouter.httpclient.endpoint.EndpointType.NoOpEndpointType;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.pathnode.PathNode;

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

	public static class Example5 extends BaseEndpoint<Void,NoOpEndpointType>{

		public Optional<String> str = Optional.empty();

		public Example5(){
			super(HttpRequestMethod.GET, new PathNode().leaf(""), false);
		}
	}

	public static class Example6 extends BaseEndpoint<Void,NoOpEndpointType>{

		public Optional<String> str;

		public Example6(){
			super(HttpRequestMethod.GET, new PathNode().leaf(""), false);
			this.str = Optional.empty();
		}
	}

	public static class Example7 extends BaseEndpoint<Void,NoOpEndpointType>{

		public Optional<String> optionalString;

		public Example7(){
			super(HttpRequestMethod.GET, new PathNode().leaf(""), false);
		}
	}

	@Test
	public void testToDatarouterHttpRequest() throws URISyntaxException{
		Example5 endpoint5 = new Example5();
		endpoint5.setUrlPrefix(new URI(""));
		EndpointTool.toDatarouterHttpRequest(endpoint5);

		Example6 endpoint6 = new Example6();
		endpoint6.setUrlPrefix(new URI(""));
		EndpointTool.toDatarouterHttpRequest(endpoint6);
	}

	/**
	 * Official Behavior of Endpoints
	 *
	 * Endpoints cannot have null values for optional fields. The optional fields must be initialized
	 */
	@Test(expectedExceptions = RuntimeException.class)
	public void testToDatarouterHttpRequestThrows() throws URISyntaxException{
		Example7 endpoint = new Example7();
		endpoint.setUrlPrefix(new URI(""));
		EndpointTool.toDatarouterHttpRequest(endpoint);
	}

	public static class Example8 extends BaseEndpoint<Void,NoOpEndpointType>{

		public final String str;

		public Example8(String str){
			super(HttpRequestMethod.GET, new PathNode().leaf(""), false);
			this.str = str;
		}
	}

	/**
	 * Official Behavior of Endpoints
	 *
	 * Endpoints cannot have null values for final fields. Null final fields are rejected by the client, and will throw
	 * an exception
	 */
	@Test(expectedExceptions = RuntimeException.class)
	public void testEndpointNullParams() throws URISyntaxException{
		Example8 endpoint = new Example8(null);
		endpoint.setUrlPrefix(new URI(""));
		EndpointTool.toDatarouterHttpRequest(endpoint);
	}

	@Test
	public void testEndpointEmptyStringParams() throws URISyntaxException{
		Example8 endpoint = new Example8("");
		endpoint.setUrlPrefix(new URI(""));
		EndpointTool.toDatarouterHttpRequest(endpoint);
	}


	public static class Example9 extends BaseEndpoint<Void,NoOpEndpointType>{

		public final String str;

		public Example9(String str){
			super(HttpRequestMethod.POST, new PathNode().leaf(""), false);
			this.str = str;
		}
	}

}
