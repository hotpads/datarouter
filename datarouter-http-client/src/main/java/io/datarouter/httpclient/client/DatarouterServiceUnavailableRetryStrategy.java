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

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.HttpStatusCode;
import io.datarouter.instrumentation.trace.TracerTool;

public class DatarouterServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterServiceUnavailableRetryStrategy.class);

	private static final Set<Integer> STATUS_CODES_TO_RETRY = Stream.of(
			HttpStatusCode.SC_502_BAD_GATEWAY,
			HttpStatusCode.SC_503_SERVICE_UNAVAILABLE,
			HttpStatusCode.SC_504_GATEWAY_TIMEOUT)
			.map(HttpStatusCode::getStatusCode)
			.collect(Collectors.toSet());

	private final Supplier<Integer> retryCount;

	public DatarouterServiceUnavailableRetryStrategy(Supplier<Integer> retryCount){
		this.retryCount = retryCount;
	}

	@Override
	public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context){
		int statusCode = response.getStatusLine().getStatusCode();
		if(!STATUS_CODES_TO_RETRY.contains(statusCode)){
			return false;
		}
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		boolean willRetry = HttpRetryTool.shouldRetry(context, executionCount, retryCount);
		String traceparent = (String)context.getAttribute(DatarouterHttpCallTool.TRACEPARENT);
		String url = clientContext.getTargetHost() + clientContext.getRequest().getRequestLine().getUri();
		if(willRetry){
			HttpEntity httpEntity = response.getEntity();
			String entity = HttpRetryTool.tryEntityToString(httpEntity);
			logger.warn("failure target={} traceparent={} failureCount={} statusCode={} entity={}", url, traceparent,
					executionCount, statusCode, entity);
			TracerTool.appendToSpanInfo("willRetry", statusCode);
		}else{
			// don't log everything, caller will get details in an Exception
			logger.warn("failure target={} traceparent={} failureCount={} statusCode={} (final)", url, traceparent,
					executionCount, statusCode);
		}
		return willRetry;
	}

	@Override
	public long getRetryInterval(){
		return 0;
	}

}
