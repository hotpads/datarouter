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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.datarouter.gson.GsonTool;
import io.datarouter.httpclient.DocumentedGenericHolder;
import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.Endpoint;
import io.datarouter.httpclient.endpoint.JavaEndpointTool;
import io.datarouter.httpclient.endpoint.param.EndpointParam;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.ExampleProvider;
import io.datarouter.types.ExampleProviderTool;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;
import io.datarouter.util.lang.MethodParameterExtractionTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.DispatchType;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerTool;
import io.datarouter.web.handler.documentation.DocumentedExampleDto.DocumentedExampleEnumDto;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;
import io.datarouter.web.handler.types.HandlerDecoder;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.ParamDefaultEnum;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ApiDocService{
	private static final Logger logger = LoggerFactory.getLogger(ApiDocService.class);

	private static final Set<String> HIDDEN_SPEC_PARAMS = Set.of(
			SecurityParameters.CSRF_IV,
			SecurityParameters.CSRF_TOKEN,
			SecurityParameters.SIGNATURE);
	private static final String UNDEFINED_REQUEST_TYPE = "unknown";

	@Inject
	private DatarouterInjector injector;
	@Inject
	private ApiDocTypeOverrides apiDocOverrides;

	public Map<String,List<DocumentedEndpointJspDto>> buildDocumentation(String apiUrlContext,
			List<RouteSet> routeSets){
		Map<DispatchType,List<DispatchRule>> dispatchRulesByDispatchType = Scanner.of(routeSets)
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getPattern().pattern().startsWith(apiUrlContext))
				.groupBy(DispatchRule::getDispatchType);
		Map<String,List<DocumentedEndpointJspDto>> endpointJspDtosByDispatchType = new TreeMap<>();
		dispatchRulesByDispatchType.forEach((dispatchType, rules) -> {
			var endpointJspDtos = Scanner.of(rules)
					.concatIter(this::buildEndpointDocumentation)
					.sort(Comparator.comparing(DocumentedEndpointJspDto::getUrl))
					.list();
			if(dispatchType.equals(DispatchType.DEFAULT)){
				endpointJspDtosByDispatchType.put("Other", endpointJspDtos);
			}else{
				endpointJspDtosByDispatchType.put(dispatchType.value, endpointJspDtos);
			}

		});
		return endpointJspDtosByDispatchType;
	}

	private List<DocumentedEndpointJspDto> buildEndpointDocumentation(DispatchRule rule){
		List<DocumentedEndpointJspDto> endpoints = new ArrayList<>();
		Class<? extends BaseHandler> handler = rule.getHandlerClass();
		while(handler != null && !handler.getName().equals(BaseHandler.class.getName())){
			for(Method method : handler.getDeclaredMethods()){
				if(!method.isAnnotationPresent(Handler.class)){
					continue;
				}
				Handler handlerAnnotation = method.getAnnotation(Handler.class);
				String url = rule.getPattern().pattern();
				if(!url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)
						&& !url.substring(url.lastIndexOf('/') + 1).equals(method.getName())
						&& !handlerAnnotation.defaultHandler()){
					continue;
				}
				if(url.contains(BaseRouteSet.REGEX_ONE_DIRECTORY)){
					String urlSuffix = handlerAnnotation.defaultHandler() ? "" : "/" + method.getName();
					url = url.replace(BaseRouteSet.REGEX_ONE_DIRECTORY, "") + urlSuffix;
				}
				String implementation = handler.getSimpleName();
				List<DocumentedParameterJspDto> parameters = new ArrayList<>();
				String description = handlerAnnotation.description();
				if(description.isEmpty()){
					if(EndpointTool.paramIsBaseEndpointObject(method)){
						@SuppressWarnings("unchecked")
						Class<BaseEndpoint> baseEndpoint = (Class<BaseEndpoint>)method.getParameters()[0]
								.getType();
						Endpoint annotation = baseEndpoint.getAnnotation(Endpoint.class);
						if(annotation != null){
							description = annotation.description();
						}
					}
				}
				Class<? extends HandlerDecoder> decoderClass = HandlerTool.getHandlerDecoderClass(
						handlerAnnotation,
						rule);
				JsonAwareHandlerCodec jsonDecoder = null;
				if(JsonAwareHandlerCodec.class.isAssignableFrom(decoderClass)){
					jsonDecoder = (JsonAwareHandlerCodec)injector.getInstance(decoderClass);
				}
				DocumentedSecurityDetails securityDetails = createApplicableSecurityParameters(rule, jsonDecoder);
				parameters.addAll(securityDetails.parameters);
				parameters.addAll(createMethodParameters(method, jsonDecoder));

				Type responseType = method.getGenericReturnType();
				String responseExample;
				Set<DocumentedExampleEnumDto> responseExampleEnumDtos = new HashSet<>();
				if(responseType == Void.TYPE){
					responseExample = null;
				}else{
					try{
						DocumentedExampleDto responseObject = createBestExample(
								jsonDecoder,
								responseType,
								new HashSet<>());
						responseExampleEnumDtos = responseObject.exampleEnumDtos();

						Class<? extends HandlerEncoder> encoderClass = HandlerTool.getHandlerEncoderClass(
								handlerAnnotation,
								rule);
						if(JsonAwareHandlerCodec.class.isAssignableFrom(encoderClass)){
							JsonAwareHandlerCodec encoder = (JsonAwareHandlerCodec)injector.getInstance(encoderClass);
							responseExample = GsonTool.prettyPrint(encoder.getJsonSerializer().serialize(
									responseObject.exampleObject()));
						}else{
							responseExample = "Not a JSON endpoint";
						}
					}catch(Exception e){
						responseExample = "Impossible to render";
						logger.warn("Could not create response example for {}", responseType, e);
					}
				}
				String responseTypeString = buildTypeString(responseType, jsonDecoder, apiDocOverrides.getOverrides());
				var response = new DocumentedResponseJspDto(
						responseTypeString,
						responseExample,
						buildEnumValuesString(responseExampleEnumDtos),
						ApiDocSchemaTool.buildSchemaFromType(responseType, jsonDecoder,
								apiDocOverrides.getOverrides()));
				boolean isDeprecated = method.isAnnotationPresent(Deprecated.class)
						|| handler.isAnnotationPresent(Deprecated.class);
				String requestType = getRequestType(method);
				List<DocumentedErrorJspDto> errors = buildError(method);
				Set<DocumentedExampleEnumDto> requestParamExampleEnumDtos = Scanner.of(parameters)
						.concatIter(parameter -> parameter.exampleEnumDtos)
						.collect(Collectors.toSet());
				var endpoint = new DocumentedEndpointJspDto(
						url,
						implementation,
						parameters,
						securityDetails.apiKeyFieldName,
						description,
						response,
						isDeprecated,
						handlerAnnotation.deprecatedOn(),
						handlerAnnotation.deprecationLink(),
						errors,
						buildEnumValuesString(requestParamExampleEnumDtos),
						requestType,
						getEndpointPath(handlerAnnotation.newWebEndpoint()),
						getEndpointPath(handlerAnnotation.newMobileEndpoint()),
						handlerAnnotation.newServiceHref());
				endpoints.add(endpoint);
			}
			handler = handler.getSuperclass().asSubclass(BaseHandler.class);
		}
		return endpoints;
	}

	@SuppressWarnings("checkstyle:EmptyBlock")
	private String getEndpointPath(Class<? extends BaseEndpoint> endpoint){
		// default annotation value
		if(endpoint.equals(BaseEndpoint.class)){
			return "";
		}
		Constructor<?>[] constructors = endpoint.getDeclaredConstructors();
		for(Constructor<?> ctor : constructors){
			int paramCount = ctor.getParameterCount();
			Object[] args = new Object[paramCount];
			Arrays.fill(args, null); // fill all arguments with null
			try{
				BaseEndpoint instance = (BaseEndpoint)ctor.newInstance(args);
				return instance.pathNode.toSlashedString();
			}catch(Exception _){
			}
		}
		return "";
	}

	private String getRequestType(Method method){
		if(EndpointTool.paramIsBaseEndpointObject(method)){
			return getRequestTypeFromEndpointObject(method.getParameters()[0].getType());
		}
		return UNDEFINED_REQUEST_TYPE;
	}

	private String getRequestTypeFromEndpointObject(Class<?> endpointType){
		@SuppressWarnings("unchecked")
		BaseEndpoint baseEndpoint = ReflectionTool.createWithoutNoArgs(
				(Class<? extends BaseEndpoint>)endpointType);
		return baseEndpoint.method.persistentString;
	}

	private List<DocumentedErrorJspDto> buildError(Method method){
		return Scanner.of(method.getExceptionTypes())
				.concatOpt(HttpDocumentedExceptionTool::findDocumentation)
				.map(exception -> new DocumentedErrorJspDto(exception.getStatusCode(), exception.getErrorMessage()))
				.list();
	}

	private static String buildTypeString(Type type, JsonAwareHandlerCodec jsonDecoder,
			Map<Class<?>, String> typeOverrides){
		return ApiDocSchemaTool.buildSchemaFromType(type, jsonDecoder, typeOverrides).toFieldString();
	}

	private List<DocumentedParameterJspDto> createMethodParameters(Method method, JsonAwareHandlerCodec jsonDecoder){
		Parameter[] parameters = method.getParameters();
//		boolean isJavaEndpointObject = EndpointTool.paramIsJavaEndpointObject(method);
//		if(isJavaEndpointObject){
//			@SuppressWarnings("unchecked")
//			BaseJavaEndpoint<?,?> baseJavaEndpoint = ReflectionTool.createWithoutNoArgs(
//					(Class<? extends BaseJavaEndpoint<?,?>>)method.getParameters()[0].getType());
//			return Scanner.of(baseJavaEndpoint.getClass().getFields())
//					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
//					.map(parameter -> createDocumentedParameterFromField(parameter, jsonDecoder))
//					.list();
//		}
//
//		boolean isWebApiObject = EndpointTool.paramIsWebApiObject(method);
//		if(isWebApiObject){
//			@SuppressWarnings("unchecked")
//			BaseWebApi<?,?> baseWebApi = ReflectionTool.createWithoutNoArgs(
//					(Class<? extends BaseWebApi<?,?>>)method.getParameters()[0].getType());
//			return Scanner.of(baseWebApi.getClass().getFields())
//					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
//					.map(parameter -> createDocumentedParameterFromField(parameter, jsonDecoder))
//					.list();
//		}
//
//		boolean isMobileEndpointObject = EndpointTool.paramIsMobileEndpointObject(method);
//		if(isMobileEndpointObject){
//			@SuppressWarnings("unchecked")
//			BaseMobileEndpoint<?,?> baseMobileEndpoint = ReflectionTool.createWithoutNoArgs(
//					(Class<? extends BaseMobileEndpoint<?,?>>)method.getParameters()[0].getType());
//			return Scanner.of(baseMobileEndpoint.getClass().getFields())
//					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
//					.map(parameter -> createDocumentedParameterFromField(parameter, jsonDecoder))
//					.list();
//		}
//
//		boolean isExternalEndpointObject = EndpointTool.paramIsExternalEndpointObject(method);
//		if(isExternalEndpointObject){
//			@SuppressWarnings("unchecked")
//			BaseExternalEndpoint<?,?> baseExternalEndpoint = ReflectionTool.createWithoutNoArgs(
//					(Class<? extends BaseExternalEndpoint<?,?>>)method.getParameters()[0].getType());
//			return Scanner.of(baseExternalEndpoint.getClass().getFields())
//					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
//					.map(parameter -> createDocumentedParameterFromField(parameter, jsonDecoder))
//					.list();
//		}

		boolean isBaseEndpointObject = EndpointTool.paramIsBaseEndpointObject(method);
		if(isBaseEndpointObject){
			@SuppressWarnings("unchecked")
			BaseEndpoint baseEndpoint = ReflectionTool.createWithoutNoArgs(
					(Class<? extends BaseEndpoint>)method.getParameters()[0].getType());
			return Scanner.of(baseEndpoint.getClass().getFields())
					.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
					.map(parameter -> createDocumentedParameterFromField(parameter, jsonDecoder))
					.list();
		}

		return Scanner.of(parameters)
				.map(parameter -> createDocumentedParameterJspDto(parameter, jsonDecoder))
				.list();
	}

	private DocumentedParameterJspDto createDocumentedParameterJspDto(
			Parameter parameter,
			JsonAwareHandlerCodec jsonDecoder){
		String description = null;
		String name = parameter.getName();
		Param param = parameter.getAnnotation(Param.class);
		Set<DocumentedExampleEnumDto> exampleEnumDtos = new HashSet<>();
		if(param != null){
			description = param.description();
			if(!param.value().isEmpty()){
				name = param.value();
			}
			if(!param.enumClass().equals(ParamDefaultEnum.class)){
				String enumValuesDisplay = Scanner.of(param.enumClass().getEnumConstants())
						.map(jsonDecoder.getJsonSerializer()::serialize)
						.sort()
						.collect(Collectors.joining(","));
				exampleEnumDtos.add(new DocumentedExampleEnumDto(param.enumClass().getSimpleName(), enumValuesDisplay));
			}
		}
		boolean isDeprecated = parameter.isAnnotationPresent(Deprecated.class);
		return createDocumentedParameter(
				name,
				parameter,
				description,
				exampleEnumDtos,
				jsonDecoder,
				isDeprecated);
	}

	private DocumentedSecurityDetails createApplicableSecurityParameters(
			DispatchRule rule, JsonAwareHandlerCodec jsonDecoder){
		List<String> applicableSecurityParameterNames = new ArrayList<>();
		if(rule.hasSignature()){
			applicableSecurityParameterNames.add(SecurityParameters.SIGNATURE);
		}
		String apiKeyFieldName = null;
		if(rule.hasApiKey()){
			apiKeyFieldName = rule.getApiKeyPredicates().getFirst().getApiKeyFieldName();
			applicableSecurityParameterNames.add(apiKeyFieldName);
		}
		if(rule.hasCsrfToken()){
			applicableSecurityParameterNames.add(SecurityParameters.CSRF_TOKEN);
			applicableSecurityParameterNames.add(SecurityParameters.CSRF_IV);
		}
		List<DocumentedParameterJspDto> parameters = Scanner.of(applicableSecurityParameterNames)
				// TODO double check the params here
				.map(parameterName -> new DocumentedParameterJspDto(
						parameterName,
						buildTypeString(String.class, jsonDecoder, apiDocOverrides.getOverrides()),
						null,
						true,
						false,
						HIDDEN_SPEC_PARAMS.contains(parameterName),
						null,
						false,
						new HashSet<>(),
						null))
				.list();
		return new DocumentedSecurityDetails(parameters, apiKeyFieldName);
	}

	private DocumentedParameterJspDto createDocumentedParameter(
			String paramName,
			Parameter parameter,
			String description,
			Set<DocumentedExampleEnumDto> exampleEnumDtos,
			JsonAwareHandlerCodec jsonDecoder,
			boolean isDeprecated){
		Type type;
		boolean isRequired;

		if(Optional.class.isAssignableFrom(parameter.getType())){
			isRequired = false;
			type = MethodParameterExtractionTool.extractParameterizedTypeFromOptionalParameter(parameter);
		}else{
			isRequired = true;
			type = parameter.getParameterizedType();
		}

		Optional<Class<?>> clazz = type instanceof Class
				? Optional.of((Class<?>)type)
				: Optional.empty();
		String example = null;
		if(includeType(clazz)){
			try{
				DocumentedExampleDto exampleDto = createBestExample(jsonDecoder, type, new HashSet<>());
				exampleEnumDtos.addAll(exampleDto.exampleEnumDtos());
				example = GsonTool.prettyPrint(jsonDecoder.getJsonSerializer().serialize(exampleDto.exampleObject()));
			}catch(Exception e){
				logger.warn("Could not create parameter example {} for {}", type, paramName, e);
			}
		}

		boolean isRequestBody = parameter.isAnnotationPresent(RequestBody.class);
		return new DocumentedParameterJspDto(
				paramName,
				buildTypeString(type, jsonDecoder, apiDocOverrides.getOverrides()),
				example,
				isRequired,
				isRequestBody,
				HIDDEN_SPEC_PARAMS.contains(paramName),
				description,
				isDeprecated,
				exampleEnumDtos,
				isRequestBody ? ApiDocSchemaTool.buildSchemaFromType(type, jsonDecoder,
						apiDocOverrides.getOverrides()) : null);
	}

	private DocumentedParameterJspDto createDocumentedParameterFromField(
			Field field,
			JsonAwareHandlerCodec jsonDecoder){
		boolean isOptional = field.getType().isAssignableFrom(Optional.class);
		Type type;
		String example = null;
		Set<DocumentedExampleEnumDto> exampleEnumDtos = new HashSet<>();
		if(isOptional){
			Type parameterizedType = JavaEndpointTool.extractParameterizedType(field);
			type = parameterizedType;
			if(includeType(Optional.of(parameterizedType.getClass()))){
				try{
					DocumentedExampleDto exampleDto = createBestExample(
							jsonDecoder,
							parameterizedType,
							new HashSet<>());
					exampleEnumDtos = exampleDto.exampleEnumDtos();
					example = GsonTool.prettyPrint(jsonDecoder.getJsonSerializer().serialize(
							exampleDto.exampleObject()));
				}catch(Exception e){
					logger.warn("Could not create parameter example {} for {}", field.getType(), field.getName(), e);
				}
			}
		}else if(field.getGenericType() instanceof ParameterizedType parameterizedType){
			type = parameterizedType;
			try{
				DocumentedExampleDto exampleDto = handleParameterizedTypes(
						jsonDecoder,
						parameterizedType,
						new HashSet<>(),
						new HashSet<>());
				exampleEnumDtos = exampleDto.exampleEnumDtos();
				example = GsonTool.prettyPrint(jsonDecoder.getJsonSerializer().serialize(exampleDto.exampleObject()));
			}catch(Exception e){
				logger.warn("Could not create parameter example {} for {}", field.getType(), field.getName(), e);
			}
		}else{
			type = field.getType();
			if(includeType(Optional.of(field.getType()))){
				try{
					DocumentedExampleDto exampleDto = createBestExample(jsonDecoder, field.getType(), new HashSet<>());
					exampleEnumDtos = exampleDto.exampleEnumDtos();
					example = GsonTool.prettyPrint(jsonDecoder.getJsonSerializer().serialize(
							exampleDto.exampleObject()));
				}catch(Exception e){
					logger.warn("Could not create parameter example {} for {}", field.getType(), field.getName(), e);
				}
			}
		}
		boolean isDeprecated = field.isAnnotationPresent(Deprecated.class);
		boolean isRequestBody = field.isAnnotationPresent(RequestBody.class);
		return new DocumentedParameterJspDto(
				JavaEndpointTool.getFieldName(field),
				buildTypeString(type, jsonDecoder, apiDocOverrides.getOverrides()),
				example,
				!isOptional,
				isRequestBody,
				HIDDEN_SPEC_PARAMS.contains(field.getName()),
				Optional.ofNullable(field.getAnnotation(EndpointParam.class))
						.map(EndpointParam::description)
						.orElse(null),
				isDeprecated,
				exampleEnumDtos,
				isRequestBody
						? ApiDocSchemaTool.buildSchemaFromType(field.getGenericType(), jsonDecoder,
						apiDocOverrides.getOverrides())
						: null);
	}

	private boolean includeType(Optional<Class<?>> type){
		return !type.map(Number.class::isAssignableFrom).orElse(false)
				&& !type.map(String.class::isAssignableFrom).orElse(false)
				&& !type.map(Boolean.class::isAssignableFrom).orElse(false)
				&& !type.map(Class::isPrimitive).orElse(false);
	}

	private static DocumentedExampleDto createBestExample(
			JsonAwareHandlerCodec jsonDecoder,
			Type type,
			Set<Type> parents){
		return createBestExample(jsonDecoder, type, parents, 0);
	}

	protected static DocumentedExampleDto createBestExample(
			JsonAwareHandlerCodec jsonDecoder,
			Type type,
			Set<Type> parents,
			int callWithoutWarning){
		if(parents.contains(type)){
			return new DocumentedExampleDto(null);
		}
		callWithoutWarning = callWithoutWarning - 1;
		Set<Type> parentsWithType = Scanner.of(parents)
				.append(type)
				.collect(HashSet::new);
		return switch(type){
		case ParameterizedType pType -> handleParameterizedTypes(jsonDecoder, pType, parents, parentsWithType);
		case TypeVariable _ -> {
			if(callWithoutWarning < 1){
				logger.warn("undocumented generic, please use AutoBuildable type={} parents={}", type, parents);
			}
			yield new DocumentedExampleDto(null);
		}
		case WildcardType _ -> {
			logger.warn("please document type={} parents={}", type, parents);
			yield new DocumentedExampleDto(null);
		}
		case Class<?> cls when cls.isArray() && cls.getComponentType().isPrimitive() ->
				new DocumentedExampleDto(Array.newInstance(cls.getComponentType(), 1));
		case Class<?> cls when cls.isArray() -> {
			Object[] array = (Object[])Array.newInstance(cls.getComponentType(), 1);
			array[0] = createBestExample(jsonDecoder, cls.getComponentType(), parentsWithType);
			yield new DocumentedExampleDto(array);
		}
		case Class<?> cls when cls.isEnum() -> {
			String enumValuesDisplay = Scanner.of(cls.getEnumConstants())
					.map(jsonDecoder.getJsonSerializer()::serialize)
					.sort()
					.collect(Collectors.joining(","));
			Set<DocumentedExampleEnumDto> exampleEnumDtos = Set.of(
					new DocumentedExampleEnumDto(cls.getSimpleName(), enumValuesDisplay));
			yield new DocumentedExampleDto(cls.getEnumConstants()[0], exampleEnumDtos);
		}
		case Class<?> cls when cls == boolean.class || cls == Boolean.class -> new DocumentedExampleDto(false);
		case Class<?> cls when cls == int.class || cls == Integer.class -> new DocumentedExampleDto(0);
		case Class<?> cls when cls == long.class || cls == Long.class -> new DocumentedExampleDto(0L);
		case Class<?> cls when cls == short.class || cls == Short.class -> new DocumentedExampleDto((short)0);
		case Class<?> cls when cls == byte.class || cls == Byte.class -> new DocumentedExampleDto((byte)0);
		case Class<?> cls when cls == char.class || cls == Character.class -> new DocumentedExampleDto('c');
		case Class<?> cls when cls == float.class || cls == Float.class -> new DocumentedExampleDto(0f);
		case Class<?> cls when cls == double.class || cls == Double.class -> new DocumentedExampleDto(0d);
		case Class<?> cls when cls.isPrimitive() -> new DocumentedExampleDto(0);
		case Class<?> cls when cls == String.class -> new DocumentedExampleDto("");
		case Class<?> cls when cls == Number.class -> new DocumentedExampleDto(0);
		case Class<?> cls when cls == Ulid.class -> new DocumentedExampleDto(new Ulid());
		case Class<?> cls when cls == UlidReversed.class -> new DocumentedExampleDto(new UlidReversed());
		case Class<?> cls when cls == Date.class -> new DocumentedExampleDto(new Date());
		case Class<?> cls when cls == UUID.class -> new DocumentedExampleDto(UUID.randomUUID());
		case Class<?> cls when cls == OffsetDateTime.class -> new DocumentedExampleDto(OffsetDateTime.now());
		case Class<?> cls when cls == OffsetTime.class -> new DocumentedExampleDto(OffsetTime.now());
		case Class<?> cls when cls == LocalDateTime.class -> new DocumentedExampleDto(LocalDateTime.now());
		case Class<?> cls when cls == LocalDate.class -> new DocumentedExampleDto(LocalDate.now());
		case Class<?> cls when cls == LocalTime.class -> new DocumentedExampleDto(LocalTime.now());
		case Class<?> cls when cls == Instant.class -> new DocumentedExampleDto(Instant.now());
		case Class<?> cls when cls == Duration.class -> new DocumentedExampleDto(Duration.ofNanos(59784294311L));
		case Class<?> cls when cls == URI.class -> new DocumentedExampleDto(URI.create("https://github.com/hotpads/datarouter"));
		case Class<?> cls when cls == JsonArray.class -> new DocumentedExampleDto(new JsonArray());
		case Class<?> cls when cls == JsonObject.class -> new DocumentedExampleDto(new JsonObject());
		case Class<?> cls when ExampleProvider.class.isAssignableFrom(cls) -> new DocumentedExampleDto(
				ExampleProviderTool.makeExample(cls));
		case Class<?> cls when cls == JsonPrimitive.class -> new DocumentedExampleDto(new JsonPrimitive(
				"{} || [] || 1 || true || \"string\" || null"));
		case Class<?> cls when cls.isRecord() ->
				handleRecords(cls, jsonDecoder, type, parentsWithType, callWithoutWarning);
		case Class<?> cls when cls.isInterface() || Modifier.isAbstract(cls.getModifiers()) ->
				new DocumentedExampleDto(null);
		default -> {
			Class<?> cls = (Class<?>)type;
			Set<DocumentedExampleEnumDto> exampleEnumDtos = new HashSet<>();
			Object example = ReflectionTool.createNullArgsWithUnsafeAllocator(cls);
			for(Field field : ReflectionTool.getDeclaredFieldsIncludingAncestors(cls)){
				if(cls.equals(field.getType()) || Modifier.isStatic(field.getModifiers())){
					continue;
				}
				field.setAccessible(true);
				try{
					DocumentedExampleDto exampleDto = createBestExample(
							jsonDecoder,
							field.getGenericType(),
							parentsWithType,
							callWithoutWarning);
					exampleEnumDtos.addAll(exampleDto.exampleEnumDtos());
					try{
						field.set(example, exampleDto.exampleObject());
					}catch(Exception e){
						logger.warn("error setting {}", field, e);
					}
				}catch(Exception e){
					logger.warn("error creating {}", type, e);
				}
			}
			yield new DocumentedExampleDto(example, exampleEnumDtos);
		}
		};
	}

	private record ExampleRecordComponent(
			RecordComponent recordComponent,
			Object exampleValue){
	}

	private static DocumentedExampleDto handleParameterizedTypes(
			JsonAwareHandlerCodec jsonDecoder,
			ParameterizedType parameterizedType,
			Set<Type> parents,
			Set<Type> parentsWithType){
		Class<?> rawType = (Class<?>)parameterizedType.getRawType();
		if(List.class.isAssignableFrom(rawType)){
			Type type = parameterizedType.getActualTypeArguments()[0];
			DocumentedExampleDto exampleDto = createBestExample(jsonDecoder, type, parentsWithType);
			List<Object> list = exampleDto.exampleObject() == null ? List.of() : List.of(exampleDto.exampleObject());
			return new DocumentedExampleDto(list, exampleDto.exampleEnumDtos());
		}
		if(Set.class.isAssignableFrom(rawType) || Collection.class.isAssignableFrom(rawType)){
			Type type = parameterizedType.getActualTypeArguments()[0];
			DocumentedExampleDto exampleDto = createBestExample(jsonDecoder, type, parentsWithType);
			return new DocumentedExampleDto(
					Collections.singleton(exampleDto.exampleObject()),
					exampleDto.exampleEnumDtos());
		}
		if(Map.class.isAssignableFrom(rawType)){
			Type keyType = parameterizedType.getActualTypeArguments()[0];
			Type valueType = parameterizedType.getActualTypeArguments()[1];
			DocumentedExampleDto key = createBestExample(jsonDecoder, keyType, parentsWithType);
			DocumentedExampleDto value = createBestExample(jsonDecoder, valueType, parentsWithType);
			Map<Object,Object> example = Collections.singletonMap(key.exampleObject(), value.exampleObject());
			Set<DocumentedExampleEnumDto> exampleEnumDtos = Scanner.concat(
					key.exampleEnumDtos(),
					value.exampleEnumDtos())
					.collect(Collectors.toSet());
			if(SortedMap.class.isAssignableFrom(rawType)){
				return new DocumentedExampleDto(new TreeMap<>(example), exampleEnumDtos);
			}
			return new DocumentedExampleDto(example, exampleEnumDtos);
		}
		if(Optional.class.isAssignableFrom(rawType)){
			Type type = parameterizedType.getActualTypeArguments()[0];
			DocumentedExampleDto example = createBestExample(jsonDecoder, type, parentsWithType);
			return new DocumentedExampleDto(Optional.ofNullable(example.exampleObject()), example.exampleEnumDtos());
		}
		if(DocumentedGenericHolder.class.isAssignableFrom(rawType)){
			DocumentedExampleDto exampleDto = createBestExample(jsonDecoder, rawType, parents, 3);
			DocumentedGenericHolder autoBuildable = (DocumentedGenericHolder)exampleDto.exampleObject();
			List<DocumentedExampleDto> innerObjects = Scanner.of(parameterizedType.getActualTypeArguments())
					.map(paramType -> createBestExample(jsonDecoder, paramType, parentsWithType))
					.list();
			List<String> fieldNames = autoBuildable.getGenericFieldNames();
			Map<String,Object> genericFields = new HashMap<>();
			for(int i = 0; i < innerObjects.size(); i++){
				String fieldName = fieldNames.get(i);
				Field field;
				try{
					field = rawType.getDeclaredField(fieldName);
				}catch(NoSuchFieldException | SecurityException e){
					logger.warn("fieldName={} rawType={}", fieldName, rawType, e);
					continue;
				}
				field.setAccessible(true);
				Object value = innerObjects.get(i).exampleObject();
				if(rawType.isRecord()){
					genericFields.put(field.getName(), value);
				}else{
					ReflectionTool.set(field, autoBuildable, value);
				}
			}
			if(rawType.isRecord()){
				Object exampleRecord = Scanner.of(rawType.getRecordComponents())
						.map(recordComponent -> {
							try{
								return new ExampleRecordComponent(recordComponent,
										recordComponent.getAccessor().invoke(exampleDto.exampleObject()));
							}catch(IllegalAccessException | InvocationTargetException e){
								logger.error("Error invoking accessor for recordComponentName={} on rawType={}",
										recordComponent.getName(),
										rawType,
										e);
								throw new RuntimeException(e);
							}
						})
						.map(exampleRecordComponent -> {
							if(genericFields.containsKey(exampleRecordComponent.recordComponent.getName())){
								return new ExampleRecordComponent(exampleRecordComponent.recordComponent,
										genericFields.get(exampleRecordComponent.recordComponent.getName()));
							}
							return exampleRecordComponent;
						})
						.map(ExampleRecordComponent::exampleValue)
						.listTo(values -> {
							try{
								return ReflectionTool.getCanonicalRecordConstructor(rawType)
										.newInstance(values.toArray());
							}catch(Exception e){
								logger.error("Error invoking canonical constructor for rawType={} with values={}",
										rawType,
										values,
										e);
								throw new RuntimeException(e);
							}
						});
				autoBuildable = (DocumentedGenericHolder) exampleRecord;
			}
			Set<DocumentedExampleEnumDto> enums = Scanner.of(innerObjects)
					.concatIter(DocumentedExampleDto::exampleEnumDtos)
					.collect(Collectors.toSet());
			enums.addAll(exampleDto.exampleEnumDtos());
			return new DocumentedExampleDto(autoBuildable, enums);
		}
		return new DocumentedExampleDto(createBestExample(jsonDecoder, rawType, parentsWithType));
	}

	private static DocumentedExampleDto handleRecords(
			Class<?> recordClazz,
			JsonAwareHandlerCodec jsonDecoder,
			Type type,
			Set<Type> parentsWithType,
			int callWithoutWarning){
		Set<DocumentedExampleEnumDto> exampleEnumDtos = new HashSet<>();
		List<Object> constructorParams = new ArrayList<>();
		Object newRecord = null;
		try{
			Scanner.of(recordClazz.getRecordComponents())
					.map(RecordComponent::getGenericType)
					.map(genericType -> createBestExample(
							jsonDecoder,
							genericType,
							parentsWithType,
							callWithoutWarning))
					.forEach(exampleDto -> {
						exampleEnumDtos.addAll(exampleDto.exampleEnumDtos());
						constructorParams.add(exampleDto.exampleObject());
					});
			Constructor<?> constructor = ReflectionTool.getCanonicalRecordConstructor(recordClazz);
			constructor.setAccessible(true);
			newRecord = constructor.newInstance(constructorParams.toArray());
		}catch(Exception e){
			logger.warn("error creating {}", type, e);
		}
		return new DocumentedExampleDto(newRecord, exampleEnumDtos);
	}

	private static String buildEnumValuesString(Collection<DocumentedExampleEnumDto> exampleEnumDtos){
		var builder = new StringBuilder();
		Scanner.of(exampleEnumDtos)
				.sort(Comparator.comparing(DocumentedExampleEnumDto::enumName))
				.forEach(dto -> {
					builder.append(dto.enumName());
					builder.append(": ");
					builder.append(dto.enumValuesDisplay().replace("\"", ""));
					builder.append("\n");
				});
		return builder.toString();
	}

}
