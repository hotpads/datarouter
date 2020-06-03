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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
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

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.httpclient.security.SignatureGenerator;
import io.datarouter.instrumentation.trace.TracerTool;

@Singleton
public class StandardDatarouterHttpClient implements DatarouterHttpClient{
	private static final Logger logger = LoggerFactory.getLogger(StandardDatarouterHttpClient.class);

	private final CloseableHttpClient httpClient;
	private final JsonSerializer jsonSerializer;
	private final SignatureGenerator signatureGenerator;
	private final CsrfGenerator csrfGenerator;
	private final Supplier<String> apiKeySupplier;
	private final DatarouterHttpClientConfig config;
	private final PoolingHttpClientConnectionManager connectionManager;
	private final DatarouterHttpClientIoExceptionCircuitBreaker circuitWrappedHttpClient;
	private final Supplier<Boolean> enableBreakers;

	StandardDatarouterHttpClient(
			CloseableHttpClient httpClient,
			JsonSerializer jsonSerializer,
			SignatureGenerator signatureGenerator,
			CsrfGenerator csrfGenerator,
			Supplier<String> apiKeySupplier,
			DatarouterHttpClientConfig config,
			PoolingHttpClientConnectionManager connectionManager,
			String name,
			Supplier<Boolean> enableBreakers){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureGenerator = signatureGenerator;
		this.csrfGenerator = csrfGenerator;
		this.apiKeySupplier = apiKeySupplier;
		this.config = config;
		this.connectionManager = connectionManager;
		this.circuitWrappedHttpClient = new DatarouterHttpClientIoExceptionCircuitBreaker(name);
		this.enableBreakers = enableBreakers;
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
		try(var $ = TracerTool.startSpan("JsonSerializer deserialize")){
			TracerTool.appendToSpanInfo("characters", entity.length());
			return jsonSerializer.deserialize(entity, deserializeToType);
		}
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request) throws DatarouterHttpException{
		return executeChecked(request, (Consumer<HttpEntity>)null);
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException{
		setSecurityProperties(request);

		var context = new HttpClientContext();
		context.setAttribute(HttpRetryTool.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		CookieStore cookieStore = new BasicCookieStore();
		for(BasicClientCookie cookie : request.getCookies()){
			cookieStore.addCookie(cookie);
		}
		context.setCookieStore(cookieStore);
		return circuitWrappedHttpClient.call(httpClient, request, httpEntityConsumer, context, enableBreakers);
	}

	@Override
	public Conditional<DatarouterHttpResponse> tryExecute(DatarouterHttpRequest request){
		DatarouterHttpResponse response;
		try{
			response = executeChecked(request);
		}catch(DatarouterHttpException e){
			return Conditional.failure(e);
		}
		return Conditional.success(response);
	}

	@Override
	public Conditional<DatarouterHttpResponse> tryExecute(
			DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer){
		DatarouterHttpResponse response;
		try{
			response = executeChecked(request, httpEntityConsumer);
		}catch(DatarouterHttpException e){
			return Conditional.failure(e);
		}
		return Conditional.success(response);
	}

	@Override
	public <E> Conditional<E> tryExecute(DatarouterHttpRequest request, Type deserializeToType){
		E response;
		try{
			response = executeChecked(request, deserializeToType);
		}catch(DatarouterHttpException e){
			logger.warn("", e);
			return Conditional.failure(e);
		}
		return Conditional.success(response);
	}

	private void setSecurityProperties(DatarouterHttpRequest request){
		Map<String,String> params = new HashMap<>();
		if(csrfGenerator != null){
			String csrfIv = csrfGenerator.generateCsrfIv();
			params.put(SecurityParameters.CSRF_IV, csrfIv);
			params.put(SecurityParameters.CSRF_TOKEN, csrfGenerator.generateCsrfToken(csrfIv));
		}
		if(apiKeySupplier != null){
			params.put(SecurityParameters.API_KEY, apiKeySupplier.get());
		}
		if(request.canHaveEntity() && request.getEntity() == null){
			params = request.addPostParams(params).getFirstPostParams();
			if(signatureGenerator != null && !params.isEmpty()){
				String signature = signatureGenerator.getHexSignature(request.getFirstPostParams()).signature;
				request.addPostParam(SecurityParameters.SIGNATURE, signature);
			}
			request.setEntity(request.getFirstPostParams());
		}else if(request.getMethod() == HttpRequestMethod.GET){
			params = request.addGetParams(params).getFirstGetParams();
			if(signatureGenerator != null && !params.isEmpty()){
				String signature = signatureGenerator.getHexSignature(request.getFirstGetParams()).signature;
				request.addGetParam(SecurityParameters.SIGNATURE, signature);
			}
		}else{
			request.addHeaders(params);
			if(signatureGenerator != null && request.getEntity() != null){
				String signature = signatureGenerator.getHexSignature(
						request.getFirstGetParams(),
						request.getEntity()).signature;
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

	@SuppressWarnings("unused")
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
