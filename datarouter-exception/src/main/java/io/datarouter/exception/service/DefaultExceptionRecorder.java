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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.session.CurrentSessionInfo;
import io.datarouter.auth.session.Session;
import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.exception.utils.ExceptionDetailsDetector;
import io.datarouter.exception.utils.ExceptionDetailsDetector.ExceptionRecorderDetails;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationConstants.ExceptionInstrumentationConstants;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.exception.ExceptionCategory;
import io.datarouter.storage.exception.UnknownExceptionCategory;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.trace.conveyor.DatarouterDebuggingBuffers;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.exception.ExceptionCounters;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.WebExceptionCategory;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.monitoring.GitProperties;
import io.datarouter.web.monitoring.exception.ExceptionAndHttpRequestDto;
import io.datarouter.web.util.ExceptionTool;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.IpAddressService;
import jakarta.inject.Inject;

public class DefaultExceptionRecorder implements ExceptionRecorder{
	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionRecorder.class);

	@Inject
	private GitProperties gitProperties;
	@Inject
	private ExceptionDetailsDetector exceptionDetailsDetector;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettingRoot;
	@Inject
	private CurrentSessionInfo currentSessionInfo;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterExceptionSettingRoot settings;
	@Inject
	private DatarouterDebuggingBuffers debuggingBuffers;
	@Inject
	private ServerName serverName;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private IpAddressService ipAddressService;
	@Inject
	private ServerTypeDetector detector;

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
		return recordException(
				category,
				location,
				methodName,
				name,
				type,
				lineNumber,
				callOrigin,
				ExceptionTool.getStackTraceAsString(exception),
				additionalEmailRecipients);
	}

	private ExceptionRecordDto recordException(
			ExceptionCategory category,
			String location,
			String methodName,
			String name,
			String type,
			Integer lineNumber,
			String callOrigin,
			String stackTrace,
			List<String> additionalEmailRecipients){
		if(callOrigin == null){
			callOrigin = location;
		}
		ExceptionCounters.inc(category.name());
		ExceptionCounters.inc(category.name() + " " + webappName);
		ExceptionCounters.inc("name " + name);
		ExceptionCounters.inc(type);
		ExceptionCounters.inc(callOrigin);
		ExceptionCounters.inc(category.name() + " origin " + callOrigin);
		ExceptionCounters.inc(type + " " + callOrigin);
		var exceptionRecord = new ExceptionRecord(
				ExceptionRecordKey.generate(),
				System.currentTimeMillis(),
				serviceName.get(),
				serverName.get(),
				category.name(),
				Optional.ofNullable(name).orElse(ExceptionRecorderDetails.getDefaultName(type, name, callOrigin)),
				stackTrace,
				type,
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				location,
				methodName,
				lineNumber,
				callOrigin,
				additionalEmailRecipients,
				environmentName.get());
		exceptionRecord.trimFields();
		if(settings.publishRecords.get()){
			debuggingBuffers.exceptions.offer(exceptionRecord.toDto());
		}
		logger.warn("Exception recorded with id {}", exceptionRecord.getKey().getId());
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

	@Override
	public ExceptionRecordDto recordExceptionAndHttpRequest(ExceptionAndHttpRequestDto exceptionDto,
			ExceptionCategory category){
		ExceptionRecordDto exceptionRecordDto = recordException(
				category,
				exceptionDto.errorLocation,
				exceptionDto.methodName,
				exceptionDto.name,
				exceptionDto.errorClass,
				exceptionDto.lineNumber,
				exceptionDto.callOrigin,
				exceptionDto.stackTrace,
				List.of());
		recordHttpRequest(exceptionDto, exceptionRecordDto, true);
		return exceptionRecordDto;
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

		boolean omitPayload = RequestAttributeTool.get(request, Dispatcher.TRANSMITS_PII)
					.orElse(false);
		String ip = ipAddressService.getIpAddress(request);
		var httpRequestRecord = new HttpRequestRecord(
				exceptionRecord == null ? null : exceptionRecord.id(),
				RequestAttributeTool.get(request, BaseHandler.TRACE_CONTEXT),
				request,
				userRoles,
				userToken.orElse(null),
				omitPayload,
				ip);
		saveAndPublishHttpRequest(httpRequestRecord, publish);
	}

	private void recordHttpRequest(
			ExceptionAndHttpRequestDto exceptionDto,
			ExceptionRecordDto exceptionRecord,
			boolean publish){
		HttpRequestRecord httpRequestRecord = new HttpRequestRecord(exceptionDto, exceptionRecord.id());
		saveAndPublishHttpRequest(httpRequestRecord, publish);
	}

	private void saveAndPublishHttpRequest(HttpRequestRecord httpRequestRecord, boolean publish){
		httpRequestRecord.trimFields();
		httpRequestRecord.trimBinaryBody(ExceptionInstrumentationConstants.MAX_SIZE_BINARY_BODY);
		if(publish && settings.publishRecords.get()){
			debuggingBuffers.httpRequests.offer(httpRequestRecord.toDto());
		}
	}

	public Boolean publishToSharedNonProdQueue(){
		return !detector.mightBeProduction() && settings.publishNonProdDataToSharedQueue.get();
	}

}
