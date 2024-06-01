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
package io.datarouter.graphql.client.util.example.arg;

import io.datarouter.graphql.client.util.config.Ql;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;

public class ExampleOfficeGraphQlArgumentType implements GraphQlArgumentType{

	public final String location;

	public ExampleOfficeGraphQlArgumentType(String location){
		this.location = location;
	}

	@Override
	public GraphQlArgumentType getSample(){
		return new ExampleOfficeGraphQlArgumentType("san francisco");
	}

	public static class ExampleSnackGraphQlArgumentType implements GraphQlArgumentType{

		@Ql(required = true, description = "How many snack types to return")
		public final Integer limit;

		public ExampleSnackGraphQlArgumentType(Integer limit){
			this.limit = limit;
		}

		@Override
		public GraphQlArgumentType getSample(){
			return new ExampleSnackGraphQlArgumentType(3);
		}

	}

}
