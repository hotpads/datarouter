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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkTool{
	private static final Logger logger = LoggerFactory.getLogger(LinkTool.class);

	/**
	 * Convert all field names and values to a map.
	 *
	 * If optional fields are not present, they will not be added to the map
	 */
	public static Map<String,String> getParamFields(BaseLink<?> link){
		Map<String,String> getParams = new LinkedHashMap<>();
		for(Field field : link.getClass().getFields()){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			if(field.getAnnotation(EndpointRequestBody.class) != null){
				continue;
			}
			String key = getFieldName(field);
			Object value = null;
			try{
				value = field.get(link);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			boolean isOptional = field.getType().isAssignableFrom(Optional.class);
			if(isOptional && value == null){
				throw new RuntimeException(String.format(
						"%s: Optional fields cannot be null. '%s' needs to be initialized to Optional.empty().",
						link.getClass().getSimpleName(), key));
			}
			if(value == null){
				throw new RuntimeException(String.format(
						"%s: Fields cannot be null. '%s' needs to be initialized or changed to an Optional field.",
						link.getClass().getSimpleName(), key));
			}
			Optional<String> parsedValue = getValue(field, value);
			parsedValue.ifPresent(paramValue -> getParams.put(key, paramValue));
		}
		return getParams;
	}

	private static String getFieldName(Field field){
		EndpointParam endpointParam = field.getAnnotation(EndpointParam.class);
		return Optional.ofNullable(endpointParam)
				.map(EndpointParam::serializedName)
				.filter(name -> !name.isEmpty())
				.orElseGet(field::getName);
	}

	private static Optional<String> getValue(Field field, Object value){
		if(!field.getType().isAssignableFrom(Optional.class)){
			return Optional.of(value.toString());
		}
		Optional<?> optionalValue = (Optional<?>)value;
		if(optionalValue.isPresent()){
			return Optional.of(optionalValue.get().toString());
		}
		return Optional.empty();
	}

	public static boolean paramIsLinkObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> linkType = parameters[0].getType();
		return method.getParameterCount() == 1 && BaseLink.class.isAssignableFrom(linkType);
	}

}
