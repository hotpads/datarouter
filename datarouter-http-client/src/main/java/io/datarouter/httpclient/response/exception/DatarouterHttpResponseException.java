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
package io.datarouter.httpclient.response.exception;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import io.datarouter.httpclient.response.DatarouterHttpResponse;

@SuppressWarnings("serial")
public class DatarouterHttpResponseException extends DatarouterHttpException{

	public static final String X_EXCEPTION_ID = "x-eid";

	private final DatarouterHttpResponse response;

	public DatarouterHttpResponseException(DatarouterHttpResponse response, long requestStartTimeMs){
		super(buildMessage(response, requestStartTimeMs), null);
		this.response = response;
	}

	private static String buildMessage(DatarouterHttpResponse response, long requestStartTimeMs){
		String message = "HTTP response returned with status code " + response.getStatusCode();
		Header header = response.getFirstHeader(X_EXCEPTION_ID);
		if(header != null){
			message += " and exception id " + header.getValue();
		}
		message += " after " + (System.currentTimeMillis() - requestStartTimeMs) + "ms";
		message += " with entity:\n" + response.getEntity();
		return message;
	}

	public DatarouterHttpResponse getResponse(){
		return response;
	}

	/**
	 * 4XX status code. Issue exists in the client or request.
	 */
	public boolean isClientError(){
		int statusCode = response.getStatusCode();
		return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	/**
	 * 5XX status code. Issue exists on the server.
	 */
	public boolean isServerError(){
		int statusCode = response.getStatusCode();
		return statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}
}