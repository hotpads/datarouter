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

	public static final String RETRY_SAFE_ATTRIBUTE = "retrySafe";
	private static final int DEFAULT_RETRY_COUNT = 2;

	private int retryCount;
	private boolean logOnRetry;

	public DatarouterHttpRetryHandler(){
		retryCount = DEFAULT_RETRY_COUNT;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context){
		if(logOnRetry){
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			logger.warn("Request {} failure Nº {}", clientContext.getRequest().getRequestLine(), executionCount,
					exception);
		}
		Object retrySafe = context.getAttribute(RETRY_SAFE_ATTRIBUTE);
		if(retrySafe == null || !(retrySafe instanceof Boolean) || !(Boolean)retrySafe || executionCount > retryCount){
			return false;
		}
		return true;
	}

	public int getRetryCount(){
		return retryCount;
	}

	public void setRetryCount(int retryCount){
		this.retryCount = retryCount;
	}

	public void setLogOnRetry(boolean logOnRetry){
		this.logOnRetry = logOnRetry;
	}

}
