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
package io.datarouter.exception.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.conveyors.DatarouterExceptionBuffers;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordPublisherDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordPublisherDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.exception.utils.ExceptionDetailsDetector;
import io.datarouter.exception.utils.ExceptionDetailsDetector.ExceptionRecorderDetails;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.exception.ExceptionCategory;
import io.datarouter.storage.exception.UnknownExceptionCategory;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.config.service.ServiceName;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.exception.ExceptionCounters;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.WebExceptionCategory;
import io.datarouter.web.handler.BaseHandler;
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
	private ExceptionDetailsDetector exceptionDetailsDetector;
	@Inject
	private DatarouterExceptionRecordPublisherDao exceptionRecordPublisherDao;
	@Inject
	private DatarouterHttpRequestRecordPublisherDao httpRequestRecordPublisherDao;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;
	@Inject
	private CurrentSessionInfo currentSessionInfo;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterExceptionSettingRoot settings;
	@Inject
	private DatarouterExceptionBuffers exceptionBuffers;
	@Inject
	private ServerName serverName;
	@Inject
	private ServiceName serviceName;

	@Override
	public Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin){
		return tryRecordException(exception, callOrigin, UnknownExceptionCategory.UNKNOWN);
	}

	@Override
	public Optional<ExceptionRecordDto> tryRecordException(
			Throwable exception,
			String callOrigin,
			ExceptionCategory category){
		return tryRecordException(exception, callOrigin, category, List.of());
	}

	@Override
	public Optional<ExceptionRecordDto> tryRecordException(
			Throwable exception,
			String callOrigin,
			ExceptionCategory category,
			List<String> additionalEmailRecipients){
		try{
			ExceptionRecorderDetails exceptionDetails = exceptionDetailsDetector.detect(exception, callOrigin,
					datarouterWebSettingRoot.stackTraceHighlights.get());
			return Optional.of(recordException(
					exception,
					category,
					exceptionDetails.className, // location
					exceptionDetails.methodName,
					exceptionDetails.parsedName,
					exceptionDetails.type,
					exceptionDetails.lineNumber,
					callOrigin,
					additionalEmailRecipients));
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
			String name,
			String type,
			Integer lineNumber,
			String callOrigin){
		return recordException(exception, category, location, methodName, name, type, lineNumber, callOrigin, List
				.of());
	}

	@Override
	public ExceptionRecordDto recordException(
			Throwable exception,
			ExceptionCategory category,
			String location,
			String methodName,
			String name,
			String type,
			Integer lineNumber,
			String callOrigin,
			List<String> additionalEmailRecipients){
		if(callOrigin == null){
			callOrigin = location;
		}
		ExceptionCounters.inc(category.name());
		ExceptionCounters.inc(category.name() + " " + webappName);
		ExceptionCounters.inc(name);
		ExceptionCounters.inc(type);
		ExceptionCounters.inc(callOrigin);
		ExceptionCounters.inc(type + " " + callOrigin);
		ExceptionRecord exceptionRecord = new ExceptionRecord(
				serviceName.get(),
				serverName.get(),
				category.name(),
				Optional.ofNullable(name).orElse(ExceptionRecorderDetails.getDefaultName(type, name, callOrigin)),
				ExceptionTool.getStackTraceAsString(exception),
				type,
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				location,
				methodName,
				lineNumber,
				callOrigin);
		exceptionRecord.trimFields();
		exceptionBuffers.exceptionRecordBuffer.offer(exceptionRecord);
		logger.warn("Exception recorded ({})", exceptionRecordService.buildExceptionLinkForCurrentServer(
				exceptionRecord));
		if(settings.publishRecords.get()){
			exceptionRecordPublisherDao.put(exceptionRecord);
		}
		if(exceptionHandlingConfig.shouldReportError(exceptionRecord.toDto())){
			report(exceptionRecord, category, additionalEmailRecipients);
		}
		return exceptionRecord.toDto();
	}

	@Override
	public Optional<ExceptionRecordDto> tryRecordExceptionAndHttpRequest(
			Throwable exception,
			String callOrigin,
			HttpServletRequest request){
		try{
			ExceptionRecorderDetails exceptionDetails = exceptionDetailsDetector.detect(exception, callOrigin,
					datarouterWebSettingRoot.stackTraceHighlights.get());
			return Optional.of(recordExceptionAndHttpRequest(
					exception,
					exceptionDetails.className,
					exceptionDetails.methodName,
					exceptionDetails.parsedName,
					exceptionDetails.type,
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
			String name,
			String type,
			Integer lineNumber,
			HttpServletRequest request,
			String callOrigin){
		ExceptionRecordDto exceptionRecord = recordException(
				exception,
				WebExceptionCategory.HTTP_REQUEST,
				location,
				methodName,
				name,
				type,
				lineNumber,
				callOrigin);
		recordHttpRequest(request, exceptionRecord, true);
		return exceptionRecord;
	}

	protected void report(@SuppressWarnings("unused") ExceptionRecord exceptionRecord,
			@SuppressWarnings("unused") ExceptionCategory category,
			@SuppressWarnings("unused") List<String> additionalEmailRecipients){
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
				RequestAttributeTool.get(request, BaseHandler.TRACE_CONTEXT),
				request,
				userRoles,
				userToken.orElse(null),
				omitPayload);
		httpRequestRecord.trimFields();
		exceptionBuffers.httpRequestRecordBuffer.offer(httpRequestRecord);
		httpRequestRecord.trimBinaryBody(HttpRequestRecordDto.BINARY_BODY_MAX_SIZE);
		if(publish && settings.publishRecords.get()){
			httpRequestRecordPublisherDao.put(httpRequestRecord);
		}
	}

}
