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
import java.io.InputStream;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;

@Singleton
public class DefaultEncoder implements HandlerEncoder{

	private final MavEncoder mavEncoder;
	private final InputStreamHandlerEncoder inputStreamHandlerEncoder;
	private final JsonEncoder jsonEncoder;

	@Inject
	public DefaultEncoder(MavEncoder mavEncoder, InputStreamHandlerEncoder inputStreamHandlerEncoder,
			JsonEncoder jsonEncoder){
		this.mavEncoder = mavEncoder;
		this.inputStreamHandlerEncoder = inputStreamHandlerEncoder;
		this.jsonEncoder = jsonEncoder;
	}

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request)
	throws ServletException, IOException{
		if(result == null){
			return;
		}
		if(result instanceof Mav){
			mavEncoder.finishRequest(result, servletContext, response, request);
			return;
		}
		if(result instanceof InputStream){
			inputStreamHandlerEncoder.finishRequest(result, servletContext, response, request);
			return;
		}
		jsonEncoder.finishRequest(result, servletContext, response, request);
	}

	@Override
	public void sendHandledExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request)
	throws IOException{
		jsonEncoder.sendHandledExceptionResponse(exception, servletContext, response, request);
	}

	@Override
	public void sendInvalidRequestParamResponse(RequestParamValidatorErrorResponseDto errorResponseDto,
			ServletContext servletContext, HttpServletResponse response, HttpServletRequest request)
	throws IOException{
		jsonEncoder.sendInvalidRequestParamResponse(errorResponseDto, servletContext, response, request);
	}

	@Override
	public void sendExceptionResponse(HttpServletRequest request, HttpServletResponse response, Throwable exception,
			Optional<String> exceptionId)
	throws IOException{
		if(shouldSendHtml(request)){
			mavEncoder.sendExceptionResponse(request, response, exception, exceptionId);
		}else{
			jsonEncoder.sendExceptionResponse(request, response, exception, exceptionId);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void sendForbiddenResponse(HttpServletRequest request, HttpServletResponse response,
			SecurityValidationResult securityValidationResult)
	throws IOException{
	}

	protected boolean shouldSendHtml(HttpServletRequest request){
		String accept = request.getHeader(HttpHeaders.ACCEPT);
		if(accept == null){
			return true;
		}
		int jsonIndex = accept.indexOf(ContentType.APPLICATION_JSON.getMimeType());
		if(jsonIndex == -1){
			return true;
		}
		int htmlIndex = accept.indexOf(ContentType.TEXT_HTML.getMimeType());
		if(htmlIndex == -1){
			return false;
		}
		return htmlIndex < jsonIndex;
	}

}
