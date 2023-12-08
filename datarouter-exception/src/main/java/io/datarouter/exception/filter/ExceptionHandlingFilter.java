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
package io.datarouter.exception.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.utils.ExceptionDetailsDetector;
import io.datarouter.exception.utils.ExceptionDetailsDetector.ExceptionRecorderDetails;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.DefaultEncoder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.shutdown.ShutdownService;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RequestTool;
import io.datarouter.web.util.http.exception.HttpExceptionTool;

public abstract class ExceptionHandlingFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

	private static final Set<Integer> statusToLog = Set.of(
			HttpServletResponse.SC_NOT_FOUND,
			HttpServletResponse.SC_GONE,
			HttpServletResponse.SC_FORBIDDEN,
			ShutdownService.SHUTDOWN_STATUS_CODE);

	private DatarouterInjector injector;
	private WebappName webappName;
	private ExceptionRecorder exceptionRecorder;
	private ExceptionDetailsDetector exceptionDetailsDetector;
	private DatarouterWebSettingRoot webSettings;

	@Override
	public void init(FilterConfig filterConfig){
		injector = getInjector(filterConfig.getServletContext());
		webappName = injector.getInstance(WebappName.class);
		exceptionRecorder = injector.getInstance(ExceptionRecorder.class);
		exceptionDetailsDetector = injector.getInstance(ExceptionDetailsDetector.class);
		webSettings = injector.getInstance(DatarouterWebSettingRoot.class);
	}

	@SuppressWarnings("unused")
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException{
		HttpServletResponse response = (HttpServletResponse)res;
		HttpServletRequest request = (HttpServletRequest)req;

		RequestAttributeTool.set(request, BaseHandler.REQUEST_RECEIVED_AT, new Date());
		try{
			fc.doFilter(req, res);
		}catch(Throwable e){
			Optional<String> exceptionId = tryRecordExceptionAndRequestNotification(request, e)
					.map(ExceptionRecordKey::getId);
			logger.error("ExceptionHandlingFilter caught an exception exceptionId={}", exceptionId.orElse(""), e);
			exceptionId.ifPresent(id -> response.setHeader(HttpHeaders.X_EXCEPTION_ID, id));
			writeExceptionToResponseWriter(response, e, request, exceptionId);
		}
		Counters.inc(webappName + " response " + response.getStatus());
		Counters.inc("response " + response.getStatus());
		if(statusToLog.contains(response.getStatus())){
			logger.warn("{} on {} ip={} userAgent={}", response.getStatus(), RequestTool.getRequestUriWithQueryString(
					request), RequestTool.getIpAddress(request), RequestTool.getUserAgent(request));
		}
	}

	private Optional<ExceptionRecordKey> tryRecordExceptionAndRequestNotification(
			HttpServletRequest request,
			Throwable exception){
		try{
			String callOrigin;
			Optional<Class<? extends BaseHandler>> handlerClass = RequestAttributeTool.get(request,
					BaseHandler.HANDLER_CLASS);
			Optional<Method> handlerMethod = RequestAttributeTool.get(request, BaseHandler.HANDLER_METHOD);
			if(handlerClass.isPresent() && handlerMethod.isPresent()){
				callOrigin = handlerClass.get().getName() + "." + handlerMethod.get().getName();
			}else{
				callOrigin = null;
			}

			HttpServerCounters.count("error " + callOrigin);
			String methodName = null;
			String name = null;
			String type = null;
			PlaceAndLineNumber pair = searchJspName(exception);
			String location = pair.place();
			Integer lineNumber = pair.lineNumber();
			if(location == null){
				ExceptionRecorderDetails details = exceptionDetailsDetector.detect(exception, callOrigin,
						webSettings.stackTraceHighlights.get());
				location = details.className;
				methodName = details.methodName;
				name = details.parsedName;
				type = details.type;
				lineNumber = details.lineNumber;
			}
			ExceptionRecordDto exceptionRecord = exceptionRecorder.recordExceptionAndHttpRequest(exception, location,
					methodName, name, type, lineNumber, request, callOrigin);

			return Optional.of(new ExceptionRecordKey(exceptionRecord.id()));
		}catch(Exception e){
			logger.error("Exception while logging", e);
			return Optional.empty();
		}
	}

	private static PlaceAndLineNumber searchJspName(Throwable exception){
		String place;
		Integer lineNumber = null;
		Throwable cause = exception;
		do{
			String key = "An exception occurred processing JSP page ";
			if(cause.getMessage() == null){
				cause = cause.getCause();
				continue;
			}
			int indexOfBegin = cause.getMessage().indexOf(key);
			if(indexOfBegin > -1){
				String key2 = " at line ";
				int index = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf("\n");
				place = cause.getMessage().substring(indexOfBegin + key.length(), index);
				try{
					lineNumber = Integer.parseInt(cause.getMessage().substring(index + key2.length(), endLine));
				}catch(NumberFormatException ex){// It's OK if it's not a number
				}
				return new PlaceAndLineNumber(place, lineNumber);
			}
			/* An error occurred at line: 3 in the jsp file: /WEB-INF/jsp/jspError.jsp */
			String keyInTheJspFile = " in the jsp file: ";
			indexOfBegin = cause.getMessage().indexOf(keyInTheJspFile);
			if(indexOfBegin > -1){
				String key2 = " at line: ";
				int index = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf('\n', indexOfBegin);
				place = cause.getMessage().substring(indexOfBegin + keyInTheJspFile.length(), endLine);
				try{
					lineNumber = Integer.parseInt(cause.getMessage().substring(index + key2.length(), indexOfBegin));
				}catch(NumberFormatException ex){// It's OK if it's not a number
				}
				return new PlaceAndLineNumber(place, lineNumber);
			}
			place = getJspName(cause.getMessage());
			if(place != null){
				return new PlaceAndLineNumber(place, lineNumber);
			}
			for(StackTraceElement element : cause.getStackTrace()){
				place = getJspName(element.getClassName());
				if(place != null){
					return new PlaceAndLineNumber(place, lineNumber);
				}
			}
			cause = cause.getCause();
		}while(cause != null);
		return new PlaceAndLineNumber(null, null);
	}

	private record PlaceAndLineNumber(
			String place,
			Integer lineNumber){
	}

	private static String getJspName(String string){
		if(string == null){
			return null;
		}
		String key = "WEB_002dINF";
		int index = string.indexOf(key);
		if(index > -1){
			String jspName = string.substring(index);
			jspName = jspName.replace('.', '/');
			jspName = jspName.replace("_002d", "-");
			jspName = jspName.replace('_', '.');
			return "/" + jspName;
		}
		return null;
	}

	private void writeExceptionToResponseWriter(
			HttpServletResponse response,
			Throwable exception,
			HttpServletRequest request,
			Optional<String> exceptionId){
		try{
			HandlerEncoder encoder = RequestAttributeTool.get(request, BaseHandler.HANDLER_ENCODER_ATTRIBUTE)
					.orElseGet(() -> injector.getInstance(DefaultEncoder.class));
			int httpStatusCode = HttpExceptionTool.getHttpStatusCodeForException(response, exception);
			response.setStatus(httpStatusCode);
			encoder.sendExceptionResponse(request, response, exception, exceptionId);
		}catch(Exception e){
			logger.error("Exception while writing error http response", e);

		}
	}

}
