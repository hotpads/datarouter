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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.BooleanTool;

/**
 * @deprecated use the JDK {@code Optional<Boolean>}
 */
@Deprecated
public class OptionalBoolean extends OptionalParameter<Boolean>{
	private static final Logger logger = LoggerFactory.getLogger(OptionalBoolean.class);

	public OptionalBoolean(){
	}

	public OptionalBoolean(Boolean optBoolean){
		super(optBoolean);
	}

	@Override
	public Class<Boolean> getInternalType(){
		return Boolean.class;
	}

	@Override
	public OptionalParameter<Boolean> fromString(String stringValue, Method method, Parameter parameter){
		boolean isBoolean = BooleanTool.isBoolean(stringValue);
		if(isBoolean && !"true".equals(stringValue) && !"false".equals(stringValue)){
			logger.warn("Bad boolean parameter value='{}' handler='{}' method='{}' parameter='{}'",
					stringValue,
					method.getDeclaringClass(),
					method.getName(),
					parameter.getName());
		}
		return new OptionalBoolean(isBoolean ? BooleanTool.isTrue(stringValue) : null);
	}

}
