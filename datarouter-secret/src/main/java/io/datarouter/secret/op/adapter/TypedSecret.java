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
package io.datarouter.secret.op.adapter;

import java.util.Objects;

import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

public class TypedSecret<T>{

	private final String name;
	private final T value;

	public TypedSecret(String name, T value){
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(name));
		this.name = name;
		this.value = Objects.requireNonNull(value);
	}

	public String getName(){
		return name;
	}

	public T getValue(){
		return value;
	}

	public static void validateName(String name){
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(name));
	}

	public static void validateSecret(TypedSecret<?> secret){
		Require.noNulls(secret, secret.value);
		validateName(secret.name);
	}

}