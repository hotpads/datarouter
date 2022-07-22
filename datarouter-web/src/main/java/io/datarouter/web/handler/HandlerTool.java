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
package io.datarouter.web.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.BaseLink;
import io.datarouter.httpclient.endpoint.EndpointTool;
import io.datarouter.httpclient.endpoint.LinkTool;
import io.datarouter.httpclient.endpoint.NoOpResponseType;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.types.optional.OptionalParameter;

public class HandlerTool{

	public static Optional<?> getParameterValue(Object parameterValue){
		if(parameterValue instanceof OptionalParameter){
			return ((OptionalParameter<?>)parameterValue).getOptional();
		}
		return Optional.ofNullable(parameterValue);
	}

	@SuppressWarnings("deprecation")
	public static void assertHandlerHasMethod(Class<? extends BaseHandler> handlerClass, String path){
		for(Method method : handlerClass.getDeclaredMethods()){
			if(Modifier.isStatic(method.getModifiers())){
				continue;
			}
			if(method.getAnnotation(Handler.class) == null){
				continue;
			}
			if(method.getAnnotation(Handler.class).defaultHandler()){
				// validation doesn't work when defaultHandler = true
				return;
			}
			String methodName = method.getName();
			if(path.equals(methodName)){
				if(!EndpointTool.paramIsEndpointObject(method)){
					return;
				}
				Class<?> endpointType = method.getParameters()[0].getType();
				@SuppressWarnings("unchecked")
				BaseEndpoint<?,?> endpoint = ReflectionTool.createWithoutNoArgs(
						(Class<? extends BaseEndpoint<?,?>>)endpointType);
				String endpointPath = Scanner.of(endpoint.pathNode.toSlashedString().split("/"))
						.findLast()
						.get();
				if(!methodName.equals(endpointPath)){
					throw new IllegalArgumentException(String.format(
							"Handler-Endpoint Mismatch. handler=%s endpoint=%s, endpointPath=%s methodName=%s "
									+ "dispatchRulePath=%s",
							handlerClass.getSimpleName(),
							endpoint.getClass().getSimpleName(),
							endpointPath,
							methodName,
							path));
				}
				Type methodReturnType = method.getGenericReturnType();
				boolean isPrimitive = method.getReturnType().isPrimitive();
				if(isPrimitive){
					methodReturnType = ClassTool.getBoxedFromPrimitive(methodReturnType);
				}
				Type endpointResponseType = EndpointTool.getResponseType(endpoint);
				if(endpointResponseType.equals(NoOpResponseType.class)){
					return;
				}
				if(!methodReturnType.equals(endpointResponseType)){
					throw new IllegalArgumentException(String.format(
							"Handler-Endpoint Response Type Mismatch. handler=%s endpoint=%s handlerReturnType=%s "
									+ "endpointResponseType=%s",
							handlerClass.getSimpleName(),
							endpoint.getClass().getSimpleName(),
							methodReturnType.getTypeName(),
							endpointResponseType.getTypeName()));
				}
				return;
			}
		}
		throw new IllegalArgumentException(handlerClass.getSimpleName() + " does not have a method that matches "
				+ path);
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	public static List<Class<? extends BaseEndpoint<?,?>>> getEndpointsFromHandler(
			Class<? extends BaseHandler> handler){
		List<Class<? extends BaseEndpoint<?,?>>> endpoints = new ArrayList<>();
		for(Method method : handler.getDeclaredMethods()){
			if(Modifier.isStatic(method.getModifiers())){
				continue;
			}
			if(method.getAnnotation(Handler.class) == null){
				continue;
			}
			if(method.getAnnotation(Handler.class).defaultHandler()){
				// doesn't work when defaultHandler = true
				continue;
			}
			if(!EndpointTool.paramIsEndpointObject(method)){
				continue;
			}
			Class<?> endpointType = method.getParameters()[0].getType();
			endpoints.add((Class<? extends BaseEndpoint<?,?>>)endpointType);
		}
		return endpoints;
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	public static List<Class<? extends BaseLink<?>>> getLinksFromHandler(
			Class<? extends BaseHandler> handler){
		List<Class<? extends BaseLink<?>>> links = new ArrayList<>();
		for(Method method : handler.getDeclaredMethods()){
			if(Modifier.isStatic(method.getModifiers())){
				continue;
			}
			if(method.getAnnotation(Handler.class) == null){
				continue;
			}
			if(method.getAnnotation(Handler.class).defaultHandler()){
				// doesn't work when defaultHandler = true
				continue;
			}
			if(!LinkTool.paramIsLinkObject(method)){
				continue;
			}
			Class<?> linkType = method.getParameters()[0].getType();
			links.add((Class<? extends BaseLink<?>>)linkType);
		}
		return links;
	}

}
