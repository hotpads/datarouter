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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.response.ApiResponseDto;
import io.datarouter.httpclient.response.ApiResponseDtoV2;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.FieldSet;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HandlerDtoTypeTestService{

	private static final int MAX_DEPTH = 7;

	@Inject
	private DatarouterInjector injector;

	public void testHandlerDtoTypes(){
		Scanner.of(injector.getInstancesOfType(BaseHandler.class).values())
				.map(BaseHandler::getClass)
				.concatIter(HandlerTool::getEndpointsFromHandler)
				.include(endpoint -> {
					Type responseType = ((ParameterizedType)endpoint.getGenericSuperclass())
									.getActualTypeArguments()[0];
							boolean requestBodyContainsDatabeanOrFieldSet =
									Scanner.of(ReflectionTool.getDeclaredFieldsIncludingAncestors(endpoint))
											.include(field -> field.getAnnotation(RequestBody.class) != null)
											.map(Field::getGenericType)
											.anyMatch(type -> typeIncludeDatabeansOrFieldSet(type, 0));
							return typeIncludeDatabeansOrFieldSet(responseType, 0)
									|| requestBodyContainsDatabeanOrFieldSet;
						})
				.map(Class::getSimpleName)
				.flush(invalidMethods -> {
					if(!invalidMethods.isEmpty()){
						throw new RuntimeException("Endpoints with Databeans/PKs in their request/response DTOs: "
								+ invalidMethods);
					}
				});
	}

	protected static boolean typeIncludeDatabeansOrFieldSet(Type type, int depth){
		// short circuit for classes which have themselves as a field or a cyclical field loop a -> b -> a
		if(depth > MAX_DEPTH){
			return false;
		}
		int newDepth = depth + 1;
		if(type instanceof Class<?> clazz){
			return classContainsDatabeanOrFieldSet(clazz, newDepth);
		}else if(type instanceof ParameterizedType parameterizedType){
			return classContainsDatabeanOrFieldSet((Class<?>) parameterizedType.getRawType(), newDepth)
					|| Scanner.of(parameterizedType.getActualTypeArguments())
					.anyMatch(typeArgument -> typeIncludeDatabeansOrFieldSet(typeArgument, newDepth));
		}
		return false;
	}

	protected static boolean classContainsDatabeanOrFieldSet(Class<?> clazz, int depth){
		if(clazz == null
				|| clazz.isPrimitive()
				|| clazz.isEnum()
				// catches Object, Void, Constructor, String, boxed types, etc
				|| clazz.getCanonicalName().contains("java.")
				|| clazz.isAssignableFrom(Collection.class)
				// Simplify common types
				|| clazz == ApiResponseDto.class
				|| clazz == ApiResponseDtoV2.class){
			return false;
		}
		if(clazz.isArray()){
			clazz = clazz.getComponentType();
		}
		if(Databean.class.isAssignableFrom(clazz) || FieldSet.class.isAssignableFrom(clazz)){
			return true;
		}

		return Scanner.of(ReflectionTool.getDeclaredFieldsIncludingAncestors(clazz))
				.exclude(field -> Modifier.isStatic(field.getModifiers()))
				.exclude(field -> field.getAnnotation(IgnoredField.class) != null)
				// short circuit for classes which have themselves as a field
				.exclude(field -> field.getType().equals(field.getDeclaringClass()))
				.map(Field::getGenericType)
				.anyMatch(type -> typeIncludeDatabeansOrFieldSet(type, depth));
	}

}
