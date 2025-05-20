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
package io.datarouter.httpclient.endpoint.link;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;

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
			if(shouldIgnoreField(field)){
				continue;
			}
			if(isNameValueListField(field)){
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

	// TODO this isn't supported yet when the Link is used in a Handler param
	public static List<NameValuePair> getNameValueListParamFields(BaseLink<?> link){
		List<NameValuePair> list = new ArrayList<>();
		for(Field field : link.getClass().getFields()){
			if(shouldIgnoreField(field)){
				continue;
			}
			if(!isNameValueListField(field)){
				continue;
			}
			String key = getFieldName(field);
			Object value = null;
			try{
				value = field.get(link);
			}catch(IllegalArgumentException | IllegalAccessException ex){
				logger.error("", ex);
			}
			if(value == null){
				throw new RuntimeException(String.format(
						"%s: List Fields cannot be null. '%s' needs to be initialized to an empty List",
						link.getClass().getSimpleName(), key));
			}
			list.addAll((List<NameValuePair>)value);
		}
		return list;
	}

	private static boolean isNameValueListField(Field field){
		Type genericType = field.getGenericType();
		if(genericType instanceof ParameterizedType parameterizedType){
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			for(Type typeArgument : typeArguments){
				if(((Class<?>)typeArgument).isAssignableFrom(NameValuePair.class)){
					return true;
				}
			}
		}
		return false;
	}

	private static boolean shouldIgnoreField(Field field){
		IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
		if(ignoredField != null){
			return true;
		}
		if(Modifier.isStatic(field.getModifiers())){
			return true;
		}
		if(field.getAnnotation(RequestBody.class) != null){
			return true;
		}
		return false;
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
		return optionalValue.map(Object::toString);
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
