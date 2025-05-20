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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.JavaEndpointTool;
import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.endpoint.link.NoOpResponseType;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.api.mobile.BaseMobileEndpoint;
import io.datarouter.web.api.web.BaseWebApi;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.BaseHandler.NoOpHandlerDecoder;
import io.datarouter.web.handler.BaseHandler.NoOpHandlerEncoder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.HandlerDecoder;

public class HandlerTool{

	public static Optional<?> getParameterValue(Object parameterValue){
		if(parameterValue instanceof Optional){
			return (Optional<?>) parameterValue;
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
				if(EndpointTool.paramIsJavaEndpointObject(method)){
					Class<?> endpointType = method.getParameters()[0].getType();
					@SuppressWarnings("unchecked")
					BaseJavaEndpoint<?,?> javaEndpoint = ReflectionTool.createWithoutNoArgs(
							(Class<? extends BaseJavaEndpoint<?,?>>)endpointType);

					validateOptionalFields(javaEndpoint.getClass());

					String endpointPath = Scanner.of(javaEndpoint.pathNode.toSlashedString().split("/"))
							.findLast()
							.get();
					assertNonMismatch(methodName, endpointPath, handlerClass, javaEndpoint.getClass(), path);

					Type methodReturnType = method.getGenericReturnType();
					boolean isPrimitive = method.getReturnType().isPrimitive();
					if(isPrimitive){
						methodReturnType = ClassTool.getBoxedFromPrimitive(methodReturnType);
					}
					Type endpointResponseType = JavaEndpointTool.getResponseType(javaEndpoint);
					if(endpointResponseType.equals(NoOpResponseType.class)){
						return;
					}
					validateUnknownTypes(endpointResponseType, methodReturnType, handlerClass, javaEndpoint.getClass());
					validateReturnTypes(endpointResponseType, methodReturnType, handlerClass, javaEndpoint.getClass());
					return;
				}

				if(EndpointTool.paramIsWebApiObject(method)){
					Class<?> endpointType = method.getParameters()[0].getType();
					@SuppressWarnings("unchecked")
					BaseWebApi<?,?> webApi = ReflectionTool.createWithoutNoArgs(
							(Class<? extends BaseWebApi<?,?>>)endpointType);

					validateOptionalFields(webApi.getClass());

					String webApiPath = Scanner.of(webApi.pathNode.toSlashedString().split("/"))
							.findLast()
							.get();
					assertNonMismatch(methodName, webApiPath, handlerClass, webApi.getClass(), path);
					Type methodReturnType = method.getGenericReturnType();
					boolean isPrimitive = method.getReturnType().isPrimitive();
					if(isPrimitive){
						methodReturnType = ClassTool.getBoxedFromPrimitive(methodReturnType);
					}
					Type apiResponseType = JavaEndpointTool.getResponseType(webApi.getClass());
					if(apiResponseType.equals(NoOpResponseType.class)){
						return;
					}
					validateUnknownTypes(apiResponseType, methodReturnType, handlerClass, webApi.getClass());
					validateReturnTypes(apiResponseType, methodReturnType, handlerClass, webApi.getClass());
					return;
				}

				if(EndpointTool.paramIsMobileEndpointObject(method)){
					Class<?> endpointType = method.getParameters()[0].getType();
					@SuppressWarnings("unchecked")
					BaseMobileEndpoint<?,?> mobileEndpoint = ReflectionTool.createWithoutNoArgs(
							(Class<? extends BaseMobileEndpoint<?,?>>)endpointType);

					validateOptionalFields(mobileEndpoint.getClass());

					String mobileApiPath = Scanner.of(mobileEndpoint.pathNode.toSlashedString().split("/"))
							.findLast()
							.get();
					assertNonMismatch(methodName, mobileApiPath, handlerClass, mobileEndpoint.getClass(), path);
					Type methodReturnType = method.getGenericReturnType();
					boolean isPrimitive = method.getReturnType().isPrimitive();
					if(isPrimitive){
						methodReturnType = ClassTool.getBoxedFromPrimitive(methodReturnType);
					}
					Type endpointResponseType = JavaEndpointTool.getResponseType(mobileEndpoint.getClass());
					if(endpointResponseType.equals(NoOpResponseType.class)){
						return;
					}
					validateUnknownTypes(endpointResponseType, methodReturnType, handlerClass,
							mobileEndpoint.getClass());
					validateReturnTypes(endpointResponseType, methodReturnType, handlerClass,
							mobileEndpoint.getClass());
					return;
				}
				return;
			}
		}
		throw new IllegalArgumentException(handlerClass.getSimpleName() + " does not have a method that matches "
				+ path);
	}

	private static void validateUnknownTypes(
			Type endpointResponseType,
			Type methodReturnType,
			Class<? extends BaseHandler> handlerClass,
			Class<?> endpointClass){
		if(endpointResponseType.toString().contains("?") || methodReturnType.toString().contains("?")){
			throw new IllegalArgumentException(String.format(
					"Unknown types are forbidden. Explicitly declare a return type  handler=%s endpoint=%s "
							+ "handlerReturnType=%s "
							+ "endpointResponseType=%s",
					handlerClass.getSimpleName(),
					endpointClass.getSimpleName(),
					methodReturnType.getTypeName(),
					endpointResponseType.getTypeName()));
		}
	}

	private static void assertNonMismatch(
			String methodName,
			String apiPath,
			Class<? extends BaseHandler> handlerClass,
			Class<?> endpointClass,
			String path){
		if(!methodName.equals(apiPath)){
			throw new IllegalArgumentException(String.format(
					"Handler Mismatch. handler=%s endpoint=%s, path=%s methodName=%s "
							+ "dispatchRulePath=%s",
					handlerClass.getSimpleName(),
					endpointClass.getSimpleName(),
					apiPath,
					methodName,
					path));
		}
	}

	private static void validateReturnTypes(
			Type endpointResponseType,
			Type methodReturnType,
			Class<? extends BaseHandler> handlerClass,
			Class<?> responseTypeWrapperClass){
		if(!methodReturnType.equals(endpointResponseType)){
			throw new IllegalArgumentException(String.format(
					"Handler Response Type Mismatch. handler=%s endpoint=%s handlerReturnType=%s "
							+ "endpointResponseType=%s",
					handlerClass.getSimpleName(),
					responseTypeWrapperClass.getSimpleName(),
					methodReturnType.getTypeName(),
					endpointResponseType.getTypeName()));
		}
	}

	/**
	 * Validate optional fields are not final and initialized to Optional.empty()
	 */
	protected static void validateOptionalFields(Class<?> clazz){
		for(Field field : clazz.getFields()){
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
			if(!isOptional){
				continue;
			}
			if(Modifier.isFinal(field.getModifiers())){
				throw new RuntimeException(String.format(
						"%s: Optional fields cannot be final. '%s' needs to be initialized to "
						+ "Optional.empty() out side of the constructor.",
						clazz.getSimpleName(), field.getName()));
			}
		}
	}

	public static List<Class<? extends BaseEndpoint>> getEndpointsFromHandler(
			Class<? extends BaseHandler> handler){
		List<Class<? extends BaseEndpoint>> endpoints = new ArrayList<>();
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
			if(!EndpointTool.paramIsBaseEndpointObject(method)){
				continue;
			}
			Class<?> endpointType = method.getParameters()[0].getType();
			endpoints.add((Class<? extends BaseEndpoint>)endpointType);
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

	public static Class<? extends HandlerDecoder> getHandlerDecoderClass(
			Handler handlerAnnotation,
			DispatchRule rule){
		Class<? extends HandlerDecoder> decoderClass = handlerAnnotation.decoder();
		if(decoderClass.equals(NoOpHandlerDecoder.class)){
			decoderClass = rule.getDefaultHandlerDecoder();
		}
		return decoderClass;
	}

	public static Class<? extends HandlerEncoder> getHandlerEncoderClass(
			Handler handlerAnnotation,
			DispatchRule rule){
		Class<? extends HandlerEncoder> encoderClass = handlerAnnotation.encoder();
		if(encoderClass.equals(NoOpHandlerEncoder.class)){
			encoderClass = rule.getDefaultHandlerEncoder();
		}
		return encoderClass;
	}

	public static List<Method> getHandlerAnnotatedMethods(Class<? extends BaseHandler> handler){
		return Scanner.of(handler.getDeclaredMethods())
				.include(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.list();
	}

	public static void validateHandlerMethodDeprecationAnnotation(Method handlerMethod){
		Handler handlerAnnotation = handlerMethod.getAnnotation(Handler.class);
		if(handlerAnnotation != null && !handlerAnnotation.deprecatedOn().isEmpty()){
			try{
				parseHandlerDeprecatedOnDate(handlerAnnotation.deprecatedOn());
			}catch(DateTimeException | ArithmeticException e){
				throw new IllegalArgumentException("Invalid date [" + handlerAnnotation.deprecatedOn()
						+ "] for @Handler annotation on " + handlerMethod.getDeclaringClass().getSimpleName() + "."
						+ handlerMethod.getName() + "(): Must be of format YYYY-MM-DD.", e);
			}
		}
	}

	public static Instant parseHandlerDeprecatedOnDate(String deprecatedOn){
		return LocalDate.parse(deprecatedOn)
				.atStartOfDay(ZoneIds.UTC)
				.toInstant();
	}

}
