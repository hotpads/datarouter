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
package io.datarouter.httpclient;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;

/**
 * Holds http request and resulting response/exception
 */
public class DatarouterHttpContext{

	public final DatarouterHttpRequest request;
	public final DatarouterHttpResponse response;
	public final DatarouterHttpException exception;

	public DatarouterHttpContext(
			DatarouterHttpRequest request,
			DatarouterHttpResponse response,
			DatarouterHttpException exception){
		this.request = request;
		this.exception = exception;
		if(exception instanceof DatarouterHttpResponseException){
			this.response = ((DatarouterHttpResponseException)exception).getResponse();
		}else{
			this.response = response;
		}
	}

}
