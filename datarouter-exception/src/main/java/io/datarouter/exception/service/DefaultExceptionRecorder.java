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
package io.datarouter.exception.service;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordPublisherDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordPublisherDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.exception.ExceptionCategory;
import io.datarouter.storage.exception.UnknownExceptionCategory;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.exception.ExceptionCounters;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.WebExceptionCategory;
import io.datarouter.web.monitoring.GitProperties;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.util.ExceptionTool;
import io.datarouter.web.util.RequestAttributeTool;

public class DefaultExceptionRecorder implements ExceptionRecorder{
	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionRecorder.class);

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private GitProperties gitProperties;
	@Inject
	private ExceptionRecordService exceptionRecordService;
	@Inject
	private DatarouterExceptionRecordPublisherDao exceptionRecordPublisherDao;
	@Inject
	private DatarouterHttpRequestRecordPublisherDao httpRequestRecordPublisherDao;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;
	@Inject
	private CurrentSessionInfo currentSessionInfo;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterExceptionRecordDao exceptionRecordDao;
	@Inject
	private DatarouterHttpRequestRecordDao httpRequestRecordDao;
	@Inject
	private DatarouterExceptionSettingRoot settings;

	@Override
	public Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin){
		return tryRecordException(exception, callOrigin, UnknownExceptionCategory.UNKNOWN);
	}

	@Override
	public Optional<ExceptionRecordDto> tryRecordException(
			Throwable exception,
			String callOrigin,
			ExceptionCategory category){
		try{
			DefaultExceptionRecorderDetails exceptionDetails = detectExceptionLocation(exception);
			return Optional.of(recordException(
					exception,
					category,
					exceptionDetails.className,
					exceptionDetails.methodName,
					exceptionDetails.lineNumber,
					callOrigin));
		}catch(Exception e){
			logger.warn("Exception while recording an exception", e);
		}
		return Optional.empty();
	}

	@Override
	public ExceptionRecordDto recordException(
			Throwable exception,
			ExceptionCategory category,
			String location,
			String methodName,
			Integer lineNumber,
			String callOrigin){
		if(callOrigin == null){
			callOrigin = location;
		}
		ExceptionCounters.inc(category.name());
		ExceptionCounters.inc(category.name() + " " + webappName);
		ExceptionCounters.inc(exception.getClass().getName());
		ExceptionCounters.inc(callOrigin);
		ExceptionCounters.inc(exception.getClass().getName() + " " + callOrigin);
		ExceptionRecord exceptionRecord = new ExceptionRecord(
				datarouterService.getServiceName(),
				datarouterProperties.getServerName(),
				ExceptionTool.getStackTraceAsString(exception),
				exception.getClass().getName(),
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				location,
				methodName,
				lineNumber,
				callOrigin);
		exceptionRecordDao.put(exceptionRecord);
		logger.warn("Exception recorded ({})", exceptionRecordService.buildExceptionLinkForCurrentServer(
				exceptionRecord));
		if(settings.publishRecords.get()){
			exceptionRecordPublisherDao.put(exceptionRecord);
		}
		if(exceptionHandlingConfig.shouldReportError(exceptionRecord.toDto())){
			report(exceptionRecord, category);
		}
		return exceptionRecord.toDto();
	}

	@Override
	public Optional<ExceptionRecordDto> tryRecordExceptionAndHttpRequest(
			Throwable exception,
			String callOrigin,
			HttpServletRequest request){
		try{
			DefaultExceptionRecorderDetails exceptionDetails = detectExceptionLocation(exception);
			return Optional.of(recordExceptionAndHttpRequest(
					exception,
					exceptionDetails.className,
					exceptionDetails.methodName,
					exceptionDetails.lineNumber,
					request,
					callOrigin));
		}catch(Exception e){
			logger.warn("Exception while recording an exception", e);
			return Optional.empty();
		}
	}

	@Override
	public ExceptionRecordDto recordExceptionAndHttpRequest(
			Throwable exception,
			String location,
			String methodName,
			Integer lineNumber,
			HttpServletRequest request,
			String callOrigin){
		ExceptionRecordDto exceptionRecord = recordException(
				exception,
				WebExceptionCategory.HTTP_REQUEST,
				location,
				methodName,
				lineNumber,
				callOrigin);
		recordHttpRequest(request, exceptionRecord, true);
		return exceptionRecord;
	}

	protected void report(@SuppressWarnings("unused") ExceptionRecord exceptionRecord,
			@SuppressWarnings("unused") ExceptionCategory category){
	}

	@Override
	public void recordHttpRequest(HttpServletRequest request){
		recordHttpRequest(request, null, false);
	}

	private void recordHttpRequest(
			HttpServletRequest request,
			ExceptionRecordDto exceptionRecord,
			boolean publish){
		Optional<String> userToken = currentSessionInfo.getSession(request).map(Session::getUserToken);
		String userRoles = currentSessionInfo.getRoles(request).toString();

		boolean omitPayload = RequestAttributeTool.get(request, Dispatcher.TRANSMITS_PII).orElse(false);
		HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
				exceptionRecord == null ? null : exceptionRecord.id,
				request,
				userRoles,
				userToken.orElse(null),
				omitPayload);
		httpRequestRecord.trimContentType();
		httpRequestRecord.trimAcceptCharset();
		httpRequestRecord.trimXForwardedFor();
		httpRequestRecord.trimPath();
		httpRequestRecord.trimAcceptLanguage();
		httpRequestRecordDao.put(httpRequestRecord);
		httpRequestRecord.trimBinaryBody(10_000);
		if(publish && settings.publishRecords.get()){
			httpRequestRecordPublisherDao.put(httpRequestRecord);
		}
	}

	private DefaultExceptionRecorderDetails detectExceptionLocation(Throwable wholeException){
		Throwable rootCause = ExceptionUtils.getRootCause(wholeException);
		Throwable exception = Optional.ofNullable(rootCause).orElse(wholeException);
		StackTraceElement stackTraceElement = searchClassName(exception)
				.orElseGet(() -> {
					StackTraceElement[] stackTrace = exception.getStackTrace();
					// stackTrace is often null in case of OOM
					return stackTrace.length == 0 ? null : stackTrace[0];
				});
		if(stackTraceElement == null){
			return new DefaultExceptionRecorderDetails(
					"noClass",
					"noMethod",
					0);
		}
		return new DefaultExceptionRecorderDetails(
				stackTraceElement.getClassName(),
				stackTraceElement.getMethodName(),
				stackTraceElement.getLineNumber());
	}

	private Optional<StackTraceElement> searchClassName(Throwable cause){
		for(StackTraceElement element : cause.getStackTrace()){
			for(String highlight : datarouterWebSettingRoot.stackTraceHighlights.get()){
				if(element.getClassName().contains(highlight)){
					return Optional.of(element);
				}
			}
		}
		return Optional.empty();
	}

	private static class DefaultExceptionRecorderDetails{

		public final String className;
		public final String methodName;
		public final int lineNumber;

		protected DefaultExceptionRecorderDetails(String className, String methodName, int lineNumber){
			this.className = className;
			this.methodName = methodName;
			this.lineNumber = lineNumber;
		}

	}

}
