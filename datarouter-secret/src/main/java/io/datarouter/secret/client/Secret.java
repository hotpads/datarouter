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
package io.datarouter.secret.client;

import java.util.Objects;

import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

//TODO metadata would include version(s), tags, description, created/updated info, etc.
//TODO add metadata here or in a separate class?
//TODO how to get around difficulty of requiring value to always exist when getting only metadata?
//TODO gonna need a builder for this probably (description, version, tags, etc.)
public final class Secret{

	private final String name;
	private final String value;

	public Secret(String name, String value){
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(name));
		this.name = name;
		this.value = Objects.requireNonNull(value);
	}

	public String getName(){
		return name;
	}

	public String getValue(){
		return value;
	}

	public static void validateName(String name){
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(name));
	}

	public static void validateSecret(Secret secret){
		Require.noNulls(secret, secret.value);
		validateName(secret.name);
	}

}
