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
package io.datarouter.httpclient.client;

import java.io.IOException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatarouterHttpRetryHandler implements HttpRequestRetryHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpRetryHandler.class);

	private final int retryCount;

	public DatarouterHttpRetryHandler(int retryCount){
		this.retryCount = retryCount;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context){
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		boolean willRetry = HttpRetryTool.shouldRetry(context, executionCount, retryCount);
		String requestId = (String)context.getAttribute(StandardDatarouterHttpClient.X_REQUEST_ID);
		if(willRetry){
			logger.warn("Request {} id={} failure Nº {}", clientContext.getRequest().getRequestLine(), requestId,
					executionCount, exception);
		}else{
			// don't log everything, caller will get details in an Exception
			logger.warn("Request {} id={} failure Nº {} (final)", clientContext.getRequest().getRequestLine(),
					requestId, executionCount);
		}
		return willRetry;
	}

}
