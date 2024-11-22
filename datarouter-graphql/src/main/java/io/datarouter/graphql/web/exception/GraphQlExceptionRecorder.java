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
package io.datarouter.graphql.web.exception;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.InvalidSyntaxError;
import graphql.SerializationError;
import graphql.execution.NonNullableFieldWasNullError;
import graphql.execution.ResultPath;
import graphql.validation.ValidationError;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.error.DatarouterGraphQlDataValidationError;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.IpAddressService;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlExceptionRecorder{
	private static final Logger logger = LoggerFactory.getLogger(GraphQlExceptionRecorder.class);

	@Inject
	private ExceptionRecorder recorder;
	@Inject
	private IpAddressService ipAddressService;

	public List<GraphQlErrorDto> recordGraphQlErrors(List<GraphQLError> errors, HttpServletRequest request){
		if(errors == null || errors.isEmpty()){
			return List.of();
		}
		List<GraphQlErrorDto> errorDtos = new ArrayList<>();
		for(GraphQLError error : errors){
			Optional<Class<? extends BaseHandler>> handlerClass = RequestAttributeTool.get(request,
					BaseHandler.HANDLER_CLASS);
			Optional<Method> handlerMethod = RequestAttributeTool.get(request, BaseHandler.HANDLER_METHOD);
			String callOrigin = handlerClass.get().getName() + "." + handlerMethod.get().getName() + " " + getPath(
					error).orElse("");
			Throwable exception;

			// Some GraphQl errors don't have throwable attached. We will need to create custom exceptions for each them
			if(ExceptionWhileDataFetching.class.isAssignableFrom(error.getClass())){
				exception = ((ExceptionWhileDataFetching)error).getException();
				errorDtos.add(GraphQlErrorDto.internalError("Exception while fetching data: " + exception.getMessage(),
						getPath(error)));
			}else if(SerializationError.class.isAssignableFrom(error.getClass())){
				exception = ((SerializationError)error).getException();
				errorDtos.add(GraphQlErrorDto.illegalQuery("Can't serialize value: " + exception.getMessage(), getPath(
						error)));
			}else if(ValidationError.class.isAssignableFrom(error.getClass())){
				exception = new GraphQlValidationException((ValidationError)error);
				errorDtos.add(GraphQlErrorDto.illegalQuery(error.getMessage(), getPath(error)));
			}else if(InvalidSyntaxError.class.isAssignableFrom(error.getClass())){
				exception = new GraphQlInvalidSyntaxException((InvalidSyntaxError)error);
				errorDtos.add(GraphQlErrorDto.illegalQuery(error.getMessage(), getPath(error)));
			}else if(NonNullableFieldWasNullError.class.isAssignableFrom(error.getClass())){
				exception = new GraphQlNonNullableFieldWasNullException((NonNullableFieldWasNullError)error);
				errorDtos.add(GraphQlErrorDto.illegalQuery(error.getMessage(), getPath(error)));
			}else if(DatarouterGraphQlDataValidationError.class.isAssignableFrom(error.getClass())){
				/* Note: don't send this error to exception recorder because it's a service's input data validation
				 * warning which is used to communicate with client */
				DatarouterGraphQlDataValidationError qlError = (DatarouterGraphQlDataValidationError)error;
				logResult(request, qlError);
				errorDtos.add(qlError.getError());
				GraphQlExceptionCounters.inc(qlError);
				continue;
			}else{
				// other errors that we might encounter in the future but currently we have not seen
				exception = new GraphQLException(error.getClass().getName() + ", message=" + error.getMessage());
				errorDtos.add(GraphQlErrorDto.illegalQuery(error.getMessage()));
			}
			GraphQlExceptionCounters.inc(exception, error.getErrorType());
			GraphQlExceptionCounters.inc(error, error.getErrorType());
			Optional<String> exceptionId = recorder.tryRecordExceptionAndHttpRequest(exception, callOrigin, request)
					.map(ExceptionRecordDto::id);
			logger.error("GraphQlExceptionRecorder caught an exception exceptionId={}", exceptionId.orElse(""),
					exception);
		}
		return errorDtos;
	}

	private Optional<String> getPath(GraphQLError error){
		return Optional.ofNullable(error.getPath())
				.map(ResultPath::fromList)
				.map(path -> "" + path);
	}

	public void logResult(HttpServletRequest request, GraphQLError error){
		logger.warn("{}, uri=\"{}\", path=\"{}\", ip={}, userAgent=\"{}\", referrer=\"{}\"",
				error.toString(),
				request.getRequestURI(),
				getPath(error).orElse(""),
				ipAddressService.getIpAddress(request),
				RequestTool.getUserAgent(request),
				RequestTool.getReferer(request));
	}

}
