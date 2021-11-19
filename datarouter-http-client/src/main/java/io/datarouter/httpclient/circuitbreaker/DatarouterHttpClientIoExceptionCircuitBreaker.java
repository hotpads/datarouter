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
package io.datarouter.httpclient.circuitbreaker;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpCircuitBreakerException;
import io.datarouter.httpclient.response.exception.DatarouterHttpConnectionAbortedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRequestInterruptedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.W3TraceContext;

public class DatarouterHttpClientIoExceptionCircuitBreaker extends ExceptionCircuitBreaker{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpClientIoExceptionCircuitBreaker.class);

	private static final Duration LOG_SLOW_REQUEST_THRESHOLD = Duration.ofSeconds(2);

	public static final String TRACEPARENT = "traceparent";
	public static final String TRACESTATE = "tracestate";

	public DatarouterHttpClientIoExceptionCircuitBreaker(String name){
		super(name);
	}

	public DatarouterHttpResponse call(
			CloseableHttpClient httpClient,
			DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer,
			HttpClientContext context,
			Supplier<Boolean> enableBreakers,
			Supplier<Boolean> traceInQueryString,
			Supplier<Boolean> debugLog)
	throws DatarouterHttpException{
		CircuitBreakerState state = getState();
		if(state == CircuitBreakerState.OPEN && enableBreakers.get()){
			incrementCounterOnStateChange("open");
			throw new DatarouterHttpCircuitBreakerException(name, callResultQueue.getOriginalException());
		}

		DatarouterHttpException ex;
		Tracer tracer = TracerThreadLocal.get();
		TracerTool.startSpan(tracer, "http call " + request.getPath(), TraceSpanGroupType.HTTP);
		W3TraceContext traceContext;
		if(tracer != null && tracer.getTraceContext().isPresent()){
			traceContext = tracer.getTraceContext().get().copy();
			traceContext.updateParentIdAndAddTracestateMember();
		}else{
			count("traceContext null");
			traceContext = new W3TraceContext(Trace2Dto.getCurrentTimeInNs());
		}
		String traceparent = traceContext.getTraceparent().toString();
		if(traceInQueryString.get()){
			request.addGetParam(TRACEPARENT, traceparent);
		}
		HttpRequestBase internalHttpRequest = request.getRequest();
		count("request");
		logger.debug("traceparent={} passing to request={}", traceparent, request.getPath());
		internalHttpRequest.addHeader(TRACEPARENT, traceparent);
		internalHttpRequest.addHeader(TRACESTATE, traceContext.getTracestate().toString());
		context.setAttribute(TRACEPARENT, traceContext.getTraceparent().toString());
		if(debugLog.get()){
			logger.warn("sending http request url={}", internalHttpRequest.getURI());
		}
		long requestStartTimeNs = Trace2Dto.getCurrentTimeInNs();
		try{
			HttpResponse httpResponse = httpClient.execute(internalHttpRequest, context);
			Duration duration = Duration.ofNanos(Trace2Dto.getCurrentTimeInNs() - requestStartTimeNs);
			String entity = null;
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			count("response " + statusCode);
			boolean isBadStatusCode = statusCode >= HttpStatus.SC_BAD_REQUEST;
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				// skip the httpEntityConsumer in case of error because we are going to close the input stream
				if(httpEntityConsumer != null && !isBadStatusCode){
					httpEntityConsumer.accept(httpEntity);
				}else{
					entity = EntityUtils.toString(httpEntity);
				}
			}
			Optional<Traceparent> remoteTraceparent = Optional.ofNullable(httpResponse.getFirstHeader(TRACEPARENT))
					.map(Header::getValue)
					.map(Traceparent::parse)
					.filter(Optional::isPresent)
					.map(Optional::get);
			remoteTraceparent.ifPresent(tp -> TracerTool.appendToSpanInfo("remote parentId", tp.parentId));
			if(remoteTraceparent.isPresent() && remoteTraceparent.get().shouldSample()){
				// if remote server has forced sample for trace, we also force sample the client's trace
				TracerTool.setForceSample();
			}
			if(duration.compareTo(LOG_SLOW_REQUEST_THRESHOLD) > 0){
				logger.warn("Slow request target={} durationS={} remoteTraceparent={}", request.getPath(),
						duration.getSeconds(), remoteTraceparent.orElse(null));
			}
			DatarouterHttpResponse response = new DatarouterHttpResponse(httpResponse, context, statusCode, entity);
			if(isBadStatusCode){
				TracerTool.appendToSpanInfo("bad status code", statusCode);
				ex = new DatarouterHttpResponseException(response, duration, traceparent, request.getPath());
				callResultQueue.insertFalseResultWithException(ex);
				// no need to abort the connection, we received a response line, the connection is probably still good
				response.tryClose();
				throw ex;
			}

			if(state == CircuitBreakerState.HALF_OPEN){
				callResultQueue.reset();
				incrementCounterOnStateChange("closing");
				logger.error("Half opened circuit now closing. CircuitName={}",name);
			}
			callResultQueue.insertTrueResult();
			return response;
		}catch(IOException e){
			count("IOException");
			TracerTool.appendToSpanInfo("exception", e.getMessage());
			ex = new DatarouterHttpConnectionAbortedException(e, TimeUnit.NANOSECONDS.toMillis(requestStartTimeNs),
					traceparent, request.getPath());
			callResultQueue.insertFalseResultWithException(ex);
		}catch(CancellationException e){
			count("CancellationException");
			TracerTool.appendToSpanInfo("exception", e.getMessage());
			ex = new DatarouterHttpRequestInterruptedException(e, TimeUnit.NANOSECONDS.toMillis(requestStartTimeNs),
					traceparent, request.getPath());
			callResultQueue.insertFalseResultWithException(ex);
		}finally{
			TracerTool.finishSpan();
		}
		// connection might have gone bad, destroying it
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

	private void count(String key){
		Counters.inc("httpClient " + name + " " + key);
	}

}
