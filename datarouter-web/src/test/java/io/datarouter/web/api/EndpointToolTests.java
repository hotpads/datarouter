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
package io.datarouter.web.api;

import org.testng.annotations.Test;

import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.ParamType;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.pathnode.PathNode;

public class EndpointToolTests{

	private static final PathNode PATH = new PathNode().leaf("");

	public static class ValidateEndpoint1 extends ApiToolTestEndpoint<Void>{

		public final String str;

		public ValidateEndpoint1(String str){
			super(GET, PATH);
			this.str = str;
		}

	}

	public static class ValidateEndpoint2 extends ApiToolTestEndpoint<Void>{

		@RequestBody
		public final String str;

		public ValidateEndpoint2(String str){
			super(GET, PATH);
			this.str = str;
		}

	}

	public static class ValidateEndpoint3 extends ApiToolTestEndpoint<Void>{

		@RequestBody
		public final String str;

		// by default this is a post param
		public final String str2;

		public ValidateEndpoint3(String str, String str2){
			super(POST, PATH);
			this.str = str;
			this.str2 = str2;
		}

	}

	public static class ValidateEndpoint4 extends ApiToolTestEndpoint<Void>{

		@RequestBody
		public final String str;

		@EndpointParam(paramType = ParamType.GET)
		public final String str2;
		@EndpointParam(paramType = ParamType.GET)
		public final Integer number;

		public ValidateEndpoint4(String str, String str2, Integer number){
			super(POST, PATH);
			this.str = str;
			this.str2 = str2;
			this.number = number;
		}

	}

	@Test
	public void validateEndpoint1(){
		EndpointTool.validateBaseEndpoint(new ValidateEndpoint1(""));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validateEndpoint2(){
		EndpointTool.validateBaseEndpoint(new ValidateEndpoint2(""));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validateEndpoint3(){
		EndpointTool.validateBaseEndpoint(new ValidateEndpoint3("", ""));
	}

	@Test
	public void validateEndpoint4(){
		EndpointTool.validateBaseEndpoint(new ValidateEndpoint4("", "", 1));
	}

}
