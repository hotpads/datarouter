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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.auth.session.RequestAwareCurrentSessionInfoFactory;
import io.datarouter.auth.session.RequestAwareCurrentSessionInfoFactory.RequestAwareCurrentSessionInfo;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.java.BaseJavaEndpoint;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.api.EndpointTool;
import io.datarouter.web.api.endpoint.EndpointValidator;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.handler.types.HandlerDecoder;
import io.datarouter.web.handler.types.HandlerTypingHelper;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalParameter;
import io.datarouter.web.handler.validator.DefaultRequestParamValidator;
import io.datarouter.web.handler.validator.FieldValidator;
import io.datarouter.web.handler.validator.HandlerAccountCallerValidator;
import io.datarouter.web.handler.validator.RequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.RequestDurationTool;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;

/*
 * a dispatcher servlet sets necessary parameters and then calls "handle()"
 */
public abstract class BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);

	public static final String SUBMIT_ACTION = "submitAction";
	public static final RequestAttributeKey<Date> REQUEST_RECEIVED_AT = new RequestAttributeKey<>("receivedAt");
	public static final RequestAttributeKey<String> REQUEST_DURATION_STRING = new RequestAttributeKey<>(
			"durationString");
	public static final RequestAttributeKey<String> TRACE_URL_REQUEST_ATTRIBUTE = new RequestAttributeKey<>("traceUrl");
	public static final RequestAttributeKey<HandlerEncoder> HANDLER_ENCODER_ATTRIBUTE = new RequestAttributeKey<>(
			"handlerEncoder");
	public static final RequestAttributeKey<Class<? extends BaseHandler>> HANDLER_CLASS = new RequestAttributeKey<>(
			"handlerClass");
	public static final RequestAttributeKey<Method> HANDLER_METHOD = new RequestAttributeKey<>("handlerMethod");
	public static final RequestAttributeKey<W3TraceContext> TRACE_CONTEXT = new RequestAttributeKey<>("traceContext");

	private static final Pattern LAST_SEGMENT_PATTERN = Pattern.compile("[^?]*/([^/?]+)[/?]?.*");
	private static final String DEFAULT_HANDLER_METHOD_NAME = "noHandlerFound";
	private static final Method DEFAULT_HANDLER_METHOD = ReflectionTool.getDeclaredMethodIncludingAncestors(
			BaseHandler.class, DEFAULT_HANDLER_METHOD_NAME, String.class);

	private static final String MISSING_PARAMETERS_HANDLER_METHOD_NAME = "handleMissingParameters";
	private static final Method MISSING_PARAMETERS_HANDLER_METHOD = ReflectionTool.getDeclaredMethodIncludingAncestors(
			BaseHandler.class,
			MISSING_PARAMETERS_HANDLER_METHOD_NAME,
			List.class,
			String.class);

	@Inject
	private HandlerTypingHelper handlerTypingHelper;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private Optional<ExceptionRecorder> exceptionRecorder;
	@Inject
	private RequestAwareCurrentSessionInfoFactory requestAwareCurrentSessionInfoFactory;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;
	@Inject
	private HandlerAccountCallerValidator handlerAccountCallerValidator;
	@Inject
	private HandlerMetrics handlerMetrics;

	private Class<? extends HandlerEncoder> defaultHandlerEncoder;
	private Class<? extends HandlerDecoder> defaultHandlerDecoder;
	/**
	 * same as apiKeyPredicateName
	 */
	protected String accountName;

	//these are available to all handlers without passing them around
	protected ServletContext servletContext;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Params params;
	protected List<RequestParamValidator<?>> paramValidators = new ArrayList<>();

	private final Supplier<RequestAwareCurrentSessionInfo> requestAwareCurrentSessionInfo = SingletonSupplier.of(
			() -> requestAwareCurrentSessionInfoFactory.build(request));

	/**
	 * Used via reflection, see {@link #DEFAULT_HANDLER_METHOD_NAME}
	 */
	@SuppressWarnings("unused")
	private Object noHandlerFound(String methodName){
		String message = "no method named " + methodName + " or default method found in " + getClass().getSimpleName()
				+ ", please specify " + handlerMethodParamName();
		logger.warn(message);
		MessageMav mav = new MessageMav(message);
		mav.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
		return mav;
	}

	/**
	 * Used via reflection, see {@link #MISSING_PARAMETERS_HANDLER_METHOD_NAME}
	 */
	protected Object handleMissingParameters(List<String> missingParameters, String methodName){
		String message = "missing parameters for " + getClass().getSimpleName() + "." + methodName + ": " + String.join(
				", ", missingParameters);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new RuntimeException(message);
	}

	/**
	 * Subclass methods annotated with {@code @Handler} will be available to be called as handlers
	 * for matching paths. This annotation is required as a security measure otherwise all methods
	 * in the subclass would be callable.
	 *
	 * <p>This annotation type has a string-valued element {@code deprecatedOn}.
	 * The value of this element indicates the date at which it will,
	 * or has, become unsupported. If the value is non-empty, then it will be parsed
	 * with {@link DateTimeFormatter#ISO_INSTANT}. If not able to be parsed, the current
	 * date will be used instead.
	 *
	 * <p>The epoch-second timestamp at which the handler will become unsupported will be
	 * included as a header in responses sent back to the client compliant with
	 * <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpapi-deprecation-header">the Proposed Deprecation RFC Standard</a>
	 *
	 * <p>This annotation type has an optional string-valued element {@code deprecationLink}.
	 * The value of this element indicates the URL where more information about the
	 * deprecation can be found.
	 *
	 * <p>The optional element {@code usageType} indicates when the handler is
	 * used at a different frequency than normal. This is useful for keeping necessary handlers
	 * out of infrequent-usage reports, reducing noise. If your handler is only temporarily unused,
	 * use the {@code HandlerUsageType.TEMPORARILY_UNUSED} type to be reminded when your handler starts
	 * getting called.
	 *
	 * <p><b>Note:</b>
	 * It is strongly recommended that the reason for deprecating a Handler
	 * be explained in the documentation. The documentation should also suggest and
	 * link to a recommended replacement API, if applicable. A replacement API often
	 * has subtly different semantics, so such issues should be discussed as well.
	 * */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Handler{
		String description() default "";
		String deprecatedOn() default "";
		String deprecationLink() default "";
		HandlerUsageType usageType() default HandlerUsageType.IN_USE;

		enum HandlerUsageType{
			TEMPORARILY_UNUSED,
			INFREQUENTLY_USED,
			IN_USE
		}

		/**
		 * @deprecated  Specify the encoder in the RouteSet class
		 */
		@Deprecated
		Class<? extends HandlerEncoder> encoder() default NoOpHandlerEncoder.class;
		/**
		 * @deprecated  Specify the decoder in the RouteSet class
		 */
		@Deprecated
		Class<? extends HandlerDecoder> decoder() default NoOpHandlerDecoder.class;

		/**
		 * @deprecated  Specify the path directly. The method name should match the PathNode
		 */
		@Deprecated
		boolean defaultHandler() default false;
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestParamValidators(
			Method method,
			Object[] args){
		Parameter[] parameters = method.getParameters();
		for(int index = 0; index < parameters.length; index++){
			Parameter parameter = parameters[index];
			String parameterName = parameter.getName();
			Optional<?> optionalParameterValue = HandlerTool.getParameterValue(args[index]);
			if(optionalParameterValue.isEmpty()){
				continue;
			}
			Object parameterValue = optionalParameterValue.get();
			Annotation[] parameterAnnotations = parameter.getAnnotations();
			Optional<RequestParamValidatorErrorResponseDto> errorResponseDto = Scanner.of(parameterAnnotations)
					.include(Param.class::isInstance)
					.map(Param.class::cast)
					.map(Param::validator)
					.exclude(DefaultRequestParamValidator.class::equals)
					.map(validate(parameterName, parameterValue))
					.exclude(RequestParamValidatorResponseDto::success)
					.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto)
					.findFirst();
			if(errorResponseDto.isPresent()){
				return errorResponseDto;
			}
		}
		return Optional.empty();
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestParamValidatorsFromEndpoint(
			Method method,
			Object[] args){
		return Optional.ofNullable(method.getParameters()[0].getAnnotation(EndpointValidator.class))
				.map(EndpointValidator::validator)
				.filter(Objects::nonNull)
				.map(validate("endpoint", args[0]))
				.filter(responseDto -> !responseDto.success())
				.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto);
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateApiRequestField(
			Method method,
			Object[] args){
		if(args.length == 1 && args[0] instanceof BaseEndpoint baseEndpoint){
			return validateRequestFieldValidators(method, baseEndpoint);
		}
		return Optional.empty();
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestFieldValidators(
			Method method,
			BaseEndpoint baseEndpoint){
		List<Field> endpointFields = Scanner.of(baseEndpoint.getClass().getFields())
				.exclude(field -> field.isAnnotationPresent(IgnoredField.class))
				.list();
		Set<Field> requestBodyFields = new HashSet<>(endpointFields.size());
		Set<Field> otherEndpointFields = new HashSet<>(endpointFields.size());
		Scanner.of(endpointFields)
				.forEach(field -> {
					if(field.isAnnotationPresent(RequestBody.class)){
						requestBodyFields.add(field);
					}else{
						otherEndpointFields.add(field);
					}
				});
		Optional<RequestParamValidatorErrorResponseDto> error = Optional.empty();
		// validate endpoint's GET fields
		if(!otherEndpointFields.isEmpty()){
			error = validateClassFields(otherEndpointFields, method, baseEndpoint);
		}
		if(error.isPresent()){
			return error;
		}
		Optional<Field> requestBodyFieldOptional = Scanner.of(requestBodyFields)
				.findFirst();
		if(requestBodyFieldOptional.isEmpty()){
			return error;
		}
		Field requestBodyField = requestBodyFieldOptional.get();
		Object requestBody;
		try{
			requestBody = requestBodyField.get(baseEndpoint);
		}catch(IllegalAccessException | IllegalArgumentException e){
			// maybe return a specific error?
			return error;
		}
		// validate annotated requestBody
		if(requestBodyField.isAnnotationPresent(FieldValidator.class)){
			return Optional.ofNullable(requestBodyField.getAnnotation(FieldValidator.class))
					.map(FieldValidator::value)
					.filter(validator -> !DefaultRequestParamValidator.class.isAssignableFrom(validator))
					.map(validator -> validate(requestBodyField.getName(), requestBody).apply(validator))
					.filter(responseDto -> !responseDto.success())
					.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto);
		}
		// validate record requestBody fields
		if(requestBodyField.getType().isRecord()){
			Map<String,Class<? extends RequestParamValidator<?>>> validatorByRequestBodyFieldName = Scanner
					.of(requestBodyField.getType().getDeclaredFields())
					.exclude(field -> {
						FieldValidator annotation = field.getAnnotation(FieldValidator.class);
						if(annotation == null){
							return true;
						}
						Class<? extends RequestParamValidator<?>> validator = annotation.value();
						return DefaultRequestParamValidator.class.isAssignableFrom(validator);
					})
					.toMap(Field::getName, field -> field.getAnnotation(FieldValidator.class).value());
			return Scanner.of(requestBodyField.getType().getRecordComponents())
					.include(comp -> validatorByRequestBodyFieldName.containsKey(comp.getName()))
					.map(comp -> {
						String fieldName = comp.getName();
						Object fieldValue;
						try{
							fieldValue = comp.getAccessor().invoke(requestBody);
						}catch(IllegalAccessException | InvocationTargetException e){
							logger.error("Unable to retrieve value for field '{}', method '{}'",
									fieldName,
									method.getName(),
									e);
							// maybe return error?
							return RequestParamValidatorResponseDto.makeSuccessResponse();
						}
						Optional<?> optionalFieldValue = HandlerTool.getParameterValue(fieldValue);
						if(optionalFieldValue.isEmpty()){
							return RequestParamValidatorResponseDto.makeSuccessResponse();
						}
						Object value = optionalFieldValue.get();
						logger.debug("methodName={}; fieldName={}; fieldValue={}", method.getName(), fieldName, value);
						Class<? extends RequestParamValidator<?>> validator =
								validatorByRequestBodyFieldName.get(fieldName);
						return validate(fieldName, value).apply(validator);
					})
					.exclude(RequestParamValidatorResponseDto::success)
					.findFirst()
					.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto);
		}
		// validate class requestBody fields
		return validateClassFields(Arrays.asList(requestBodyField.getType().getDeclaredFields()), method, requestBody);
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateClassFields(
			Collection<Field> fields,
			Method method,
			Object endpointOrRequestBody){
		return Scanner.of(fields)
				.include(field -> field.isAnnotationPresent(FieldValidator.class))
				.map(field -> {
					FieldValidator annotation = field.getAnnotation(FieldValidator.class);
					Class<? extends RequestParamValidator<?>> validator = annotation.value();
					if(DefaultRequestParamValidator.class.isAssignableFrom(validator)){
						return RequestParamValidatorResponseDto.makeSuccessResponse();
					}
					String fieldName = field.getName();
					Object fieldValue;
					try{
						fieldValue = field.get(endpointOrRequestBody);
					}catch(IllegalAccessException | IllegalArgumentException e){
						logger.error("Unable to retrieve value for field '{}', method '{}'",
								fieldName,
								method.getName(),
								e);
						// maybe return error?
						return RequestParamValidatorResponseDto.makeSuccessResponse();
					}
					Optional<?> optionalFieldValue = HandlerTool.getParameterValue(fieldValue);
					if(optionalFieldValue.isEmpty()){
						return RequestParamValidatorResponseDto.makeSuccessResponse();
					}
					Object value = optionalFieldValue.get();
					logger.debug("methodName={}; fieldName={}; fieldValue={}", method.getName(), fieldName, value);
					return validate(fieldName, value).apply(validator);
				})
				.exclude(RequestParamValidatorResponseDto::success)
				.findFirst()
				.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto);
	}

	private <T> Function<Class<? extends RequestParamValidator<?>>,RequestParamValidatorResponseDto> validate(
			String parameterName,
			Object parameterValue){
		return validatorClass -> {
			@SuppressWarnings("unchecked")
			RequestParamValidator<T> validator = (RequestParamValidator<T>)injector.getInstance(validatorClass);
			validator.setParameterName(parameterName);
			paramValidators.add(validator);
			return validator.validate(request, validator.getParameterClass().cast(parameterValue));
		};
	}

	protected <P,R extends RequestParamValidator<P>> R getParamValidator(Class<R> cls, P parameterValue){
		return getParamValidator(cls, parameterValue, null);
	}

	protected <P,R extends RequestParamValidator<P>> R getParamValidator(
			Class<R> cls,
			P parameterValue,
			String parameterName){
		RequestParamValidator<?> paramValidator = paramValidators.stream()
				.filter(validator -> validator.getClass().equals(cls))
				// in case the handler method is using the same param validator more than once
				.filter(validator -> parameterName == null || parameterName.equals(validator.getParameterName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("paramValidator unavailable: " + cls.getSimpleName()
						+ ", parameterValue: " + parameterValue
						+ (parameterName == null ? "" : ", parameterName: " + parameterName)));
		return cls.cast(paramValidator);
	}

	public void handleWrapper(){//dispatcher servlet calls this
		try{
			permitted();

			HandlerMethodAndArgs handlerMethodAndArgs = getHandlerMethodAndArgs();
			Method method = handlerMethodAndArgs.method();
			Object[] args = handlerMethodAndArgs.args();
			RequestAttributeTool.set(request, HANDLER_CLASS, getClass());
			RequestAttributeTool.set(request, HANDLER_METHOD, method);

			HandlerEncoder encoder = getHandlerEncoder(method);
			RequestAttributeTool.set(request, HANDLER_ENCODER_ATTRIBUTE, encoder);

			Optional<RequestParamValidatorErrorResponseDto> errorResponseDtoOptional;
			if(EndpointTool.paramIsBaseEndpointObject(method)){
				Parameter[] parameters = method.getParameters();
				if(Optional.ofNullable(parameters[0].getAnnotation(EndpointValidator.class)).isPresent()){
					errorResponseDtoOptional = validateRequestParamValidatorsFromEndpoint(method, args);
				}else{
					errorResponseDtoOptional = validateApiRequestField(method, args);
				}
			}else{
				errorResponseDtoOptional = validateRequestParamValidators(method, args);
			}
			if(errorResponseDtoOptional.isPresent()){
				RequestParamValidatorErrorResponseDto errorResponseDto = errorResponseDtoOptional.get();
				encoder.sendInvalidRequestParamResponse(errorResponseDto, servletContext, response, request);
				return;
			}
			surfaceDeprecationInformation(method);
			invokeHandlerMethod(method, args, encoder);
		}catch(IOException | ServletException e){
			throw new RuntimeException("", e);
		}
	}

	public HandlerMethodAndArgs getHandlerMethodAndArgs(){
		String methodName = handlerMethodName();
		List<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(getClass(), methodName).stream()
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.toList();

		HandlerMethodAndArgs pair = handlerTypingHelper.findMethodByName(
				possibleMethods,
				defaultHandlerDecoder,
				request);
		Method method = pair.method();
		Object[] args = pair.args();

		Optional<Method> defaultHandlerMethod = getDefaultHandlerMethod();
		if(method == null){
			if(defaultHandlerMethod.isPresent()){
				pair = handlerTypingHelper.findMethodByName(Collections.singletonList(defaultHandlerMethod.get()),
						defaultHandlerDecoder, request);
				method = pair.method();
				args = pair.args();
			}
		}
		if(method == null){
			if(!possibleMethods.isEmpty() || defaultHandlerMethod.isPresent()){
				Method desiredMethod = possibleMethods.isEmpty()
						? defaultHandlerMethod.get() : possibleMethods.getFirst();
				List<String> missingParameters;
				if(EndpointTool.paramIsBaseEndpointObject(desiredMethod)){
					missingParameters = getMissingParameterNamesIfApi(desiredMethod);
				}else{
					missingParameters = getMissingParameterNames(desiredMethod);
				}
				args = new Object[]{missingParameters, desiredMethod.getName()};
				method = MISSING_PARAMETERS_HANDLER_METHOD;
			}else{
				args = new Object[]{methodName};
				method = DEFAULT_HANDLER_METHOD;
			}
		}
		return new HandlerMethodAndArgs(method, Optional.ofNullable(args).orElse(new Object[]{}));
	}

	public record HandlerMethodAndArgs(
			Method method,
			Object[] args){
	}

	// This lookup does not take into account params
	public String estimateHandlerMethod(){
		String methodName = handlerMethodName();
		List<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(getClass(), methodName).stream()
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.toList();

		Optional<Method> defaultHandlerMethod = getDefaultHandlerMethod();
		Method method;
		if(!possibleMethods.isEmpty() || defaultHandlerMethod.isPresent()){
			method = possibleMethods.isEmpty() ? defaultHandlerMethod.get() : possibleMethods.getFirst();
		}else{
			method = DEFAULT_HANDLER_METHOD;
		}
		return method.getName();
	}

	private HandlerEncoder getHandlerEncoder(Method method){
		Class<? extends HandlerEncoder> encoderClass = defaultHandlerEncoder;
		if(method.isAnnotationPresent(Handler.class)){
			Class<? extends HandlerEncoder> methodEncoder = method.getAnnotation(Handler.class).encoder();
			if(!methodEncoder.equals(NoOpHandlerEncoder.class)){
				encoderClass = methodEncoder;
			}
		}
		return injector.getInstance(encoderClass);
	}

	public HandlerDecoder getHandlerDecoder(Method method){
		Class<? extends HandlerDecoder> decoderClass = defaultHandlerDecoder;
		if(method.isAnnotationPresent(Handler.class)){
			Class<? extends HandlerDecoder> methodDecoder = method.getAnnotation(Handler.class).decoder();
			if(!methodDecoder.equals(NoOpHandlerDecoder.class)){
				decoderClass = methodDecoder;
			}
		}
		return injector.getInstance(decoderClass);
	}

	public void invokeHandlerMethod(Method method, Object[] args, HandlerEncoder encoder)
	throws ServletException, IOException{
		handlerMetrics.incMethodInvocation(getClass(), method.getName(), RequestTool.getUserAgent(getRequest()));
		if(accountName != null && !accountName.isEmpty()){
			HandlerMetrics.incMethodInvocationByApiKeyPredicateName(getClass(), method.getName(), accountName);
		}

		if(accountName != null && !accountName.isEmpty()){
			if(args.length == 1 && args[0] instanceof BaseJavaEndpoint<?,?> endpoint){
				handlerAccountCallerValidator.validate(accountName, endpoint);
			}else{
				handlerAccountCallerValidator.validate(accountName, method);
			}
		}

		Object result;
		try{
			result = method.invoke(this, args);
		}catch(IllegalAccessException e){
			throw new RuntimeException(e);
		}catch(InvocationTargetException e){
			Throwable cause = e.getCause();
			if(cause instanceof HandledException handledException){
				Exception exception = (Exception)cause;//don't allow HandledExceptions to be Throwable
				Optional<String> eid = exceptionRecorder
						.map(recorder -> {
							try{
								return recorder.recordExceptionAndHttpRequest(
										exception,
										exception.getClass().getName(),
										getClass().getName(),
										null,
										method.getName(),
										null,
										request,
										null);
							}catch(Exception recordingException){
								logger.warn("Error recording exception", recordingException);
								return null;
							}
						})
						.map(ExceptionRecordDto::id);
				eid.ifPresent(exceptionId -> response.setHeader(HttpHeaders.X_EXCEPTION_ID, exceptionId));
				encoder.sendHandledExceptionResponse(handledException, servletContext, response, request);
				logger.warn("returning statusCode={} eid={} message={}", handledException.getHttpResponseCode(),
						eid.orElse(null), cause.getMessage());
				return;
			}
			if(cause instanceof RuntimeException){
				throw (RuntimeException)cause;
			}
			throw new RuntimeException(cause);
		}
		RequestDurationTool.getRequestElapsedDurationString(request)
				.ifPresent(durationStr -> RequestAttributeTool.set(request, REQUEST_DURATION_STRING, durationStr));
		encoder.finishRequest(result, servletContext, response, request);
		postProcess(result);
	}

	protected RequestAwareCurrentSessionInfo getSessionInfo(){
		return requestAwareCurrentSessionInfo.get();
	}

	protected ZoneId getUserZoneId(){
		return currentUserSessionInfoService.getZoneId(request);
	}

	/*---------------- optionally override these -----------------*/

	protected void postProcess(@SuppressWarnings("unused") Object result){
		//override if necessary
	}

	private boolean permitted(){
		//allow everyone by default
		return true;
		//override if necessary
		//could also have a filter with more authentication somewhere else
	}

	private String handlerMethodName(){
		return params.optional(handlerMethodParamName())
				.orElseGet(() -> getLastPathSegment(RequestTool.getPath(request)));
	}

	private Optional<Method> getDefaultHandlerMethod(){
		Optional<Method> defaultHandlerMethod = getDefaultHandlerMethodForClass(getClass());
		if(defaultHandlerMethod.isEmpty()){
			for(Class<?> cls : ReflectionTool.getAllSuperClassesAndInterfaces(getClass())){
				defaultHandlerMethod = getDefaultHandlerMethodForClass(cls);
				if(defaultHandlerMethod.isPresent()){
					break;
				}
			}
		}
		defaultHandlerMethod.ifPresent(method -> method.setAccessible(true));
		return defaultHandlerMethod;
	}

	private Optional<Method> getDefaultHandlerMethodForClass(Class<?> cls){
		return Scanner.of(cls.getDeclaredMethods())
				.include(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.include(possibleMethod -> possibleMethod.getDeclaredAnnotation(Handler.class).defaultHandler())
				.findFirst();
	}

	protected static String getLastPathSegment(String uri){
		if(uri == null){
			return "";
		}
		return LAST_SEGMENT_PATTERN.matcher(uri).replaceAll("$1");
	}

	private String handlerMethodParamName(){
		return SUBMIT_ACTION;
	}

	private List<String> getMissingParameterNames(Method method){
		return Scanner.of(method.getParameters())
				.exclude(parameter -> OptionalParameter.class.isAssignableFrom(parameter.getType())
						|| Optional.class.isAssignableFrom(parameter.getType()))
				.map(Parameter::getName)
				.exclude(param -> params.toMap().containsKey(param))
				.list();
	}

	private List<String> getMissingParameterNamesIfApi(Method method){
		Class<?> endpointType = method.getParameters()[0].getType();
		@SuppressWarnings("unchecked")
		BaseEndpoint baseEndpoint = ReflectionTool.createWithoutNoArgs(
				(Class<? extends BaseEndpoint>)endpointType);

		return Scanner.of(EndpointTool.getRequiredKeys(baseEndpoint).getAllKeys())
				.exclude(param -> params.toMap().containsKey(param))
				.list();
	}

	private void surfaceDeprecationInformation(Method handlerMethod){
		Handler handlerAnnotation = handlerMethod.getAnnotation(Handler.class);
		if(handlerAnnotation != null && !handlerAnnotation.deprecatedOn().isEmpty()){
			Instant deprecatedAt = HandlerTool.parseHandlerDeprecatedOnDate(handlerAnnotation.deprecatedOn());
			response.setHeader("Deprecation", "@" + deprecatedAt.getEpochSecond());
			if(!handlerAnnotation.deprecationLink().isEmpty()){
				response.setHeader("Link", handlerAnnotation.deprecationLink());
			}
		}
	}

	/*---------------- get/set -----------------*/

	public HttpServletRequest getRequest(){
		return request;
	}

	public Charset getDefaultMultipartCharset(){
		return null;
	}

	public void setParams(Params params){
		this.params = params;
	}

	public void setServletContext(ServletContext context){
		this.servletContext = context;
	}

	public void setRequest(HttpServletRequest request){
		this.request = request;
	}

	public void setResponse(HttpServletResponse response){
		this.response = response;
	}

	public void setDefaultHandlerEncoder(Class<? extends HandlerEncoder> defaultHandlerEncoder){
		this.defaultHandlerEncoder = defaultHandlerEncoder;
	}

	public void setDefaultHandlerDecoder(Class<? extends HandlerDecoder> defaultHandlerDecoder){
		this.defaultHandlerDecoder = defaultHandlerDecoder;
	}

	/**
	 *
	 * @param accountName same as apiKeyPredicateName
	 */
	public void setAccountName(String accountName){
		this.accountName = accountName;
	}

	public static class NoOpHandlerEncoder implements HandlerEncoder{

		@Override
		public void finishRequest(
				Object result,
				ServletContext servletContext,
				HttpServletResponse response,
				HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendHandledExceptionResponse(
				HandledException exception,
				ServletContext servletContext,
				HttpServletResponse response,
				HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendInvalidRequestParamResponse(
				RequestParamValidatorErrorResponseDto errorResponseDto,
				ServletContext servletContext,
				HttpServletResponse response,
				HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendExceptionResponse(
				HttpServletRequest request,
				HttpServletResponse response,
				Throwable exception,
				Optional<String> exceptionId){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendForbiddenResponse(
				HttpServletRequest request,
				HttpServletResponse response,
				SecurityValidationResult securityValidationResult){
			throw new UnsupportedOperationException();
		}

	}

	public static class NoOpHandlerDecoder implements HandlerDecoder{

		@Override
		public Object[] decode(HttpServletRequest request, Method method){
			throw new UnsupportedOperationException();
		}

	}

}
