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
package io.datarouter.web.handler.documentation;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;

import com.google.gson.internal.LinkedTreeMap;

import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.instrumentation.doc.ApiDoc;
import io.datarouter.instrumentation.typescript.TsNullable;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTime;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.documentation.ApiDocSchemaDto.ApiDocSchemaDtoBuilder;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;

public class ApiDocSchemaTool{

	public static final Map<Class<?>,String> TYPE_MAPPINGS = Map.ofEntries(
			Map.entry(byte.class, "number"),
			Map.entry(Byte.class, "number"),

			Map.entry(boolean.class, "boolean"),
			Map.entry(Boolean.class, "boolean"),

			Map.entry(Integer.class, "number"),
			Map.entry(int.class, "number"),

			Map.entry(Double.class, "number"),
			Map.entry(double.class, "number"),

			Map.entry(Float.class, "number"),
			Map.entry(float.class, "number"),

			Map.entry(Long.class, "number"),
			Map.entry(long.class, "number"),

			Map.entry(short.class, "number"),
			Map.entry(Short.class, "number"),

			Map.entry(String.class, "string"),
			Map.entry(Date.class, "string"),

			Map.entry(Number.class, "number"),
			Map.entry(Void.class, "void"),

			Map.entry(FileItem.class, "Blob"),

			Map.entry(MilliTime.class, "number"),
			Map.entry(MilliTimeReversed.class, "number"),

			Map.entry(Object.class, "any"));

	public static final List<Class<?>> COLLECTION_CLASSES = List.of(
			List.class,
			Collection.class,
			Map.class,
			LinkedTreeMap.class,
			Set.class);

	public static ApiDocSchemaDto buildSchemaFromDto(Class<?> clazz, JsonAwareHandlerCodec jsonDecoder,
			Map<Class<?>, String> typeOverrides){
		Set<String> seenClassNames = new HashSet<>();
		return ApiDocSchemaDtoBuilder
				.buildObject(clazz.getSimpleName(), getClassNameFromType(clazz),
						getFields(clazz, jsonDecoder, seenClassNames, typeOverrides))
				.build();
	}

	public static ApiDocSchemaDto buildSchemaFromType(Type type, JsonAwareHandlerCodec jsonDecoder,
			Map<Class<?>,String> typeOverrides){
		Set<String> seenClassNames = new HashSet<>();
		return buildSchemaFromType(type, jsonDecoder, seenClassNames, typeOverrides);
	}

	public static ApiDocSchemaDto buildSchemaFromType(Type type, JsonAwareHandlerCodec jsonDecoder,
			Set<String> seenClassNames, Map<Class<?>, String> typeOverrides){
		if(type instanceof Class<?> clazz){
			if(typeOverrides.containsKey(clazz)){
				return ApiDocSchemaDtoBuilder.buildPrimitive(clazz.getSimpleName().toLowerCase(),
								typeOverrides.get(clazz))
						.withIsArray(clazz.isArray())
						.build();
			}else if(TYPE_MAPPINGS.containsKey(clazz)){
				return ApiDocSchemaDtoBuilder.buildPrimitive(clazz.getSimpleName().toLowerCase(),
								TYPE_MAPPINGS.get(clazz))
						.withIsArray(clazz.isArray())
						.build();
			}else if(clazz.isPrimitive()){
				return ApiDocSchemaDtoBuilder.buildPrimitive(clazz.getSimpleName().toLowerCase(),
								clazz.getSimpleName().toLowerCase())
						.withIsArray(clazz.isArray())
						.build();
			}else if(clazz.isEnum()){
				return ApiDocSchemaDtoBuilder.buildEnum(clazz.getSimpleName(), getClassNameFromType(clazz),
								getEnumValues(clazz, jsonDecoder))
						.withIsArray(clazz.isArray())
						.build();
			}
			return ApiDocSchemaDtoBuilder
					.buildObject(clazz.getSimpleName(), getClassNameFromType(clazz), getFields(clazz,
							jsonDecoder, seenClassNames, typeOverrides))
					.withIsArray(clazz.isArray())
					.build();
		}else if(type instanceof ParameterizedType){
			String typeName = ((ParameterizedType)type).getRawType().getTypeName();
			String simpleName = typeName.substring(typeName.lastIndexOf('.') + 1);
			Class<?> clazz = (Class<?>)((ParameterizedType)type).getRawType();
			return ApiDocSchemaDtoBuilder.buildParametrized(
							simpleName,
							getClassNameFromType(((ParameterizedType)type).getRawType()),
							getFields(clazz, jsonDecoder, seenClassNames, typeOverrides),
							Scanner.of(((ParameterizedType)type).getActualTypeArguments())
									.map(innerType -> buildSchemaFromType(innerType, jsonDecoder, seenClassNames,
											typeOverrides))
									.list())
					.withIsArray(clazz.isArray())
					.build();
		}
		return ApiDocSchemaDtoBuilder.buildPrimitive(type.getTypeName(), "any").build();
	}

	public static void buildAllSchemas(ApiDocSchemaDto root, Map<String, ApiDocSchemaDto> schemas){
		Queue<ApiDocSchemaDto> queue = new LinkedList<>();
		Set<String> visited = new HashSet<>();

		queue.add(root);
		visited.add(root.getClassName());

		while(!queue.isEmpty()){
			ApiDocSchemaDto current = queue.poll();
			if(("parameter".equals(current.getType()) || "object".equals(current.getType())) && current.hasFields()){
				schemas.put(current.getClassName(), current);
			}
			if("enum".equals(current.getType())){
				schemas.put(current.getClassName(), current);
			}

			if(current.hasParameters()){
				addToQueue(current.getParameters(), visited, queue);
			}

			if(current.hasFields()){
				addToQueue(current.getFields(), visited, queue);
			}
		}
	}

	private static void addToQueue(List<ApiDocSchemaDto> schemas, Set<String> visited, Queue<ApiDocSchemaDto> queue){
		for(ApiDocSchemaDto schema : schemas){
			if(schema.getClassName() != null && !visited.contains(schema.getClassName())){
				queue.add(schema);
				if(schema.hasFields()){
					visited.add(schema.getClassName());
				}
			}
		}
	}

	private static List<ApiDocSchemaDto> getFields(Class<?> clazz, JsonAwareHandlerCodec jsonDecoder,
			Set<String> seenClassNames, Map<Class<?>, String> typeOverrides){
		// cycle detection
		if(seenClassNames.contains(getClassNameFromType(clazz))){
			return null;
		}
		boolean isCollection = Scanner.of(COLLECTION_CLASSES)
				.anyMatch(collectionClass -> collectionClass.isAssignableFrom(clazz));
		if(isCollection){
			return null;
		}
		seenClassNames.add(getClassNameFromType(clazz));
		return Scanner.of(ReflectionTool.getDeclaredFields(clazz))
				.exclude(field -> Modifier.isStatic(field.getModifiers()))
				.exclude(field -> field.getAnnotation(IgnoredField.class) != null)
				.exclude(field -> field.getType().equals(field.getDeclaringClass()))
				.map(field -> {
					Class<?> fieldType = field.getType();
					if(fieldType.isArray()){
						fieldType = fieldType.getComponentType();
					}
					ApiDocSchemaDtoBuilder builder;

					if(typeOverrides.containsKey(fieldType)){
						builder = ApiDocSchemaDtoBuilder.buildPrimitive(field.getName(),
								typeOverrides.get(fieldType));
					}else if(TYPE_MAPPINGS.containsKey(fieldType)){
						builder = ApiDocSchemaDtoBuilder.buildPrimitive(field.getName(),
								TYPE_MAPPINGS.get(fieldType));
					}else if(fieldType.isPrimitive()){
						builder = ApiDocSchemaDtoBuilder.buildPrimitive(field.getName(),
								fieldType.getSimpleName().toLowerCase());
					}else if(fieldType.isEnum()){
						builder =
								ApiDocSchemaDtoBuilder.buildEnum(field.getName(),
										getClassNameFromType(fieldType),
										getEnumValues(fieldType,
												jsonDecoder));
					}else if(field.getGenericType() instanceof ParameterizedType type){
						builder = ApiDocSchemaDtoBuilder.buildParametrized(field.getName(),
								getClassNameFromType(fieldType),
								getFields(fieldType, jsonDecoder, seenClassNames, typeOverrides),
								Scanner.of(type.getActualTypeArguments())
										.map(innerType -> buildSchemaFromType(innerType, jsonDecoder, seenClassNames,
												typeOverrides))
										.list());
					}else if(field.getGenericType() instanceof GenericArrayType type){
						builder = ApiDocSchemaDtoBuilder.buildPrimitive(field.getName(),
								type.getTypeName().replace("[]", ""));
					}else{
						String className = getClassNameFromType(fieldType);
						builder = ApiDocSchemaDtoBuilder.buildObject(field.getName(), className,
								getFields(fieldType, jsonDecoder, seenClassNames, typeOverrides));
					}
					injectAnnotation(field, builder);
					return builder.withIsArray(field.getType().isArray());
				})
				.map(ApiDocSchemaDtoBuilder::build)
				.list();
	}

	private static String getClassNameFromType(Type type){
		return type.getTypeName().replace("$", ".");
	}

	private static void injectAnnotation(Field field, ApiDocSchemaDtoBuilder builder){
		ApiDoc apiDocAnnotation = field.getAnnotation(ApiDoc.class);
		TsNullable tsNullableAnnotation = field.getAnnotation(TsNullable.class);
		Deprecated deprecatedAnnotation = field.getAnnotation(Deprecated.class);
		if(tsNullableAnnotation != null){
			builder.withIsOptional(true);
		}
		if(deprecatedAnnotation != null){
			builder.withIsDeprecated(true);
		}
		if(apiDocAnnotation == null){
			return;
		}
		if(apiDocAnnotation.isOptional()){
			builder.withIsOptional(true);
		}
		if(apiDocAnnotation.isDeprecated()){
			builder.withIsDeprecated(true);
		}
		if(apiDocAnnotation.max() != Long.MAX_VALUE){
			builder.withMax(apiDocAnnotation.max());
		}
		if(apiDocAnnotation.min() != Long.MIN_VALUE){
			builder.withMin(apiDocAnnotation.min());
		}
		if(apiDocAnnotation.maxLength() != Long.MAX_VALUE){
			builder.withMaxLength(apiDocAnnotation.maxLength());
		}
		if(!apiDocAnnotation.description().isEmpty()){
			builder.withDescription(apiDocAnnotation.description());
		}
		if(!apiDocAnnotation.defaultValue().isEmpty()){
			builder.withDefaultValue(apiDocAnnotation.defaultValue());
		}
	}

	private static List<String> getEnumValues(Class<?> enumType, JsonAwareHandlerCodec jsonDecoder){
		return Scanner.of(enumType.getEnumConstants())
				.map(e -> {
					if(jsonDecoder != null){
						return jsonDecoder.getJsonSerializer().serialize(e);
					}
					return e.toString();
				})
				.sort()
				.list();
	}
}
