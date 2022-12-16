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
package io.datarouter.web.exception;

import javax.servlet.http.HttpServletRequest;

public interface ExceptionHandlingConfig{

	boolean shouldDisplayStackTrace(HttpServletRequest request, Throwable exception);

	String getHtmlErrorMessage(Exception exception);
	boolean isDevServer();

	String buildExceptionLinkForCurrentServer(String exceptionRecordId);

	static class NoOpExceptionHandlingConfig implements ExceptionHandlingConfig{

		@Override
		public boolean shouldDisplayStackTrace(HttpServletRequest request, Throwable exception){
			return false;
		}

		@Override
		public String getHtmlErrorMessage(Exception exception){
			return null;
		}

		@Override
		public boolean isDevServer(){
			return false;
		}

		@Override
		public String buildExceptionLinkForCurrentServer(String exceptionRecordId){
			return null;
		}

	}

}
