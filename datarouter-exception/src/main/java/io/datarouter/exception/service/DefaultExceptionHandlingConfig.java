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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.auth.session.DatarouterSessionManager;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import jakarta.inject.Inject;

public class DefaultExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Inject
	private DatarouterExceptionSettingRoot settings;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private ExceptionRecordService exceptionRecordService;

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Throwable exception){
		return !settings.forceHideStackTrace.get() && canViewStackTrace(request);
	}

	protected boolean canViewStackTrace(HttpServletRequest request){
		return isDevServer() || DatarouterSessionManager.getFromRequest(request)
				.map(DatarouterSession::getRoles)
				.orElse(Set.of())
				.contains(DatarouterUserRoleRegistry.DATAROUTER_MONITORING);
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
