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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.JavaEndpointTool;
import io.datarouter.httpclient.endpoint.JavaEndpointTool.ParamsKeysMap;
import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.FormData;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.ParamType;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.web.api.external.BaseExternalEndpoint;
import io.datarouter.web.api.mobile.BaseMobileEndpoint;
import io.datarouter.web.api.web.BaseWebApi;
import io.datarouter.web.dispatcher.DispatchType;

public class EndpointTool{
	private static final Logger logger = LoggerFactory.getLogger(EndpointTool.class);

	public static DispatchType getDispatchTypeForEndpoint(Class<? extends BaseEndpoint> baseClass){
		if(BaseJavaEndpoint.class.isAssignableFrom(baseClass)){
			return DispatchType.JAVA_ENDPOINT;
		}else if(BaseWebApi.class.isAssignableFrom(baseClass)){
			return DispatchType.WEB_API;
		}else if(BaseMobileEndpoint.class.isAssignableFrom(baseClass)){
			return DispatchType.MOBILE_ENDPOINT;
		}
		return DispatchType.DEFAULT;
	}

	public static boolean paramIsBaseEndpointObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseEndpoint.class.isAssignableFrom(endpointType);
	}

	private static ParamsKeysMap getRequiredKeys(Field[] fields, HttpRequestMethod method){
		List<String> getKeys = new LinkedList<>();
		List<String> postKeys = new LinkedList<>();
		for(Field field : fields){
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			if(field.getAnnotation(RequestBody.class) != null){
				continue;
			}
			boolean isOptional = field.getType().isAssignableFrom(Optional.class);
			if(isOptional){
				continue;
			}

			String key = JavaEndpointTool.getFieldName(field);

			EndpointParam param = field.getAnnotation(EndpointParam.class);
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				if(method == HttpRequestMethod.GET){
					getKeys.add(key);
				}else if(method == HttpRequestMethod.POST){
					postKeys.add(key);
				}
				continue;
			}
			ParamType paramType = param.paramType();
			if(paramType == ParamType.GET){
				getKeys.add(key);
			}else if(paramType == ParamType.POST){
				postKeys.add(key);
			}
		}
		return new ParamsKeysMap(getKeys, postKeys);
	}

	public static ParamsKeysMap getRequiredKeys(BaseEndpoint baseEndpoint){
		return getRequiredKeys(baseEndpoint.getClass().getFields(), baseEndpoint.method);
	}

	public static Optional<Field> findFormData(Field[] fields){
		return Stream.of(fields)
				.filter(field -> field.isAnnotationPresent(FormData.class))
				.findFirst();
	}

	public static Optional<Field> findRequestBody(Field[] fields){
		return Stream.of(fields)
				.filter(field -> field.isAnnotationPresent(RequestBody.class))
				.findFirst();
	}

	public static boolean paramIsJavaEndpointObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseJavaEndpoint.class.isAssignableFrom(endpointType);
	}

	public static boolean paramIsWebApiObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseWebApi.class.isAssignableFrom(endpointType);
	}

	public static boolean paramIsMobileEndpointObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseMobileEndpoint.class.isAssignableFrom(endpointType);
	}

	public static boolean paramIsExternalEndpointObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseExternalEndpoint.class.isAssignableFrom(endpointType);
	}

	public static boolean paramIsLinkObject(Method method){
		if(method.getParameterCount() != 1){
			return false;
		}
		Parameter[] parameters = method.getParameters();
		Class<?> endpointType = parameters[0].getType();
		return BaseLink.class.isAssignableFrom(endpointType);
	}

	public static void validateBaseEndpoint(BaseEndpoint baseEndpoint){
		HttpRequestMethod requestType = baseEndpoint.method;
		if(requestType == HttpRequestMethod.GET){
			if(hasEntity(baseEndpoint.getClass())){
				String message = baseEndpoint.getClass().getSimpleName() + " - GET request cannot have a POST-body";
				throw new IllegalArgumentException(message);
			}
		}
		// This is not a hard protocol restriction.
		// Datarouter has trouble decoding if there is a body and post param for a POST request.
		if(requestType == HttpRequestMethod.POST){
			if(hasEntity(baseEndpoint.getClass()) && containsParamOfType(baseEndpoint, ParamType.POST)){
				String message = baseEndpoint.getClass().getSimpleName()
						+ " - Request cannot have a POST-body and POST params";
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
		}
	}

	private static boolean hasEntity(Class<?> clazz){
		for(Field field : clazz.getFields()){
			if(field.isAnnotationPresent(RequestBody.class)){
				return true;
			}
		}
		return false;
	}

	@Deprecated
	private static boolean containsParamOfType(BaseEndpoint baseEndpoint, ParamType type){
		return containsParamOfType(baseEndpoint.getClass(), type);
	}

	private static boolean containsParamOfType(Class<?> clazz, ParamType type){
		for(Field field : clazz.getFields()){
			if(field.getAnnotation(IgnoredField.class) != null){
				continue;
			}
			if(field.getAnnotation(RequestBody.class) != null){
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			EndpointParam param = field.getAnnotation(EndpointParam.class);
			ParamType paramType;
			if(param == null || param.paramType() == null || param.paramType() == ParamType.DEFAULT){
				paramType = type;
			}else{
				paramType = param.paramType();
			}
			if(paramType == ParamType.GET && type == ParamType.GET){
				return true;
			}
			if(paramType == ParamType.POST && type == ParamType.POST){
				return true;
			}
		}
		return false;
	}
}
