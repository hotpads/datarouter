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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.storage.metadata.DatarouterExceptionRecordSummaryMetadataDao;
import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadata;
import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadataKey;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.monitoring.exception.ExceptionDto;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.user.session.service.RoleManager;

public class DefaultExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Inject
	private DatarouterExceptionSettingRoot settings;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private DatarouterExceptionRecordSummaryMetadataDao exceptionSummaryMetadataDao;
	@Inject
	private RoleManager roleManager;
	@Inject
	private ExceptionRecordService exceptionRecordService;

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Throwable exception){
		return !settings.forceHideStackTrace.get() && isInternalUser(request);
	}

	protected boolean isInternalUser(HttpServletRequest request){
		return isDevServer() || DatarouterSessionManager.getFromRequest(request)
				.map(DatarouterSession::getRoles)
				.map(roleManager::isAdmin)
				.orElse(false);
	}

	@Override
	public boolean shouldReportError(ExceptionRecordDto exceptionRecord){
		if(!settings.shouldReport.get()){
			return false;
		}
		var metadataKey = new ExceptionRecordSummaryMetadataKey(exceptionRecord.type,
				exceptionRecord.exceptionLocation);
		ExceptionRecordSummaryMetadata recordMetadata = exceptionSummaryMetadataDao.get(metadataKey);
		return recordMetadata == null || recordMetadata.getMuted() == null || !recordMetadata.getMuted();
	}

	@Override
	public boolean shouldReportError(ExceptionDto dto){
		if(!settings.shouldReport.get()){
			return false;
		}
		return true;
	}

	@Override
	public String getHtmlErrorMessage(Exception exception){
		return "Error";
	}

	@Override
	public boolean isDevServer(){
		return serverTypeDetector.mightBeDevelopment();
	}

	@Override
	public String buildExceptionLinkForCurrentServer(String exceptionRecordId){
		return exceptionRecordService.buildExceptionLinkForCurrentServer(exceptionRecordId);
	}

}
