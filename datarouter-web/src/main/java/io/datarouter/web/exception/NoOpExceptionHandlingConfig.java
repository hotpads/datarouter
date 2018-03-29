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
package io.datarouter.web.exception;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.monitoring.exception.ExceptionDto;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSessionManager;

@Singleton
public class NoOpExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception){
		return DatarouterSessionManager
			.getFromRequest(request)
			.map(DatarouterUserRole::isSessionAdmin)
			.orElse(false);
	}

	@Override
	public boolean shouldReportError(ExceptionRecord exceptionRecord){
		return false;
	}

	@Override
	public boolean shouldReportError(ExceptionDto dto){
		return false;
	}

	@Override
	public String getHtmlErrorMessage(Exception exception){
		return "Error";
	}

	@Override
	public String getServerName(){
		return null;
	}

	@Override
	public boolean isDevServer(){
		return false;
	}

	@Override
	public String getRecipientEmail(){
		return null;
	}

}
