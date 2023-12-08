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
package io.datarouter.httpclient.client;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.instrumentation.trace.TracerTool;

public class DatarouterHttpRetryHandler implements HttpRequestRetryHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpRetryHandler.class);

	private final Supplier<Integer> retryCount;

	public DatarouterHttpRetryHandler(Supplier<Integer> retryCount){
		this.retryCount = retryCount;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context){
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		boolean willRetry = HttpRetryTool.shouldRetry(context, executionCount, retryCount);
		String traceparent = (String)context.getAttribute(DatarouterHttpClientIoExceptionCircuitBreaker.TRACEPARENT);
		String url = clientContext.getTargetHost() + clientContext.getRequest().getRequestLine().getUri();
		if(willRetry){
			logger.warn("failure target={} traceparent={} failureCount={}",
					url,
					traceparent,
					executionCount,
					exception);
			TracerTool.appendToSpanInfo("willRetry", exception.getMessage());
		}else{
			// don't log everything, caller will get details in an Exception
			logger.warn("failure target={} traceparent={} failureCount={} (final) {}",
					url,
					traceparent,
					executionCount,
					exception);
		}
		return willRetry;
	}

}
