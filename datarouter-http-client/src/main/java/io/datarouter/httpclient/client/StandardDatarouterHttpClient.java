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
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpConnectionAbortedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRequestInterruptedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.httpclient.security.DefaultCsrfValidator;
import io.datarouter.httpclient.security.DefaultSignatureValidator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;

@Singleton
public class StandardDatarouterHttpClient implements DatarouterHttpClient{
	private static final Logger logger = LoggerFactory.getLogger(StandardDatarouterHttpClient.class);

	private static final Duration LOG_SLOW_REQUEST_THRESHOLD = Duration.ofSeconds(10);

	private final CloseableHttpClient httpClient;
	private final JsonSerializer jsonSerializer;
	private final DefaultSignatureValidator signatureValidator;
	private final DefaultCsrfValidator csrfValidator;
	private final Supplier<String> apiKeySupplier;
	private final DatarouterHttpClientConfig config;
	private final PoolingHttpClientConnectionManager connectionManager;

	StandardDatarouterHttpClient(CloseableHttpClient httpClient, JsonSerializer jsonSerializer,
			DefaultSignatureValidator signatureValidator, DefaultCsrfValidator csrfValidator,
			Supplier<String> apiKeySupplier, DatarouterHttpClientConfig config,
			PoolingHttpClientConnectionManager connectionManager){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeySupplier = apiKeySupplier;
		this.config = config;
		this.connectionManager = connectionManager;
	}

	@Override
	public DatarouterHttpResponse execute(DatarouterHttpRequest request){
		try{
			return executeChecked(request);
		}catch(DatarouterHttpException e){
			throw new DatarouterHttpRuntimeException(e);
		}
	}

	@Override
	public DatarouterHttpResponse execute(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer){
		try{
			return executeChecked(request, httpEntityConsumer);
		}catch(DatarouterHttpException e){
			throw new DatarouterHttpRuntimeException(e);
		}
	}

	@Override
	public <E> E execute(DatarouterHttpRequest request, Type deserializeToType){
		try{
			return executeChecked(request, deserializeToType);
		}catch(DatarouterHttpException e){
			throw new DatarouterHttpRuntimeException(e);
		}
	}

	@Override
	public <E> E executeChecked(DatarouterHttpRequest request, Type deserializeToType) throws DatarouterHttpException{
		String entity = executeChecked(request).getEntity();
		return jsonSerializer.deserialize(entity, deserializeToType);
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request) throws DatarouterHttpException{
		return executeChecked(request, (Consumer<HttpEntity>)null);
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException{
		setSecurityProperties(request);

		HttpClientContext context = new HttpClientContext();
		context.setAttribute(DatarouterHttpRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		CookieStore cookieStore = new BasicCookieStore();
		for(BasicClientCookie cookie : request.getCookies()){
			cookieStore.addCookie(cookie);
		}
		context.setCookieStore(cookieStore);

		DatarouterHttpException ex;
		HttpRequestBase internalHttpRequest = null;
		long requestStartTimeMs = System.currentTimeMillis();
		try{
			TracerTool.startSpan(TracerThreadLocal.get(), "http call " + request.getPath());
			internalHttpRequest = request.getRequest();
			requestStartTimeMs = System.currentTimeMillis();
			HttpResponse httpResponse = httpClient.execute(internalHttpRequest, context);
			Duration duration = Duration.ofMillis(System.currentTimeMillis() - requestStartTimeMs);
			if(duration.compareTo(LOG_SLOW_REQUEST_THRESHOLD) > 0){
				logger.warn("Slow request target={} duration={}", request.getPath(), duration);
			}
			DatarouterHttpResponse response = new DatarouterHttpResponse(httpResponse, context, httpEntityConsumer);
			if(response.getStatusCode() >= HttpStatus.SC_BAD_REQUEST){
				throw new DatarouterHttpResponseException(response, duration);
			}
			return response;
		}catch(IOException e){
			ex = new DatarouterHttpConnectionAbortedException(e, requestStartTimeMs);
		}catch(CancellationException e){
			ex = new DatarouterHttpRequestInterruptedException(e, requestStartTimeMs);
		}finally{
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
		if(internalHttpRequest != null){
			forceAbortRequestUnchecked(internalHttpRequest);
		}
		throw ex;
	}

	private void setSecurityProperties(DatarouterHttpRequest request){
		Map<String,String> params = new HashMap<>();
		if(csrfValidator != null){
			String csrfIv = DefaultCsrfValidator.generateCsrfIv();
			params.put(SecurityParameters.CSRF_IV, csrfIv);
			params.put(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken(csrfIv));
		}
		if(apiKeySupplier != null){
			params.put(SecurityParameters.API_KEY, apiKeySupplier.get());
		}
		if(request.canHaveEntity() && request.getEntity() == null){
			params = request.addPostParams(params).getFirstPostParams();
			if(signatureValidator != null && !params.isEmpty()){
				String signature = signatureValidator.getHexSignature(request.getFirstPostParams());
				request.addPostParam(SecurityParameters.SIGNATURE, signature);
			}
			request.setEntity(request.getFirstPostParams());
		}else if(request.getMethod() == HttpRequestMethod.GET){
			params = request.addGetParams(params).getFirstGetParams();
			if(signatureValidator != null && !params.isEmpty()){
				String signature = signatureValidator.getHexSignature(request.getFirstGetParams());
				request.addGetParam(SecurityParameters.SIGNATURE, signature);
			}
		}else{
			request.addHeaders(params);
			if(signatureValidator != null && request.getEntity() != null){
				String signature = signatureValidator.getHexSignature(request.getFirstGetParams(), request.getEntity());
				request.addHeader(SecurityParameters.SIGNATURE, signature);
			}
		}
	}

	@Override
	public void shutdown(){
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

	@Override
	public StandardDatarouterHttpClient addDtoToPayload(DatarouterHttpRequest request, Object dto, String dtoType){
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
		DatarouterHttpClientConfig requestConfig = request.getRequestConfig(config);
		Map<String,String> params = new HashMap<>();
		params.put(requestConfig.getDtoParameterName(), serializedDto);
		params.put(requestConfig.getDtoTypeParameterName(), dtoTypeNullSafe);
		request.addPostParams(params);
		return this;
	}

	@Override
	public StandardDatarouterHttpClient setEntityDto(DatarouterHttpRequest request, Object dto){
		String serializedDto = jsonSerializer.serialize(dto);
		request.setEntity(serializedDto, ContentType.APPLICATION_JSON);
		return this;
	}

	@Override
	public PoolStats getPoolStats(){
		return connectionManager.getTotalStats();
	}

	@Override
	public CloseableHttpClient getApacheHttpClient(){
		return httpClient;
	}

}
