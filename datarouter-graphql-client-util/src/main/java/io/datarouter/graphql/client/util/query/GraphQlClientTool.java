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
package io.datarouter.graphql.client.util.query;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.client.util.type.GraphQlSerializedName;
import io.datarouter.graphql.client.util.type.GraphQlType;
import io.datarouter.graphql.client.util.type.QlNullableArgumentString;
import io.datarouter.graphql.client.util.type.QlNullableArgumentType;

public class GraphQlClientTool{

	private static final Set<Class<?>> GRAPHQL_SIMPLE_TYPES = Set.of(
			Integer.class,
			String.class,
			Boolean.class,
			Double.class,
			Long.class,
			Float.class);

	public static String buildGraphQlQuery(
			GraphQlParentFieldAndArg parent,
			Map<String,Optional<GraphQlArgumentType>> queriesToArgs,
			List<GraphQlClientQueryBuilder> queryBuilders){
		StringBuilder sb = new StringBuilder();
		sb.append(buildFieldWithFieldArg(parent.fieldName(), parent.fieldArg()));
		sb.append("{");
		sb.append("\n");
		for(Entry<String,Optional<GraphQlArgumentType>> entry : queriesToArgs.entrySet()){
			sb.append(buildFieldWithFieldArg(entry.getKey(), entry.getValue()));
			sb.append("\n");
		}
		for(GraphQlClientQueryBuilder queryBuilder : queryBuilders){
			sb.append(queryBuilder.build());
		}
		sb.append("}");
		sb.append("\n");
		return sb.toString();
	}

	public static Set<String> createSetOfGraphQlTypeFields(Class<? extends GraphQlType> graphQlType){
		Set<String> fieldNames = new HashSet<>();
		for(Field field : graphQlType.getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			GraphQlSerializedName serializedName = field.getAnnotation(GraphQlSerializedName.class);
			if(serializedName != null){
				fieldNames.add(serializedName.value());
				continue;
			}
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	public static String buildArgumentString(GraphQlArgumentType argumentType){
		return "(" + buildArgumentStringForObject(argumentType) + ")";
	}

	private static String buildArgumentStringForObject(Object argumentType){
		List<String> allArguments = new ArrayList<>();
		for(Field field : argumentType.getClass().getFields()){
			try{
				Object value = field.get(argumentType);
				if(value != null && !Modifier.isStatic(field.getModifiers())){
					GraphQlSerializedName serializedName = field.getAnnotation(GraphQlSerializedName.class);
					String fieldName = serializedName != null ? serializedName.value() : field.getName();
					String argumentStr = fieldName + ":";
					if(GraphQlType.class.isAssignableFrom(field.getType())){
						argumentStr += "{" + buildArgumentStringForObject(value) + "}";
					}else if(List.class.isAssignableFrom(field.getType())){
						argumentStr += buildArgumentStringForList((List<?>)value);
					}else if(String.class.isAssignableFrom(field.getType())){
						argumentStr += "\"" + value.toString() + "\"";
					}else if(QlNullableArgumentType.class.isAssignableFrom(field.getType())){
						argumentStr += buildArgumentForNullableArgumentType((QlNullableArgumentType<?>)value);
					}else if(GRAPHQL_SIMPLE_TYPES.contains(field.getType())){
						argumentStr += value.toString();
					}else{
						throw new RuntimeException("unknownType=" + field.getType());
					}
					allArguments.add(argumentStr);
				}
			}catch(IllegalArgumentException | IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
		return String.join(",", allArguments);
	}

	public static String buildArgumentStringForList(List<?> inputList){
		List<String> strings = new ArrayList<>();
		for(Object item : inputList){
			if(String.class.isAssignableFrom(item.getClass())){
				strings.add("\"" + item.toString() + "\"");
			}else if(GRAPHQL_SIMPLE_TYPES.contains(item.getClass())){
				strings.add(item.toString());
			}else{
				strings.add("{" + buildArgumentStringForObject(item) + "}");
			}
		}
		return "[" + String.join(",", strings) + "]";
	}

	private static String buildFieldWithFieldArg(String fieldName, Optional<GraphQlArgumentType> fieldArgOpt){
		return fieldArgOpt.map(
						fieldArg -> fieldName + GraphQlClientTool.buildArgumentString(fieldArg))
				.orElse(fieldName);
	}

	private static String buildArgumentForNullableArgumentType(QlNullableArgumentType<?> argumentType){
		if(argumentType.toString() == null){
			return null;
		}
		if(QlNullableArgumentString.class.isAssignableFrom(argumentType.getClass())){
			return "\"" + argumentType + "\"";
		}
		return argumentType.toString();
	}

	public record GraphQlParentFieldAndArg(
			String fieldName,
			Optional<GraphQlArgumentType> fieldArg){
	}

}
