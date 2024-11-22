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

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.CsrfGenerator.RefreshableCsrfGenerator;
import io.datarouter.httpclient.security.SignatureGenerator;
import io.datarouter.httpclient.security.SignatureGenerator.RefreshableSignatureGenerator;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;
import io.datarouter.json.JsonSerializer;
import jakarta.inject.Singleton;

@Singleton
public class StandardDatarouterHttpClient
extends BaseHttpClient
implements DatarouterHttpClient{
	private static final Logger logger = LoggerFactory.getLogger(StandardDatarouterHttpClient.class);

	protected StandardDatarouterHttpClient(
			String clientName,
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
			Supplier<URI> urlPrefix,
			Supplier<Boolean> traceInQueryString,
			Supplier<Boolean> debugLog,
			String apiKeyFieldName){
		super(
				clientName,
				httpClient,
				jsonSerializer,
				signatureGenerator,
				csrfGenerator,
				apiKeySupplier,
				refreshableSignatureGenerator,
				refreshableCsrfGenerator,
				refreshableApiKeySupplier,
				config,
				connectionManager,
				name,
				urlPrefix,
				traceInQueryString,
				debugLog,
				apiKeyFieldName);
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
		return deserializeEntity(entity, deserializeToType);
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
			onException();
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

	@Override
	public StandardDatarouterHttpClient addDtoToPayload(DatarouterHttpRequest request, Object dto, String dtoType){
		String serializedDto = jsonSerializer.serialize(dto);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null){
			if(dto instanceof Iterable<?> dtos){
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

}
