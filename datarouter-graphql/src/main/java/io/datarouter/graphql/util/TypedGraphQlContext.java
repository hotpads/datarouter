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
package io.datarouter.graphql.util;

import java.util.Optional;

import graphql.GraphQLContext;

public class TypedGraphQlContext{

	private final GraphQLContext context;

	public TypedGraphQlContext(GraphQLContext context){
		this.context = context;
	}

	public <T> Optional<T> find(GraphQlContextKey<T> key){
		@SuppressWarnings("unchecked") // safety enforced by the put method
		T attribute = (T)context.get(key.name);
		return Optional.ofNullable(attribute);
	}

	public <T> void put(GraphQlContextKey<T> key, T value){
		if(value == null){
			return;
		}
		context.put(key.name, value);
	}

}
