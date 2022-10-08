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
package io.datarouter.web.handler.encoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;

public class RawStringEncoder implements HandlerEncoder{

	@Override
	public void finishRequest(
			Object result,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request)
	throws IOException{
		try(PrintWriter writer = response.getWriter()){
			writer.print(result);
		}
	}

	@Override
	public void sendHandledExceptionResponse(
			HandledException exception,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request)
	throws IOException{
		sendErrorResponse(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), response);
	}

	@Override
	public void sendInvalidRequestParamResponse(
			RequestParamValidatorErrorResponseDto errorResponseDto,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request)
	throws IOException{
		sendErrorResponse(errorResponseDto.statusCode, errorResponseDto.message, response);
	}

	public void sendErrorResponse(int statusCode, String errorMessage, HttpServletResponse response) throws IOException{
		response.sendError(statusCode, errorMessage);
	}

	@Override
	public void sendExceptionResponse(
			HttpServletRequest request,
			HttpServletResponse response,
			Throwable exception,
			Optional<String> exceptionId)
	throws IOException{
		sendErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage(), response);
	}

	@Override
	public void sendForbiddenResponse(
			HttpServletRequest request,
			HttpServletResponse response,
			SecurityValidationResult securityValidationResult){
	}

}
