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
package io.datarouter.graphql.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation.Options;
import graphql.scalars.ExtendedScalars;
import graphql.scalars.java.JavaPrimitives;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLCodeRegistry.Builder;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.datarouter.graphql.client.util.config.Ql;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.client.util.type.GraphQlId;
import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.client.util.type.GraphQlType;
import io.datarouter.graphql.config.DatarouterGraphQlExecutors.DataFetcherExecutor;
import io.datarouter.graphql.config.DatarouterGraphQlSettingsRoot;
import io.datarouter.graphql.fetcher.AsyncFetcherWrapper;
import io.datarouter.graphql.fetcher.BaseDataLoaderFetcher;
import io.datarouter.graphql.fetcher.DataloaderFetcherWrapper;
import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.graphql.scalar.DatarouterDateScalar;
import io.datarouter.graphql.tool.GraphQlTool;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.Require;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlSchemaService{

	private static final TracingInstrumentation instrumentation = new TracingInstrumentation(
			Options.newOptions().includeTrivialDataFetchers(false));

	private static final Map<Class<?>,GraphQLType> graphQLSimpleTypes = Map.of(
			Integer.class, Scalars.GraphQLInt,
			String.class, Scalars.GraphQLString,
			Boolean.class, Scalars.GraphQLBoolean,
			boolean.class, Scalars.GraphQLBoolean,
			Double.class, Scalars.GraphQLFloat,
			Float.class, Scalars.GraphQLFloat,
			Long.class, JavaPrimitives.GraphQLLong,
			Date.class, DatarouterDateScalar.GraphQlDate,
			LocalDate.class, ExtendedScalars.Date);

	@Inject
	private DatarouterInjector injector;
	@Inject
	private DataFetcherExecutor fetcherExecutor;
	@Inject
	private DatarouterGraphQlSettingsRoot graphQlSettings;

	public GraphQL build(Class<? extends GraphQlBaseHandler> handlerClass){
		GraphQlBaseHandler handler = injector.getInstance(handlerClass);
		GraphQlFetcherRegistry fetcherRegistry = injector.getInstance(handler.fetcherRegistry());
		Map<Class<?>,GraphQLType> classToType = new HashMap<>();
		Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
		Map<String,List<GraphQLFieldDefinition>> typesToFields = buildFieldDefinitionsAndLinkDataFetchers(
				handler.getRootType(),
				codeRegistryBuilder,
				classToType,
				fetcherRegistry);
		GraphQLSchema schema = GraphQLSchema.newSchema()
				.query(buildOutputObjectType(GraphQlRootType.QUERY.getPersistentString(), typesToFields))
				.mutation(buildOutputObjectType(GraphQlRootType.MUTATION.getPersistentString(), typesToFields))
				.codeRegistry(codeRegistryBuilder.build())
				.build();
		return GraphQL.newGraphQL(schema)
				.instrumentation(instrumentation)
				.build();
	}

	private Map<String,List<GraphQLFieldDefinition>> buildFieldDefinitionsAndLinkDataFetchers(
			Class<? extends GraphQlType> clazz,
			Builder codeRegistryBuilder,
			Map<Class<?>,GraphQLType> classToTypeObject,
			GraphQlFetcherRegistry fetcherRegistry){
		Require.isTrue(clazz.getFields().length != 0, clazz.getSimpleName()
				+ " needs to have at least 1 field declared.");
		Map<String,List<GraphQLFieldDefinition>> typeToFields = new HashMap<>();
		for(Field field : clazz.getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				// skip static fields
				continue;
			}
			GraphQLOutputType type = (GraphQLOutputType)buildGraphQlType(field, clazz.getSimpleName(),
					codeRegistryBuilder, classToTypeObject, fetcherRegistry);
			GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
					.name(field.getName())
					.description(GraphQlTool.getDescriptionFromField(field))
					.type(GraphQlTool.fieldIsRequired(field) ? new GraphQLNonNull(type) : type)
					.arguments(buildArguments(field, classToTypeObject, fetcherRegistry))
					.build();
			String typeName = clazz.getSimpleName();
			if(field.isAnnotationPresent(Ql.class)){
				GraphQlRootType rootType = field.getAnnotation(Ql.class).root();
				if(rootType != GraphQlRootType.NONE){
					typeName = rootType.getPersistentString();
				}else{
					typeName = clazz.getSimpleName();
				}
			}
			addFieldToFieldDefinitionMap(typeToFields, typeName, fieldDefinition);
			linkDataFetcher(typeName, field, codeRegistryBuilder, fetcherRegistry);
		}
		return typeToFields;
	}

	private void addFieldToFieldDefinitionMap(
			Map<String,List<GraphQLFieldDefinition>> map,
			String key,
			GraphQLFieldDefinition value){
		List<GraphQLFieldDefinition> fieldDefinitions = map.computeIfAbsent(key, $ -> new ArrayList<>());
		fieldDefinitions.add(value);
	}

	private void linkDataFetcher(
			String parentType,
			Field dataFetcherField,
			Builder codeRegistryBuilder,
			GraphQlFetcherRegistry fetcherRegistry){
		if(dataFetcherField.isAnnotationPresent(Ql.class)){
			String fetcherId = dataFetcherField.getAnnotation(Ql.class).fetcherId();
			GraphQlRootType rootType = dataFetcherField.getAnnotation(Ql.class).root();
			if(rootType != GraphQlRootType.NONE){
				if(fetcherId.isEmpty()){
					throw new IllegalStateException("rootType doesn't specify fetcher for parentType=" + parentType);
				}
			}
			if(!fetcherId.isEmpty()){
				Optional<Class<? extends DatarouterDataFetcher<?,?>>> fetcherClass = fetcherRegistry.find(fetcherId);
				if(fetcherClass.isEmpty()){
					throw new IllegalStateException("must register fetcherId=" + fetcherId + " in registry="
							+ fetcherRegistry.getClass().getSimpleName());
				}
				boolean isUsingLoaders = BaseDataLoaderFetcher.class.isAssignableFrom(fetcherClass.get());
				DataFetcher<?> fetcher = null;
				if(isUsingLoaders){
					fetcher = DataloaderFetcherWrapper.wrap(fetcherClass.get(), injector);
				}else{
					fetcher = AsyncFetcherWrapper.async(fetcherClass.get(), fetcherExecutor, graphQlSettings, injector);
				}
				FieldCoordinates coordinates = FieldCoordinates.coordinates(parentType, dataFetcherField.getName());
				codeRegistryBuilder.dataFetcher(coordinates, fetcher);
			}
		}
	}

	private List<GraphQLArgument> buildArguments(
			Field fieldWithArg,
			Map<Class<?>,GraphQLType> classToTypeObject,
			GraphQlFetcherRegistry fetcherRegistry){
		if(!fieldWithArg.isAnnotationPresent(Ql.class)){
			return new ArrayList<>();
		}
		String fetcherId = fieldWithArg.getAnnotation(Ql.class).fetcherId();
		if(fetcherId.isEmpty()){
			return new ArrayList<>();
		}
		Optional<Class<? extends DatarouterDataFetcher<?,?>>> fetcherClass = fetcherRegistry.find(fetcherId);
		if(fetcherClass.isEmpty()){
			throw new IllegalStateException("must register fetcherId=" + fetcherId + " in registry=" + fetcherRegistry
					.getClass().getSimpleName());
		}
		Class<? extends GraphQlArgumentType> argumentClass = GraphQlTool.getArgumentClassFromFetcherClass(fetcherClass
				.get());
		if(argumentClass.equals(EmptyGraphQlArgumentType.class)){
			return new ArrayList<>();
		}
		return Arrays.stream(argumentClass.getFields())
				.map(field -> {
					GraphQLInputType type = (GraphQLInputType)buildGraphQlType(
							field,
							argumentClass.getSimpleName(),
							null,
							classToTypeObject,
							null);
					return GraphQLArgument.newArgument()
							.name(field.getName())
							.description(GraphQlTool.getDescriptionFromField(field))
							.type(GraphQlTool.fieldIsRequired(field) ? new GraphQLNonNull(type) : type)
							.build();
				})
				.toList();
	}

	private GraphQLObjectType buildOutputObjectType(
			Class<? extends GraphQlType> clazz,
			Builder codeRegistryBuilder,
			Map<Class<?>,GraphQLType> classToTypeObject,
			GraphQlFetcherRegistry fetcherRegistry){
		Map<String,List<GraphQLFieldDefinition>> typeToFields = buildFieldDefinitionsAndLinkDataFetchers(clazz,
				codeRegistryBuilder, classToTypeObject, fetcherRegistry);
		return buildOutputObjectType(clazz.getSimpleName(), typeToFields);
	}

	private GraphQLObjectType buildOutputObjectType(
			String typeName,
			Map<String,List<GraphQLFieldDefinition>> typesToFields){
		List<GraphQLFieldDefinition> fieldDefinitions = typesToFields.get(typeName);
		if(fieldDefinitions == null || fieldDefinitions.isEmpty()){
			return null;
		}
		return GraphQLObjectType.newObject()
				.name(typeName)
				.fields(fieldDefinitions)
				.build();
	}

	private GraphQLInputObjectType buildInputObjectType(
			Class<? extends GraphQlType> clazz,
			Map<Class<?>,GraphQLType> classToTypeObject){
		List<GraphQLInputObjectField> fields = new ArrayList<>();
		for(Field field : clazz.getFields()){
			if(Modifier.isStatic(field.getModifiers())){
				// skip static fields
				continue;
			}
			GraphQLInputType type = (GraphQLInputType)buildGraphQlType(field, clazz.getSimpleName(), null,
					classToTypeObject, null);
			GraphQLInputObjectField objectField = GraphQLInputObjectField.newInputObjectField()
				.name(field.getName())
				.description(GraphQlTool.getDescriptionFromField(field))
				.type(GraphQlTool.fieldIsRequired(field) ? new GraphQLNonNull(type) : type)
				.build();
			fields.add(objectField);
		}
		return GraphQLInputObjectType.newInputObject()
				.name(clazz.getSimpleName())
				.fields(fields)
				.build();
	}

	private GraphQLType buildGraphQlType(
			Field field,
			String parentType,
			Builder codeRegistryBuilder,
			Map<Class<?>,GraphQLType> clazzToObject,
			GraphQlFetcherRegistry fetcherRegistry){
		Class<?> fieldClass = field.getType();
		if(graphQLSimpleTypes.containsKey(fieldClass)){
			return graphQLSimpleTypes.get(fieldClass);
		}else if(List.class.isAssignableFrom(fieldClass)){
			ParameterizedType type = (ParameterizedType)field.getGenericType();
			Class<?> enclosedTypeClass = (Class<?>)type.getActualTypeArguments()[0];
			if(graphQLSimpleTypes.containsKey(enclosedTypeClass)){
				return GraphQLList.list(graphQLSimpleTypes.get(enclosedTypeClass));
			}else{
				return GraphQLList.list(buildGraphQlType(enclosedTypeClass, parentType, codeRegistryBuilder,
						clazzToObject, fetcherRegistry));
			}
		}else if(fieldClass.isAnnotationPresent(GraphQlId.class)){
			return Scalars.GraphQLID;
		}else if(fieldClass.isEnum()){
			return buildGraphQlEnumType(field);
		}else{
			return buildGraphQlType(fieldClass, parentType, codeRegistryBuilder, clazzToObject, fetcherRegistry);
		}
	}

	private GraphQLType buildGraphQlType(
			Class<?> clazz,
			String parentType,
			Builder codeRegistryBuilder,
			Map<Class<?>,GraphQLType> classToTypeObject,
			GraphQlFetcherRegistry fetcherRegistry){
		if(fetcherRegistry == null){ // fetcherRegistry is always null for input args
			if(classToTypeObject.containsKey(clazz)){
				return classToTypeObject.get(clazz);
			}
			@SuppressWarnings("unchecked")
			GraphQLType type = buildInputObjectType((Class<? extends GraphQlType>)clazz, classToTypeObject);
			classToTypeObject.put(clazz, type);
			return type;
		}else if(GraphQlType.class.isAssignableFrom(clazz)){
			if(classToTypeObject.containsKey(clazz)){
				return classToTypeObject.get(clazz);
			}
			@SuppressWarnings("unchecked")
			GraphQLType type = buildOutputObjectType((Class<? extends GraphQlType>)clazz, codeRegistryBuilder,
					classToTypeObject, fetcherRegistry);
			classToTypeObject.put(clazz, type);
			return type;
		}
		throw new UnsupportedOperationException("Class=" + clazz.getSimpleName() + " in " + parentType
				+ " is not handled by graphql schema");
	}

	private GraphQLType buildGraphQlEnumType(Field enumField){
		List<GraphQLEnumValueDefinition> valueDefinitions = new ArrayList<>();
		for(Object obj : enumField.getType().getEnumConstants()){
			valueDefinitions.add(GraphQLEnumValueDefinition.newEnumValueDefinition()
					.name(obj.toString())
					.value(obj)
					.build());
		}
		return GraphQLEnumType.newEnum()
				.name(enumField.getName())
				.description(GraphQlTool.getDescriptionFromField(enumField))
				.values(valueDefinitions)
				.build();
	}

	public static class EmptyGraphQlArgumentType implements GraphQlArgumentType{

		@Override
		public GraphQlArgumentType getSample(){
			return null;
		}

	}

}
