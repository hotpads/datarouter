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
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.httpclient.response.exception.DocumentedServerError;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.documentation.HttpDocumentedExceptionTool;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorErrorResponseDto;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.util.ExceptionService;
import io.datarouter.web.util.http.ResponseTool;

public class MavEncoder implements HandlerEncoder{

	@Inject
	private ExceptionService exceptionService;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;

	@Override
	public void finishRequest(
			Object result,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request)
	throws ServletException, IOException{
		if(result == null){
			return;
		}
		Mav mav = (Mav)result;

		if(mav.isRedirect()){
			response.sendRedirect(mav.getRedirectUrl());
			return;
		}

		response.setContentType(mav.getContentType());
		response.setStatus(mav.getStatusCode());
		// add the model variables as request attributes
		mav.getModel().forEach(request::setAttribute);

		// forward to the jsp
		String targetContextName = mav.getContext();
		String viewName = mav.getViewName();
		ServletContext targetContext = servletContext;
		if(targetContextName != null){
			targetContext = servletContext.getContext(targetContextName);
			throw new RuntimeException("Could not acquire servletContext=" + targetContextName
					+ ".  Make sure context has crossContext=true enabled.");
		}
		RequestDispatcher dispatcher = targetContext.getRequestDispatcher(viewName);
		try(var $ = TracerTool.startSpan("RequestDispatcher.include", TraceSpanGroupType.HTTP)){
			dispatcher.include(request, response);
		}
	}

	@Override
	public void sendHandledExceptionResponse(
			HandledException exception,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request){
		sendErrorResponse(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), response);
	}

	@Override
	public void sendInvalidRequestParamResponse(
			RequestParamValidatorErrorResponseDto errorResponseDto,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request){
		sendErrorResponse(errorResponseDto.statusCode, errorResponseDto.message, response);
	}

	private void sendErrorResponse(int statusCode, String errorMessage, HttpServletResponse response){
		ResponseTool.sendError(response, statusCode, errorMessage);
	}

	@Override
	public void sendExceptionResponse(
			HttpServletRequest request,
			HttpServletResponse response,
			Throwable exception,
			Optional<String> exceptionId)
	throws IOException{
		response.setContentType("text/html;charset=" + StandardCharsets.UTF_8.name());
		PrintWriter out = response.getWriter();
		Optional<DocumentedServerError> optDoc = HttpDocumentedExceptionTool
				.findDocumentationInChain(exception);
		if(exceptionHandlingConfig.shouldDisplayStackTrace(request, exception)){
			try{
				response.resetBuffer();
			}catch(IllegalStateException e){
				// ignore resetBuffer error for committed response
			}
			out.println("<html><body>");
			out.print("Error");
			exceptionId
					.map(exceptionHandlingConfig::buildExceptionLinkForCurrentServer)
					.ifPresent(link -> out.print("<a href=\"" + link + "\"> " + exceptionId.get() + "</a>"));
			optDoc.ifPresent(doc -> out.print(": " + doc.getErrorMessage()));
			out.println();
			out.println("<pre>");
			out.print(exceptionService.getStackTraceStringForHtmlPreBlock(exception));
			out.println("</pre></body></html>");
		}else{
			out.print("Error");
			exceptionId.ifPresent(id -> out.print(" " + id));
			optDoc.ifPresent(doc -> out.print(": " + doc.getErrorMessage()));
		}
	}

	@Override
	public void sendForbiddenResponse(
			HttpServletRequest request,
			HttpServletResponse response,
			SecurityValidationResult securityValidationResult){
	}

}
