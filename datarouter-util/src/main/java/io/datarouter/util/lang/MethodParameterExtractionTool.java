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
package io.datarouter.util.lang;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.Require;

public class MethodParameterExtractionTool{
	private static final Logger logger = LoggerFactory.getLogger(MethodParameterExtractionTool.class);

	public static Class<?> extractParameterizedTypeFromOptionalParameter(Parameter parameter){
		Require.isTrue(Optional.class.isAssignableFrom(parameter.getType()));

		Type type = parameter.getParameterizedType();
		String typeName = type.getTypeName();
		Require.isTrue(typeName.contains("<"));
		Require.isTrue(typeName.contains(">"));

		// hacky
		int start = typeName.indexOf("<") + 1;
		int end = typeName.lastIndexOf(">");
		String parameterizedType = typeName.substring(start, end);
		Class<?> clazz = null;
		try{
			clazz = Class.forName(parameterizedType);
		}catch(ClassNotFoundException e){
			logger.error("Exception getting class for parameter={} parameterizedType={} ",
					parameter.getName(),
					typeName,
					e);
		}
		return clazz;
	}

}
