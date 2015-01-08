package com.hotpads.util.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
import com.hotpads.util.http.response.exception.HotPadsHttpRequestInterruptedException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestTimeoutException;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;
import com.hotpads.util.http.security.ApiKeyPredicate;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpClient.class);
	private static final int DEFAULT_REQUEST_TIMEOUT_MS = 3000;

	private HttpClient httpClient;
	private JsonSerializer jsonSerializer;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	private ExecutorService executor;
	private int requestTimeoutMs;
	private Integer retryCount;

	HotPadsHttpClient(HttpClient httpClient, JsonSerializer jsonSerializer, SignatureValidator signatureValidator,
			CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate, HotPadsHttpClientConfig config,
			ExecutorService executor, Integer requestTimeoutMs, Integer retryCount) {
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
		this.config = config;
		this.executor = executor;
		this.requestTimeoutMs = requestTimeoutMs == null ? DEFAULT_REQUEST_TIMEOUT_MS : requestTimeoutMs.intValue();
		this.retryCount = retryCount;
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
		int timeoutMs = request.getTimeoutMs() == null ? requestTimeoutMs : request.getTimeoutMs().intValue();
		long futureTimeoutMs = request.getFutureTimeoutMs() == null ? getFutureTimeoutMs(timeoutMs, retryCount)
				: request.getFutureTimeoutMs().intValue();
		try {
			HttpRequestCallable callable = new HttpRequestCallable(httpClient, request.getRequest(), context);
			HttpResponse httpResponse = executor.submit(callable).get(futureTimeoutMs, TimeUnit.MILLISECONDS);
			return new HotPadsHttpResponse(httpResponse);
		} catch (TimeoutException e) {
			ex = new HotPadsHttpRequestTimeoutException(e, timeoutMs);
		} catch (CancellationException | InterruptedException e) {
			ex = new HotPadsHttpRequestInterruptedException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException) { // NOTE not always the case of an HTTP exception
				ex = new HotPadsHttpConnectionAbortedException(e);
			} else {
				ex = new HotPadsHttpRequestExecutionException(e);
			}
		}
		throw ex;
	}

	private static long getFutureTimeoutMs(int requestTimeoutMs, Integer retryCount) {
		/*
		 * we want the request future to time out after all of the individual request timeouts combined,
		 * so requestTimeout * (total number of requests + 2)
		 */
		int totalPossibleRequests = (retryCount == null ? 0 : retryCount) + 2;
		return requestTimeoutMs * totalPossibleRequests;
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

	public <T> HotPadsHttpClient addDtosToPayload(HotPadsHttpRequest request, Collection<T> dtos, String dtoType) {
		String serializedDtos = jsonSerializer.serialize(dtos);
		String dtoTypeNullSafe = dtoType;
		if (dtoType == null) {
			dtoTypeNullSafe = dtos.isEmpty() ? "" : dtos.iterator().next().getClass().getCanonicalName();
		}
		Map<String, String> params = new HashMap<>();
		params.put(config.getDtoParameterName(), serializedDtos);
		params.put(config.getDtoTypeParameterName(), dtoTypeNullSafe);
		request.addPostParams(params);
		return this;
	}
}
