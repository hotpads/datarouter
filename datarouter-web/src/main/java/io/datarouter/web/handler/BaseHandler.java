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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.httpclient.endpoint.EndpointTool;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.endpoint.EndpointValidator;
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
import io.datarouter.web.handler.validator.RequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import io.datarouter.web.user.session.RequestAwareCurrentSessionInfoFactory;
import io.datarouter.web.user.session.RequestAwareCurrentSessionInfoFactory.RequestAwareCurrentSessionInfo;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.RequestDurationTool;
import io.datarouter.web.util.http.RequestTool;

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
			BaseHandler.class, MISSING_PARAMETERS_HANDLER_METHOD_NAME, List.class, String.class);

	@Inject
	private HandlerTypingHelper handlerTypingHelper;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private Optional<ExceptionRecorder> exceptionRecorder;
	@Inject
	private HandlerMetrics handlerMetrics;
	@Inject
	private RequestAwareCurrentSessionInfoFactory requestAwareCurrentSessionInfoFactory;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;

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

	protected Object handleMissingParameters(List<String> missingParameters, String methodName){
		String message = "missing parameters for " + getClass().getSimpleName() + "." + methodName + ": " + String.join(
				", ", missingParameters);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		throw new RuntimeException(message);
	}

	/*
	 * handler methods in sub-classes will need this annotation as a security measure,
	 * otherwise all methods would be callable
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Handler{
		String description() default "";
		Class<? extends HandlerEncoder> encoder() default NullHandlerEncoder.class;
		Class<? extends HandlerDecoder> decoder() default NullHandlerDecoder.class;
		/**
		 * @deprecated  Specify the path directly. The method name should match the PathNode
		 */
		@Deprecated
		boolean defaultHandler() default false;
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestParamValidators(Method method,
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
					.exclude(responseDto -> responseDto.success)
					.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto)
					.findFirst();
			if(errorResponseDto.isPresent()){
				return errorResponseDto;
			}
		}
		return Optional.empty();
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestParamValidatorsFromEndpoint(Method method,
			Object[] args){
		return Optional.ofNullable(method.getParameters()[0].getAnnotation(EndpointValidator.class))
				.map(EndpointValidator::validator)
				.filter(Objects::nonNull)
				.map(validate("endpoint", args[0]))
				.filter(responseDto -> !responseDto.success)
				.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto);
	}

	private <T> Function<Class<? extends RequestParamValidator<?>>,RequestParamValidatorResponseDto> validate(
			String parameterName, Object parameterValue){
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

	protected <P,R extends RequestParamValidator<P>> R getParamValidator(Class<R> cls, P parameterValue,
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

			Pair<Method,Object[]> handlerMethodAndArgs = getHandlerMethodAndArgs();
			Method method = handlerMethodAndArgs.getLeft();
			Object[] args = handlerMethodAndArgs.getRight();
			RequestAttributeTool.set(request, HANDLER_CLASS, getClass());
			RequestAttributeTool.set(request, HANDLER_METHOD, method);

			HandlerEncoder encoder = getHandlerEncoder(method);
			RequestAttributeTool.set(request, HANDLER_ENCODER_ATTRIBUTE, encoder);

			Optional<RequestParamValidatorErrorResponseDto> errorResponseDtoOptional;
			if(EndpointTool.paramIsEndpointObject(method)){
				errorResponseDtoOptional = validateRequestParamValidatorsFromEndpoint(method, args);
			}else{
				errorResponseDtoOptional = validateRequestParamValidators(method, args);
			}
			if(errorResponseDtoOptional.isPresent()){
				RequestParamValidatorErrorResponseDto errorResponseDto = errorResponseDtoOptional.get();
				encoder.sendInvalidRequestParamResponse(errorResponseDto, servletContext, response, request);
				return;
			}
			invokeHandlerMethod(method, args, encoder);
		}catch(IOException | ServletException e){
			throw new RuntimeException("", e);
		}
	}

	private Pair<Method,Object[]> getHandlerMethodAndArgs(){
		String methodName = handlerMethodName();
		List<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(getClass(), methodName).stream()
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.collect(Collectors.toList());

		Pair<Method,Object[]> pair = handlerTypingHelper.findMethodByName(possibleMethods, defaultHandlerDecoder,
				request);
		Method method = pair.getLeft();
		Object[] args = pair.getRight();

		Optional<Method> defaultHandlerMethod = getDefaultHandlerMethod();
		if(method == null){
			if(defaultHandlerMethod.isPresent()){
				pair = handlerTypingHelper.findMethodByName(Collections.singletonList(defaultHandlerMethod.get()),
						defaultHandlerDecoder, request);
				method = pair.getLeft();
				args = pair.getRight();
			}
		}
		if(method == null){
			if(!possibleMethods.isEmpty() || defaultHandlerMethod.isPresent()){
				Method desiredMethod = possibleMethods.isEmpty() ? defaultHandlerMethod.get() : possibleMethods.get(0);
				List<String> missingParameters = getMissingParameterNames(desiredMethod);
				args = new Object[]{missingParameters, desiredMethod.getName()};
				method = MISSING_PARAMETERS_HANDLER_METHOD;
			}else{
				args = new Object[]{methodName};
				method = DEFAULT_HANDLER_METHOD;
			}
		}
		return new Pair<>(method, Optional.ofNullable(args).orElse(new Object[]{}));
	}

	private HandlerEncoder getHandlerEncoder(Method method){
		Class<? extends HandlerEncoder> encoderClass = defaultHandlerEncoder;
		if(method.isAnnotationPresent(Handler.class)){
			Class<? extends HandlerEncoder> methodEncoder = method.getAnnotation(Handler.class).encoder();
			if(!methodEncoder.equals(NullHandlerEncoder.class)){
				encoderClass = methodEncoder;
			}
		}
		return injector.getInstance(encoderClass);
	}

	public void invokeHandlerMethod(Method method, Object[] args, HandlerEncoder encoder)
	throws ServletException, IOException{
		handlerMetrics.incMethodInvocation(getClass(), method.getName());
		if(accountName != null && !accountName.isEmpty()){
			handlerMetrics.incMethodInvocationByApiKeyPredicateName(getClass(), method.getName(), accountName);
		}
		Object result;
		try{
			result = method.invoke(this, args);
		}catch(IllegalAccessException e){
			throw new RuntimeException(e);
		}catch(InvocationTargetException e){
			Throwable cause = e.getCause();
			if(cause instanceof HandledException){
				HandledException handledException = (HandledException)cause;
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
						.map(exexceptionRecordDto -> exexceptionRecordDto.id);
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
				.exclude(parameter -> OptionalParameter.class.isAssignableFrom(parameter.getType()))
				.map(Parameter::getName)
				.exclude(param -> params.toMap().containsKey(param))
				.list();
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

	private static class NullHandlerEncoder implements HandlerEncoder{

		@Override
		public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
				HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendHandledExceptionResponse(HandledException exception, ServletContext servletContext,
				HttpServletResponse response, HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendInvalidRequestParamResponse(RequestParamValidatorErrorResponseDto errorResponseDto,
				ServletContext servletContext, HttpServletResponse response, HttpServletRequest request){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendExceptionResponse(HttpServletRequest request, HttpServletResponse response,
				Throwable exception, Optional<String> exceptionId){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendForbiddenResponse(HttpServletRequest request, HttpServletResponse response,
				SecurityValidationResult securityValidationResult){
			throw new UnsupportedOperationException();
		}

	}

	public static class NullHandlerDecoder implements HandlerDecoder{

		@Override
		public Object[] decode(HttpServletRequest request, Method method){
			throw new UnsupportedOperationException();
		}

	}

}
