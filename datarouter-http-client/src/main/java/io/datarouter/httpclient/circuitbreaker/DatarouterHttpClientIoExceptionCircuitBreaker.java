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
package io.datarouter.httpclient.circuitbreaker;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.client.StandardDatarouterHttpClient;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpCircuitBreakerException;
import io.datarouter.httpclient.response.exception.DatarouterHttpConnectionAbortedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRequestInterruptedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;

public class DatarouterHttpClientIoExceptionCircuitBreaker extends ExceptionCircuitBreaker{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpExceptionCircuitBreaker.class);

	private static final Duration LOG_SLOW_REQUEST_THRESHOLD = Duration.ofSeconds(10);

	public DatarouterHttpClientIoExceptionCircuitBreaker(String name){
		super(name);
	}

	public DatarouterHttpResponse call(CloseableHttpClient httpClient, DatarouterHttpRequest request,
			Consumer<HttpEntity> consumer, HttpClientContext context, Supplier<Boolean> enableBreakers)
	throws DatarouterHttpException{
		CircuitBreakerState state = getState();
		if(state == CircuitBreakerState.OPEN && enableBreakers.get()){
			incrementCounterOnStateChange("open");
			logger.error("Circuit opened. CircuitName={}", name);
			throw new DatarouterHttpCircuitBreakerException(name);
		}

		DatarouterHttpException ex;
		HttpRequestBase internalHttpRequest = null;
		String requestId = UUID.randomUUID().toString();
		long requestStartTimeMs = System.currentTimeMillis();
		try{
			TracerTool.startSpan(TracerThreadLocal.get(), "http call " + request.getPath());
			internalHttpRequest = request.getRequest();
			internalHttpRequest.addHeader(StandardDatarouterHttpClient.X_REQUEST_ID, requestId);
			context.setAttribute(StandardDatarouterHttpClient.X_REQUEST_ID, requestId);
			requestStartTimeMs = System.currentTimeMillis();
			HttpResponse httpResponse = httpClient.execute(internalHttpRequest, context);
			Duration duration = Duration.ofMillis(System.currentTimeMillis() - requestStartTimeMs);
			if(duration.compareTo(LOG_SLOW_REQUEST_THRESHOLD) > 0){
				logger.warn("Slow request target={} duration={}", request.getPath(), duration);
			}
			DatarouterHttpResponse response = new DatarouterHttpResponse(httpResponse, context, consumer);
			if(response.getStatusCode() >= HttpStatus.SC_BAD_REQUEST){
				callResultQueue.insertResult(false);
				throw new DatarouterHttpResponseException(response, duration);
			}

			if(state == CircuitBreakerState.HALF_OPEN){
				callResultQueue.reset();
				incrementCounterOnStateChange("closing");
				logger.error("Half opened circuit now closing. CircuitName={}",name);
			}
			callResultQueue.insertResult(true);
			return response;
		}catch(IOException e){
			callResultQueue.insertResult(false);
			ex = new DatarouterHttpConnectionAbortedException(e, requestStartTimeMs, requestId);
		}catch(CancellationException e){
			callResultQueue.insertResult(false);
			ex = new DatarouterHttpRequestInterruptedException(e, requestStartTimeMs, requestId);
		}finally{
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
		if(internalHttpRequest != null){
			forceAbortRequestUnchecked(internalHttpRequest);
		}
		throw ex;
	}

	private static void forceAbortRequestUnchecked(HttpRequestBase internalHttpRequest){
		try{
			internalHttpRequest.abort();
		}catch(Exception e){
			logger.error("aborting internal http request failed", e);
		}
	}

}