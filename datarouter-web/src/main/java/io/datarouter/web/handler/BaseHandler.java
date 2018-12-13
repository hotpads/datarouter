/**
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.exception.NotImplementedException;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.handler.types.HandlerDecoder;
import io.datarouter.web.handler.types.HandlerTypingHelper;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.validator.DefaultRequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;
import io.datarouter.web.util.http.RequestTool;

/*
 * a dispatcher servlet sets necessary parameters and then calls "handle()"
 */
public abstract class BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);

	private static final String DEFAULT_HANDLER_METHOD_NAME = "noHandlerFound";
	private static final String MISSING_PARAMETERS_HANDLER_METHOD_NAME = "handleMissingParameters";

	@Inject
	private HandlerTypingHelper handlerTypingHelper;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private Optional<ExceptionRecorder> exceptionRecorder;
	@Inject
	private HandlerCounters handlerCounters;

	private Class<? extends HandlerEncoder> defaultHandlerEncoder;
	private Class<? extends HandlerDecoder> defaultHandlerDecoder;

	//these are available to all handlers without passing them around
	protected ServletContext servletContext;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Params params;
	protected List<RequestParamValidator<?>> paramValidators = new ArrayList<>();

	/**
	 * Used via reflection, see {@link #DEFAULT_HANDLER_METHOD_NAME}
	 */
	@SuppressWarnings("unused")
	private Object noHandlerFound(){
		MessageMav mav = new MessageMav("no default handler method found in " + getClass().getSimpleName()
				+ ", please specify " + handlerMethodParamName());
		mav.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
		return mav;
	}

	protected Object handleMissingParameters(List<String> missingParameters, String methodName){
		String message = "missing parameters for " + methodName + ": " + String.join(", ", missingParameters);
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
		Class<?>[] expectedParameterClasses() default {};
		Class<?> expectedParameterClassesProvider() default Object.class;
		String description() default "";
		Class<? extends HandlerEncoder> encoder() default NullHandlerEncoder.class;
		Class<? extends HandlerDecoder> decoder() default NullHandlerDecoder.class;
		boolean defaultHandler() default false;
	}

	private Optional<RequestParamValidatorErrorResponseDto> validateRequestParamValidators(Method method,
			Object[] args){
		Parameter[] parameters = method.getParameters();
		for(int index = 0; index < parameters.length; index++){
			Parameter parameter = parameters[index];
			String parameterName = parameter.getName();
			Optional<?> optionalParameterValue = HandlerTool.getParameterValue(args[index]);
			if(!optionalParameterValue.isPresent()){
				continue;
			}
			Object parameterValue = optionalParameterValue.get();
			Annotation[] parameterAnnotations = parameter.getAnnotations();
			Optional<RequestParamValidatorErrorResponseDto> errorResponseDto = Arrays.stream(parameterAnnotations)
					.filter(Param.class::isInstance)
					.map(Param.class::cast)
					.map(Param::validator)
					.filter(validatorClass -> !validatorClass.equals(DefaultRequestParamValidator.class))
					.map(validate(parameterName, parameterValue))
					.filter(responseDto -> !responseDto.success)
					.map(RequestParamValidatorErrorResponseDto::fromRequestParamValidatorResponseDto)
					.findFirst();
			if(errorResponseDto.isPresent()){
				return errorResponseDto;
			}
		}
		return Optional.empty();
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

			HandlerEncoder encoder = getHandlerEncoder(method);
			Optional<RequestParamValidatorErrorResponseDto> errorResponseDtoOptional = validateRequestParamValidators(
					method, args);
			if(errorResponseDtoOptional.isPresent()){
				RequestParamValidatorErrorResponseDto errorResponseDto = errorResponseDtoOptional.get();
				encoder.sendInvalidRequestParamResponse(errorResponseDto, servletContext, response, request);
				return;
			}
			invokeHandlerMethod(method, args, encoder);
		}catch(IOException | ServletException e){
			throw new RuntimeException(e);
		}
	}

	private Pair<Method,Object[]> getHandlerMethodAndArgs(){
		String methodName = handlerMethodName();
		Collection<Method> possibleMethods = ReflectionTool.getDeclaredMethodsWithName(getClass(), methodName)
				.stream()
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.collect(Collectors.toList());

		Pair<Method,Object[]> pair = handlerTypingHelper.findMethodByName(possibleMethods, defaultHandlerDecoder,
				request);
		Method method = pair.getLeft();
		Object[] args = pair.getRight();
		if(method == null){
			Optional<Method> defaultHandlerMethod = getDefaultHandlerMethod();
			if(defaultHandlerMethod.isPresent()){
				pair = handlerTypingHelper.findMethodByName(Collections.singletonList(defaultHandlerMethod.get()),
						defaultHandlerDecoder, request);
				method = pair.getLeft();
				args = pair.getRight();
			}
		}
		if(method == null){
			if(CollectionTool.notEmpty(possibleMethods)){
				Method desiredMethod = CollectionTool.getFirst(possibleMethods);
				List<String> missingParameters = getMissingParameterNames(desiredMethod);
				args = new Object[]{missingParameters, desiredMethod.getName()};

				methodName = MISSING_PARAMETERS_HANDLER_METHOD_NAME;
				method = ReflectionTool.getDeclaredMethodIncludingAncestors(getClass(), methodName, List.class,
						String.class);
			}else{
				methodName = DEFAULT_HANDLER_METHOD_NAME;
				method = ReflectionTool.getDeclaredMethodIncludingAncestors(getClass(), methodName);
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
		handlerCounters.incMethodInvocation(this, method);
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
				String exceptionLocation = getClass().getName() + "/" + method.getName();
				exceptionRecorder
						.ifPresent(recorder -> {
							try{
								recorder.recordExceptionAndHttpRequest(exception, exceptionLocation, null,
										null, request);
							}catch(Exception recordingException){
								logger.warn("Error recording exception", recordingException);
							}
						});
				encoder.sendExceptionResponse(handledException, servletContext, response, request);
				logger.warn("returning {} : {}", handledException.getHttpResponseCode(), cause.getMessage());
				return;
			}
			if(cause instanceof RuntimeException){
				throw (RuntimeException)cause;
			}
			throw new RuntimeException(cause);
		}
		encoder.finishRequest(result, servletContext, response, request);
	}

	/*---------------- optionally override these -----------------*/

	private boolean permitted(){
		//allow everyone by default
		return true;
		//override if necessary
		//could also have a filter with more authentication somewhere else
	}

	private String handlerMethodName(){
		String fullPath = RequestTool.getPath(request);
		String lastPathSegment = getLastPathSegment(fullPath);
		return params.optional(handlerMethodParamName()).orElse(lastPathSegment);
	}

	private Optional<Method> getDefaultHandlerMethod(){
		Optional<Method> defaultHandlerMethod = getDefaultHandlerMethodForClass(getClass());
		if(!defaultHandlerMethod.isPresent()){
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
		return Stream.of(cls.getDeclaredMethods())
				.filter(possibleMethod -> possibleMethod.isAnnotationPresent(Handler.class))
				.filter(possibleMethod -> possibleMethod.getDeclaredAnnotation(Handler.class).defaultHandler())
				.findFirst();
	}

	private static String getLastPathSegment(String uri){
		if(uri == null){
			return "";
		}
		return uri.replaceAll("[^?]*/([^/?]+)[/?]?.*", "$1");
	}

	private String handlerMethodParamName(){
		return "submitAction";
	}

	private List<String> getMissingParameterNames(Method method){
		return Stream.of(method.getParameters())
				.map(Parameter::getName)
				.filter(param -> !params.toMap().keySet().contains(param))
				.collect(Collectors.toList());
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

	private static class NullHandlerEncoder implements HandlerEncoder{

		@Override
		public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
				HttpServletRequest request){
			throw new NotImplementedException();
		}

		@Override
		public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
				HttpServletResponse response, HttpServletRequest request){
			throw new NotImplementedException();
		}

		@Override
		public void sendInvalidRequestParamResponse(RequestParamValidatorErrorResponseDto errorResponseDto,
				ServletContext servletContext, HttpServletResponse response, HttpServletRequest request){
			throw new NotImplementedException();
		}
	}

	public static class NullHandlerDecoder implements HandlerDecoder{

		@Override
		public Object[] decode(HttpServletRequest request, Method method){
			throw new NotImplementedException();
		}
	}

	public static class BaseHandlerTests{

		@Test
		public void testGetLastPathSegment(){
			Assert.assertEquals(getLastPathSegment("/something"), "something");
			Assert.assertEquals(getLastPathSegment("~/something"), "something");
			Assert.assertEquals(getLastPathSegment("/admin/edit/reputation/viewUsers"), "viewUsers");
			Assert.assertEquals(getLastPathSegment("/admin/edit/reputation/viewUsers/"), "viewUsers");
			Assert.assertEquals(getLastPathSegment("/admin/edit/reputation/editUser?u=10"), "editUser");
			Assert.assertEquals(getLastPathSegment("/admin/edit/reputation/editUser/?u=10"), "editUser");
			Assert.assertEquals(getLastPathSegment("https://fake.url/t/rep?querystring=path/path"), "rep");
			Assert.assertEquals(getLastPathSegment("https://fake.url/t/rep/?querystring=path/path"), "rep");
		}
	}
}
