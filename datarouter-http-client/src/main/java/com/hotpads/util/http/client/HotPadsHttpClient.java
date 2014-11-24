package com.hotpads.util.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SecurityParameters;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient {
	private static final int DEFAULT_REQUEST_TIMEOUT_MS = 3000;
	private static final ProtocolVersion PROTOCOL = new ProtocolVersion("HTTP", 1, 1);
	
	private HttpClient httpClient;
	private JsonSerializer jsonSerializer;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	private ExecutorService executor;
	private int requestTimeoutMs;
	
	HotPadsHttpClient(HttpClient httpClient, JsonSerializer jsonSerializer, SignatureValidator signatureValidator,
			CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate, HotPadsHttpClientConfig config, ExecutorService executor,
			Integer requestTimeoutMs) {
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
		this.config = config;
		this.executor = executor;
		this.requestTimeoutMs = requestTimeoutMs == null ? DEFAULT_REQUEST_TIMEOUT_MS : requestTimeoutMs.intValue();
	}
	
	public String execute(HotPadsHttpRequest request) {
		if (request.canHaveEntity() && request.getEntity() == null) {
			Map<String,String> params = new HashMap<>();
			if (csrfValidator != null) {
				params.put(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken());
			}
			if (apiKeyPredicate != null) {
				params.put(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
			}
			request.addPostParams(params);
			if (signatureValidator != null) {
				byte[] signature = signatureValidator.sign(request.getPostParams());
				Map<String, String> signatureParam = Collections.singletonMap(SecurityParameters.SIGNATURE,
						Base64.encodeBase64String(signature));
				request.addPostParams(signatureParam);
			}
			request.setEntity(request.getPostParams());
		}
		
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		
		try {
			HttpRequestCallable callable = new HttpRequestCallable(httpClient, request.getRequest(), context);
			return executor.submit(callable).get(requestTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			if(e.getCause() != null) {
				throw new RuntimeException(e.getCause());
			}
			
			throw new HotPadsHttpClientException(e, request.getRequest(), null);
		} catch (InterruptedException | ExecutionException e) {
			// TODO exceptions that are not obscured by HotPadsHttpClientException or RuntimeException
//			if(e.getCause() instanceof HotPadsHttpClientException) {
//				throw (HotPadsHttpClientException) e.getCause();
//			}
			throw new RuntimeException(e);
		}
	}
	
	private class HttpRequestCallable implements Callable<String> {
		private HttpClient httpClient;
		private HttpUriRequest request;
		private HttpContext context;
		
		HttpRequestCallable(HttpClient httpClient, HttpUriRequest request, HttpContext context) {
			this.httpClient = httpClient;
			this.request = request;
			this.context = context;
		}
		
		@Override
		public String call() throws Exception {
			HttpResponse response = httpClient.execute(request, context);
			if(response == null) {
				throw new HotPadsHttpClientException(null, request, response);
			}
			if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_MOVED_PERMANENTLY) {
				throw new HotPadsHttpClientException(null, request, response);
			}
			return getResponseEntity(response);
		}
	}
	
	public <E> E execute(HotPadsHttpRequest request, Type deserializeToType) {
		return jsonSerializer.deserialize(execute(request), deserializeToType);
	}
	
	public <T> HotPadsHttpClient addDtosToPayload(HotPadsHttpRequest request, Collection<T> dtos, String dtoType) {
		String serializedDtos = jsonSerializer.serialize(dtos);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null) {
			dtoTypeNullSafe = dtos.isEmpty() ? "" : dtos.iterator().next().getClass().getCanonicalName();
		}
		Map<String,String> params = new HashMap<>();
		params.put(config.getDtoParameterName(), serializedDtos);
		params.put(config.getDtoTypeParameterName(), dtoTypeNullSafe);
		request.addPostParams(params);
		return this;
	}
	
	private String getResponseEntity(HttpResponse response) {
		HttpEntity httpEntity = response.getEntity();
		if(httpEntity == null) {
			return "";
		}
		try{
			return EntityUtils.toString(httpEntity);
		} catch (final IOException ignore) {
			return "";
		} finally {
			EntityUtils.consumeQuietly(httpEntity);
		}
	}
}
