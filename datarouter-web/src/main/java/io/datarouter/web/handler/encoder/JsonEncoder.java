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
package io.datarouter.web.handler.encoder;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.util.ExceptionTool;
import io.datarouter.web.util.http.ResponseTool;
import io.datarouter.web.util.http.exception.HttpExceptionTool;

@Singleton
public class JsonEncoder implements HandlerEncoder{

	private final JsonSerializer jsonSerializer;
	private final ExceptionHandlingConfig exceptionHandlingConfig;

	@Inject
	public JsonEncoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer jsonSerializer,
			ExceptionHandlingConfig exceptionHandlingConfig){
		this.jsonSerializer = jsonSerializer;
		this.exceptionHandlingConfig = exceptionHandlingConfig;
	}

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws IOException{
		sendRequestJson(response, request, serialize(result));
	}

	protected String serialize(Object result){
		try(var $ = TracerTool.startSpan(TracerThreadLocal.get(), "JsonEncoder serialize")){
			String string = jsonSerializer.serialize(result);
			TracerTool.appendToSpanInfo("characters", string.length());
			return string;
		}
	}

	protected void sendRequestJson(HttpServletResponse response, @SuppressWarnings("unused") HttpServletRequest request,
			String json)
	throws IOException{
		ResponseTool.sendJson(response, json);
	}

	@Override
	public void sendHandledExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request) throws IOException{
		sendErrorJson(exception.getHttpResponseCode(), getJsonForException(exception), response, request);
	}

	protected void sendErrorJson(int statusCode, String json, HttpServletResponse response,
			@SuppressWarnings("unused") HttpServletRequest request) throws IOException{
		ResponseTool.sendJson(response, statusCode, json);
	}

	private String getJsonForException(HandledException exception){
		Object httpResponseBody = exception.getHttpResponseBody();
		if(httpResponseBody == null){
			return ResponseTool.getJsonForMessage(exception.getHttpResponseCode(), exception.getMessage());
		}
		return serialize(httpResponseBody);
	}

	@Override
	public void sendInvalidRequestParamResponse(RequestParamValidatorErrorResponseDto errorResponseDto,
			ServletContext servletContext, HttpServletResponse response, HttpServletRequest request) throws IOException{
		String json = getJsonForRequestParamValidatorErrorResponseDto(errorResponseDto);
		sendErrorJson(errorResponseDto.statusCode, json, response, request);
	}

	protected String getJsonForRequestParamValidatorErrorResponseDto(
			RequestParamValidatorErrorResponseDto errorResponseDto){
		return ResponseTool.getJsonForMessage(errorResponseDto.statusCode, errorResponseDto.message);
	}

	@Override
	public void sendExceptionResponse(HttpServletRequest request, HttpServletResponse response, Throwable exception,
			Optional<String> exceptionId) throws IOException{
		Optional<Object> errorObject;
		if(exceptionHandlingConfig.shouldDisplayStackTrace(request, exception)){
			errorObject = buildDetailedErrorObject(exception, exceptionId);
		}else{
			int httpStatusCode = HttpExceptionTool.getHttpStatusCodeForException(response, exception);
			errorObject = buildSimpleErrorObject(exception, httpStatusCode, exceptionId);
		}
		if(errorObject.isPresent()){
			finishSendException(exception, errorObject.get(), response, request);
		}
	}

	protected void finishSendException(@SuppressWarnings("unused") Throwable exception, Object errorObject,
			HttpServletResponse response, HttpServletRequest request) throws IOException{
		finishRequest(errorObject, null, response, request);
	}

	// override this to write a custom json object containing detailed info for debugging by developer
	protected Optional<Object> buildDetailedErrorObject(Throwable exception, Optional<String> exceptionId){
		String exceptionRecordUrl = exceptionId
				.map(exceptionHandlingConfig::buildExceptionLinkForCurrentServer)
				.orElse(null);
		return Optional.of(new DetailedError(ExceptionTool.getStackTraceAsString(exception), exceptionRecordUrl));
	}

	/**
	 * Override this to write a custom json object for the end user
	 *
	 * @param exception The exception that occurred
	 * @param httpStatusCode The HTTP status code that will be returned
	 */
	protected Optional<Object> buildSimpleErrorObject(Throwable exception, int httpStatusCode,
			Optional<String> exceptionId){
		return exceptionId.map(SimpleError::new);
	}

	@SuppressWarnings("unused")
	@Override
	public void sendForbiddenResponse(HttpServletRequest request, HttpServletResponse response,
			SecurityValidationResult securityValidationResult) throws IOException{
	}

	private static class DetailedError{
		@SuppressWarnings("unused")
		private final String stackTrace;
		@SuppressWarnings("unused")
		private final String exceptionRecordUrl;

		private DetailedError(String stackTrace, String exceptionRecordUrl){
			this.stackTrace = stackTrace;
			this.exceptionRecordUrl = exceptionRecordUrl;
		}
	}

	private static class SimpleError{
		@SuppressWarnings("unused")
		private final String errorId;

		private SimpleError(String errorId){
			this.errorId = errorId;
		}
	}

}
