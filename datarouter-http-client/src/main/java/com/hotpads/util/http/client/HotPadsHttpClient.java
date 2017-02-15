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
import java.util.function.Consumer;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.http.json.JsonSerializer;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
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
public class HotPadsHttpClient{
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpClient.class);
	private static final int DEFAULT_REQUEST_TIMEOUT_MS = 3000;
	private static final int EXTRA_FUTURE_TIME_MS = 1000;

	private final CloseableHttpClient httpClient;
	private final JsonSerializer jsonSerializer;
	private final SignatureValidator signatureValidator;
	private final CsrfValidator csrfValidator;
	private final ApiKeyPredicate apiKeyPredicate;
	private final HotPadsHttpClientConfig config;
	private final ExecutorService executor;
	private final int requestTimeoutMs;
	private final long futureTimeoutMs;
	private final PoolingHttpClientConnectionManager connectionManager;

	HotPadsHttpClient(CloseableHttpClient httpClient, JsonSerializer jsonSerializer,
			SignatureValidator signatureValidator, CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate,
			HotPadsHttpClientConfig config, ExecutorService executor, Integer requestTimeoutMs, Long futureTimeoutMs,
			Integer retryCount, PoolingHttpClientConnectionManager connectionManager){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
		this.config = config;
		this.executor = executor;
		this.connectionManager = connectionManager;
		this.requestTimeoutMs = requestTimeoutMs == null ? DEFAULT_REQUEST_TIMEOUT_MS : requestTimeoutMs.intValue();
		this.futureTimeoutMs = futureTimeoutMs == null ? getFutureTimeoutMs(this.requestTimeoutMs, retryCount)
				: futureTimeoutMs.longValue();
	}

	public HotPadsHttpResponse execute(HotPadsHttpRequest request){
		try{
			return executeChecked(request);
		}catch(HotPadsHttpException e){
			throw new HotPadsHttpRuntimeException(e);
		}
	}

	public HotPadsHttpResponse execute(HotPadsHttpRequest request, Consumer<HttpEntity> httpEntityConsumer){
		try{
			return executeChecked(request, httpEntityConsumer);
		}catch(HotPadsHttpException e){
			throw new HotPadsHttpRuntimeException(e);
		}
	}

	public <E> E execute(HotPadsHttpRequest request, Type deserializeToType){
		try{
			return executeChecked(request, deserializeToType);
		}catch(HotPadsHttpException e){
			throw new HotPadsHttpRuntimeException(e);
		}
	}

	public <E> E executeChecked(HotPadsHttpRequest request, Type deserializeToType) throws HotPadsHttpException{
		String entity = executeChecked(request).getEntity();
		return jsonSerializer.deserialize(entity, deserializeToType);
	}

	public HotPadsHttpResponse executeChecked(HotPadsHttpRequest request) throws HotPadsHttpException{
		return executeChecked(request, (Consumer<HttpEntity>)null);
	}

	public HotPadsHttpResponse executeChecked(HotPadsHttpRequest request, Consumer<HttpEntity> httpEntityConsumer)
	throws HotPadsHttpException{
		setSecurityProperties(request);

		HttpClientContext context = new HttpClientContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		CookieStore cookieStore = new BasicCookieStore();
		for(BasicClientCookie cookie : request.getCookies()){
			cookieStore.addCookie(cookie);
		}
		context.setCookieStore(cookieStore);

		HotPadsHttpException ex;
		int timeoutMs = request.getTimeoutMs() == null ? this.requestTimeoutMs : request.getTimeoutMs().intValue();
		long futureTimeoutMs = request.getFutureTimeoutMs() == null ? this.futureTimeoutMs
				: request.getFutureTimeoutMs().longValue();
		HttpRequestBase internalHttpRequest = null;
		HttpRequestCallable requestCallable = null;
		try{
			internalHttpRequest = request.getRequest();
			requestCallable = new HttpRequestCallable(httpClient, internalHttpRequest, context);
			Future<HttpResponse> httpResponseFuture = executor.submit(requestCallable);
			HttpResponse httpResponse = httpResponseFuture.get(futureTimeoutMs, TimeUnit.MILLISECONDS);
			HotPadsHttpResponse response = new HotPadsHttpResponse(httpResponse, context, httpEntityConsumer);
			if(response.getStatusCode() >= HttpStatus.SC_BAD_REQUEST){
				throw new HotPadsHttpResponseException(response, requestCallable.getRequestStartTimeMs());
			}
			return response;
		}catch(TimeoutException e){
			ex = new HotPadsHttpRequestFutureTimeoutException(e, timeoutMs);
		}catch(CancellationException | InterruptedException e){
			ex = new HotPadsHttpRequestInterruptedException(e, requestCallable.getRequestStartTimeMs());
		}catch(ExecutionException e){
			if(e.getCause() instanceof IOException){
				ex = new HotPadsHttpConnectionAbortedException(e, requestCallable.getRequestStartTimeMs());
			}else{
				ex = new HotPadsHttpRequestExecutionException(e, requestCallable.getRequestStartTimeMs());
			}
		}
		if(internalHttpRequest != null){
			forceAbortRequestUnchecked(internalHttpRequest);
		}
		throw ex;
	}

	private void setSecurityProperties(HotPadsHttpRequest request){
		Map<String,String> params = new HashMap<>();
		if(csrfValidator != null){
			String csrfIv = CsrfValidator.generateCsrfIv();
			params.put(SecurityParameters.CSRF_IV, csrfIv);
			params.put(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken(csrfIv));
		}
		if(apiKeyPredicate != null){
			params.put(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
		}
		Map<String,String> signatureParam;
		if(request.canHaveEntity() && request.getEntity() == null){
			params = request.addPostParams(params).getPostParams();
			if(signatureValidator != null && !params.isEmpty()){
				String signature = signatureValidator.getHexSignature(request.getPostParams());
				signatureParam = Collections.singletonMap(SecurityParameters.SIGNATURE, signature);
				request.addPostParams(signatureParam);
			}
			request.setEntity(request.getPostParams());
		}else if(request.getMethod() == HttpRequestMethod.GET){
			params = request.addGetParams(params).getGetParams();
			if(signatureValidator != null && !params.isEmpty()){
				String signature = signatureValidator.getHexSignature(request.getGetParams());
				signatureParam = Collections.singletonMap(SecurityParameters.SIGNATURE, signature);
				request.addGetParams(signatureParam);
			}
		}
	}

	public void shutdown(){
		executor.shutdownNow();
		try{
			httpClient.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private static void forceAbortRequestUnchecked(HttpRequestBase internalHttpRequest){
		try{
			internalHttpRequest.abort();
		}catch(Exception e){
			logger.error("aborting internal http request failed", e);
		}
	}

	private static long getFutureTimeoutMs(int requestTimeoutMs, Integer retryCount){
		/* we want the request future to time out after all of the individual request timeouts combined, so
		 * requestTimeout * (total number of requests + 2) */
		int totalPossibleRequests = 1 + (retryCount == null ? 0 : retryCount);
		return requestTimeoutMs * totalPossibleRequests + EXTRA_FUTURE_TIME_MS;
	}

	private static class HttpRequestCallable implements Callable<HttpResponse>{
		private final HttpClient httpClient;
		private final HttpUriRequest request;
		private final HttpContext context;
		private long requestStartTimeMs;

		HttpRequestCallable(HttpClient httpClient, HttpUriRequest request, HttpContext context){
			this.httpClient = httpClient;
			this.request = request;
			this.context = context;
		}

		@Override
		public HttpResponse call() throws IOException{
			requestStartTimeMs = System.currentTimeMillis();
			return httpClient.execute(request, context);
		}

		public long getRequestStartTimeMs(){
			return requestStartTimeMs;
		}
	}

	public HotPadsHttpClient addDtoToPayload(HotPadsHttpRequest request, Object dto, String dtoType){
		String serializedDto = jsonSerializer.serialize(dto);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null){
			if(dto instanceof Iterable){
				Iterable<?> dtos = (Iterable<?>)dto;
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

	public HotPadsHttpClient setEntityDto(HotPadsHttpRequest request, Object dto){
		String serializedDto = jsonSerializer.serialize(dto);
		request.setEntity(serializedDto, ContentType.APPLICATION_JSON);
		return this;
	}

	public PoolStats getPoolStats(){
		return connectionManager.getTotalStats();
	}

	public CloseableHttpClient getApacheHttpClient(){
		return httpClient;
	}

}
