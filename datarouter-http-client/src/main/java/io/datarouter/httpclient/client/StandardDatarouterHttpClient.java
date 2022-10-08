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
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
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

import io.datarouter.httpclient.circuitbreaker.DatarouterHttpClientIoExceptionCircuitBreaker;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.CsrfGenerator.RefreshableCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.httpclient.security.SignatureGenerator;
import io.datarouter.httpclient.security.SignatureGenerator.RefreshableSignatureGenerator;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;
import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.json.JsonSerializer;

@Singleton
public class StandardDatarouterHttpClient implements DatarouterHttpClient{
	private static final Logger logger = LoggerFactory.getLogger(StandardDatarouterHttpClient.class);

	// keep protected for now
	protected final CloseableHttpClient httpClient;
	private final JsonSerializer jsonSerializer;
	protected final SignatureGenerator signatureGenerator;
	protected final CsrfGenerator csrfGenerator;
	protected final Supplier<String> apiKeySupplier;
	protected final RefreshableSignatureGenerator refreshableSignatureGenerator;
	protected final RefreshableCsrfGenerator refreshableCsrfGenerator;
	protected final RefreshableSupplier<String> refreshableApiKeySupplier;
	protected final DatarouterHttpClientConfig config;
	protected final PoolingHttpClientConnectionManager connectionManager;
	protected final DatarouterHttpClientIoExceptionCircuitBreaker circuitWrappedHttpClient;
	protected final Supplier<Boolean> enableBreakers;
	protected final Supplier<URI> urlPrefix;
	protected final Supplier<Boolean> traceInQueryString;
	protected final Supplier<Boolean> debugLog;
	protected final String apiKeyFieldName;
	protected final String name;

	StandardDatarouterHttpClient(
			CloseableHttpClient httpClient,
			JsonSerializer jsonSerializer,
			SignatureGenerator signatureGenerator,
			CsrfGenerator csrfGenerator,
			Supplier<String> apiKeySupplier,
			RefreshableSignatureGenerator refreshableSignatureGenerator,
			RefreshableCsrfGenerator refreshableCsrfGenerator,
			RefreshableSupplier<String> refreshableApiKeySupplier,
			DatarouterHttpClientConfig config,
			PoolingHttpClientConnectionManager connectionManager,
			String name,
			Supplier<Boolean> enableBreakers,
			Supplier<URI> urlPrefix,
			Supplier<Boolean> traceInQueryString,
			Supplier<Boolean> debugLog,
			String apiKeyFieldName){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureGenerator = signatureGenerator;
		this.csrfGenerator = csrfGenerator;
		this.apiKeySupplier = apiKeySupplier;
		this.refreshableSignatureGenerator = refreshableSignatureGenerator;
		this.refreshableCsrfGenerator = refreshableCsrfGenerator;
		this.refreshableApiKeySupplier = refreshableApiKeySupplier;
		this.config = config;
		this.connectionManager = connectionManager;
		this.name = name;
		this.circuitWrappedHttpClient = new DatarouterHttpClientIoExceptionCircuitBreaker(name);
		this.enableBreakers = enableBreakers;
		this.urlPrefix = urlPrefix;
		this.traceInQueryString = traceInQueryString;
		this.debugLog = debugLog;
		this.apiKeyFieldName = apiKeyFieldName;
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
		try(TraceSpanFinisher $ = TracerTool.startSpan("JsonSerializer deserialize", TraceSpanGroupType.SERIALIZATION)){
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
		//store info needed to retry
		Instant firstAttemptInstant = Instant.now();
		Map<String,List<String>> originalGetParams = request.getGetParams().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
		Map<String,List<String>> originalPostParams = request.getPostParams().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
		Map<String,List<String>> originalHeaders = request.getHeaders().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
		try{
			return executeCheckedInternal(request, httpEntityConsumer);
		}catch(DatarouterHttpResponseException e){
			if(shouldRerun40x(firstAttemptInstant, e.getResponse().getStatusCode(), request.getShouldSkipSecurity())){
				//reset any changes to request made during the first attempt
				request.setGetParams(originalGetParams);
				request.setPostParams(originalPostParams);
				request.setHeaders(originalHeaders);
				logger.warn("retrying {}", e.getResponse().getStatusCode());
				return executeCheckedInternal(request, httpEntityConsumer);
			}
			throw e;
		}
	}

	private DatarouterHttpResponse executeCheckedInternal(DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer) throws DatarouterHttpException{
		setSecurityProperties(request);

		HttpClientContext context = new HttpClientContext();
		context.setAttribute(HttpRetryTool.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		CookieStore cookieStore = new BasicCookieStore();
		for(BasicClientCookie cookie : request.getCookies()){
			cookieStore.addCookie(cookie);
		}
		context.setCookieStore(cookieStore);
		return circuitWrappedHttpClient.call(httpClient, request, httpEntityConsumer, context, enableBreakers,
				traceInQueryString, debugLog);
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
			if(!request.getShouldSkipLogs()){
				logger.warn("", e);
			}
			return Conditional.failure(e);
		}
		return Conditional.success(response);
	}

	private void setSecurityProperties(DatarouterHttpRequest request){
		if(request.getShouldSkipSecurity()){
			//the only case from below that is relevant without security is populating the entity
			if(request.canHaveEntity() && request.getEntity() == null){
				request.setEntity(request.getFirstPostParams());
			}
			return;
		}
		SignatureGenerator signatureGenerator = chooseSignatureGenerator();
		CsrfGenerator csrfGenerator = chooseCsrfGenerator();
		Supplier<String> apiKeySupplier = chooseApiKeySupplier();

		Map<String,String> params = new HashMap<>();
		if(csrfGenerator != null){
			String csrfIv = csrfGenerator.generateCsrfIv();
			params.put(SecurityParameters.CSRF_IV, csrfIv);
			params.put(SecurityParameters.CSRF_TOKEN, csrfGenerator.generateCsrfToken(csrfIv));
		}

		if(apiKeySupplier != null){
			params.put(apiKeyFieldName, apiKeySupplier.get());
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

	private Supplier<String> chooseApiKeySupplier(){
		return refreshableApiKeySupplier != null ? refreshableApiKeySupplier : apiKeySupplier != null ? apiKeySupplier
				: null;
	}

	private SignatureGenerator chooseSignatureGenerator(){
		return refreshableSignatureGenerator != null ? refreshableSignatureGenerator : signatureGenerator != null
				? signatureGenerator : null;
	}

	private CsrfGenerator chooseCsrfGenerator(){
		return refreshableCsrfGenerator != null ? refreshableCsrfGenerator : csrfGenerator != null
				? csrfGenerator : null;
	}

	private boolean shouldRerun40x(Instant previous, int statusCode, boolean shouldSkipSecurity){
		if(HttpStatus.SC_UNAUTHORIZED != statusCode && HttpStatus.SC_FORBIDDEN != statusCode || shouldSkipSecurity){
			return false;
		}
		return refreshableSignatureGenerator != null && !refreshableSignatureGenerator.refresh().isBefore(previous)
				|| refreshableCsrfGenerator != null && !refreshableCsrfGenerator.refresh().isBefore(previous)
				|| refreshableApiKeySupplier != null && !refreshableApiKeySupplier.refresh().isBefore(previous);
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

	@Override
	public JsonSerializer getJsonSerializer(){
		return jsonSerializer;
	}

}
