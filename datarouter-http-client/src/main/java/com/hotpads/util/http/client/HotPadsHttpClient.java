package com.hotpads.util.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.http.json.JsonSerializer;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.response.HotPadsHttpResponse;
import com.hotpads.util.http.response.exception.HotPadsHttpConnectionAbortedException;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestExecutionException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestFutureTimeoutException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestInterruptedException;
import com.hotpads.util.http.response.exception.HotPadsHttpResponseException;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;
import com.hotpads.util.http.security.ApiKeyPredicate;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpClient.class);
	private static final int DEFAULT_REQUEST_TIMEOUT_MS = 3000;
	private static final int EXTRA_FUTURE_TIME_MS = 1000;

	private HttpClient httpClient;
	private JsonSerializer jsonSerializer;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	private ExecutorService executor;
	private int requestTimeoutMs;
	private long futureTimeoutMs;

	HotPadsHttpClient(HttpClient httpClient, JsonSerializer jsonSerializer, SignatureValidator signatureValidator,
			CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate, HotPadsHttpClientConfig config,
			ExecutorService executor, Integer requestTimeoutMs, Long futureTimeoutMs, Integer retryCount) {
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
		this.config = config;
		this.executor = executor;
		this.requestTimeoutMs = requestTimeoutMs == null ? DEFAULT_REQUEST_TIMEOUT_MS : requestTimeoutMs.intValue();
		this.futureTimeoutMs = futureTimeoutMs == null ? getFutureTimeoutMs(this.requestTimeoutMs, retryCount)
				: futureTimeoutMs.longValue();
	}

	public HotPadsHttpResponse execute(HotPadsHttpRequest request) {
		try {
			return executeChecked(request);
		} catch (HotPadsHttpException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
	}

	public <E> E execute(HotPadsHttpRequest request, Type deserializeToType) {
		try {
			return executeChecked(request, deserializeToType);
		} catch (HotPadsHttpException e) {
			throw new HotPadsHttpRuntimeException(e);
		}
	}

	public <E> E executeChecked(HotPadsHttpRequest request, Type deserializeToType) throws HotPadsHttpException {
		String entity = executeChecked(request).getEntity();
		return jsonSerializer.deserialize(entity, deserializeToType);
	}

	public HotPadsHttpResponse executeChecked(HotPadsHttpRequest request) throws HotPadsHttpException {
		if (request.canHaveEntity() && request.getEntity() == null) {
			Map<String, String> params = new HashMap<>();
			if (csrfValidator != null) {
				params.put(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken());
			}
			if (apiKeyPredicate != null) {
				params.put(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
			}
			params = request.addPostParams(params).getPostParams();
			if (signatureValidator != null && !params.isEmpty()) {
				String signature = signatureValidator.getHexSignature(request.getPostParams());
				Map<String, String> signatureParam = Collections.singletonMap(SecurityParameters.SIGNATURE, signature);
				request.addPostParams(signatureParam);
			}
			request.setEntity(request.getPostParams());
		}

		HttpContext context = new BasicHttpContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());

		HotPadsHttpException ex;
		int timeoutMs = request.getTimeoutMs() == null ? this.requestTimeoutMs : request.getTimeoutMs().intValue();
		long futureTimeoutMs = request.getFutureTimeoutMs() == null ? this.futureTimeoutMs : request
				.getFutureTimeoutMs().longValue();
		HttpRequestBase internalHttpRequest = null;
		try {
			internalHttpRequest = request.getRequest();
			HttpRequestCallable requestCallable = new HttpRequestCallable(httpClient, internalHttpRequest, context);
			Future<HttpResponse> httpResponseFuture = executor.submit(requestCallable);
			HttpResponse httpResponse = httpResponseFuture.get(futureTimeoutMs, TimeUnit.MILLISECONDS);
			HotPadsHttpResponse response = new HotPadsHttpResponse(httpResponse);
			if (response.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
				throw new HotPadsHttpResponseException(response);
			}
			return response;
		} catch (TimeoutException e) {
			ex = new HotPadsHttpRequestFutureTimeoutException(e, timeoutMs);
		} catch (CancellationException | InterruptedException e) {
			ex = new HotPadsHttpRequestInterruptedException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				ex = new HotPadsHttpConnectionAbortedException(e);
			} else {
				ex = new HotPadsHttpRequestExecutionException(e);
			}
		}
		if (ex != null && internalHttpRequest != null) {
			forceAbortRequestUnchecked(internalHttpRequest);
		}
		throw ex;
	}
	
	private static void forceAbortRequestUnchecked(HttpRequestBase internalHttpRequest) {
		if (internalHttpRequest == null) {
			return;
		}
		try {
			internalHttpRequest.abort();
		} catch (Exception e) {
			logger.error("aborting internal http request failed", e);
		}
	}

	private static long getFutureTimeoutMs(int requestTimeoutMs, Integer retryCount) {
		/*
		 * we want the request future to time out after all of the individual request timeouts combined,
		 * so requestTimeout * (total number of requests + 2)
		 */
		int totalPossibleRequests = 1 + (retryCount == null ? 0 : retryCount);
		return requestTimeoutMs * totalPossibleRequests + EXTRA_FUTURE_TIME_MS;
	}

	private class HttpRequestCallable implements Callable<HttpResponse> {
		private HttpClient httpClient;
		private HttpUriRequest request;
		private HttpContext context;

		HttpRequestCallable(HttpClient httpClient, HttpUriRequest request, HttpContext context) {
			this.httpClient = httpClient;
			this.request = request;
			this.context = context;
		}

		@Override
		public HttpResponse call() throws IOException {
			return httpClient.execute(request, context);
		}
	}
	
	public <T> HotPadsHttpClient addDtoToPayload(HotPadsHttpRequest request, T dto, String dtoType) {
		String serializedDto = jsonSerializer.serialize(dto);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null) {
			if(dto instanceof Iterable){
				Iterable<?> dtos = (Iterable<?>) dto;
				dtoTypeNullSafe = dtos.iterator().hasNext() ? dtos.iterator().next().getClass().getCanonicalName() : "";
			}else{
				dtoTypeNullSafe = dto.getClass().getCanonicalName();
			}
		}
		HotPadsHttpClientConfig requestConfig = request.getRequestConfig(config);
		Map<String,String> params = new HashMap<>();
		params.put(requestConfig.getDtoParameterName(), serializedDto);
		params.put(requestConfig.getDtoTypeParameterName(), dtoTypeNullSafe);
		request.addPostParams(params);
		return this;
	}
}
