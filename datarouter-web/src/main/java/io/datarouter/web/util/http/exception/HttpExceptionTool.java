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
package io.datarouter.web.util.http.exception;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import io.datarouter.httpclient.response.exception.DocumentedServerError;
import io.datarouter.web.handler.documentation.HttpDocumentedExceptionTool;

public class HttpExceptionTool{

	public static int getHttpStatusCodeForException(HttpServletResponse response, Throwable exception){
		int httpStatusCode;

		Optional<DocumentedServerError> optDoc = HttpDocumentedExceptionTool
				.findDocumentationInChain(exception);
		if(optDoc.isPresent()){
			httpStatusCode = optDoc.get().getStatusCode();
		}else if(exception instanceof HttpException){
			httpStatusCode = ((HttpException)exception).getHttpResponseCode();
		}else{
			httpStatusCode = response.getStatus();
		}
		if(httpStatusCode == HttpServletResponse.SC_OK){
			return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		return httpStatusCode;
	}

}
