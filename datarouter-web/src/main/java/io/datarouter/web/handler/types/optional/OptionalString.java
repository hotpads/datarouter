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
package io.datarouter.web.handler.types.optional;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @deprecated use the JDK {@code Optional<String>}
 */
@Deprecated
public class OptionalString extends OptionalParameter<String>{

	public OptionalString(){
	}

	public OptionalString(String optString){
		super(optString);
	}

	@Override
	public Class<String> getInternalType(){
		return String.class;
	}

	@Override
	public OptionalParameter<String> fromString(String stringValue, Method method, Parameter parameter){
		return new OptionalString(stringValue);
	}

}
