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
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.java.BaseEndpoint;
import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.httpclient.endpoint.java.EndpointType;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.pathnode.PathNode;

@Singleton
public class StandardDatarouterEndpointClient<
		ET extends EndpointType>
extends BaseHttpClient
implements DatarouterEndpointClient<ET>{
	private static final Logger logger = LoggerFactory.getLogger(StandardDatarouterEndpointClient.class);

	public StandardDatarouterEndpointClient(StandardDatarouterHttpClient client){
		super(
				client.httpClient,
				client.getJsonSerializer(),
				client.signatureGenerator,
				client.csrfGenerator,
				client.apiKeySupplier,
				client.refreshableSignatureGenerator,
				client.refreshableCsrfGenerator,
				client.refreshableApiKeySupplier,
				client.config,
				client.connectionManager,
				client.name,
				client.enableBreakers,
				client.urlPrefix,
				client.traceInQueryString,
				client.debugLog,
				client.apiKeyFieldName);
	}

	private <R> R executeChecked(DatarouterHttpRequest request, PathNode pathNode, Type deserializeToType)
	throws DatarouterHttpException{
		String entity = executeChecked(request, pathNode).getEntity();
		return deserializeEntity(entity, deserializeToType);
	}

	private DatarouterHttpResponse executeChecked(DatarouterHttpRequest request, PathNode pathNode)
	throws DatarouterHttpException{
		return executeChecked(request, pathNode, (Consumer<HttpEntity>)null);
	}

	private DatarouterHttpResponse executeChecked(
			DatarouterHttpRequest request,
			PathNode pathNode,
			Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException{
		//store info needed to retry
		Instant firstAttemptInstant = Instant.now();
		Map<String,List<String>> originalGetParams = request.getGetParams().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
		Map<String,List<String>> originalPostParams = request.getPostParams().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
		Map<String,List<String>> originalHeaders = request.getHeaders().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new ArrayList<>(entry.getValue())));

		DatarouterHttpResponse response = null;
		try{
			response = executeCheckedInternal(request, httpEntityConsumer);
			return response;
		}catch(DatarouterHttpResponseException e){
			response = e.getResponse();
			if(shouldRerun40x(firstAttemptInstant, e.getResponse().getStatusCode(), request.getShouldSkipSecurity())){
				//reset any changes to request made during the first attempt
				request.setGetParams(originalGetParams);
				request.setPostParams(originalPostParams);
				request.setHeaders(originalHeaders);
				logger.warn("retrying {}", e.getResponse().getStatusCode());
				return executeCheckedInternal(request, httpEntityConsumer);
			}
			throw e;
		}finally{
			String counter = String.format("endpointHttpClient %s %s", name, pathNode.toSlashedString());
			Counters.inc(counter);

			int statusCode = response == null ? 0 : response.getStatusCode();
			String counterWithStatus = String.format("endpointHttpClient %s %s %d", name, pathNode.toSlashedString(),
					statusCode);
			Counters.inc(counterWithStatus);
		}
	}

	private <R> Conditional<R> tryExecute(DatarouterHttpRequest request, PathNode pathNode, Type deserializeToType){
		R response;
		try{
			response = executeChecked(request, pathNode, deserializeToType);
		}catch(DatarouterHttpException e){
			if(!request.getShouldSkipLogs()){
				logger.warn("", e);
			}
			return Conditional.failure(e);
		}
		return Conditional.success(response);
	}

	@Override
	public <R> Conditional<R> call(BaseEndpoint<R,ET> endpoint){
		return callAnyType(endpoint);
	}

	@Override
	public <E> Conditional<E> callAnyType(BaseEndpoint<E,?> endpoint){
		endpoint.setUrlPrefix(urlPrefix.get());
		DatarouterHttpRequest datarouterHttpRequest = EndpointTool.toDatarouterHttpRequest(endpoint, jsonSerializer);
		EndpointTool.findEntity(endpoint).ifPresent(entity -> setEntityDto(datarouterHttpRequest, entity));
		Type responseType = EndpointTool.getResponseType(endpoint.getClass());
		return tryExecute(datarouterHttpRequest, endpoint.pathNode, responseType);
	}

	@Override
	public <R> R callChecked(BaseEndpoint<R,ET> endpoint) throws DatarouterHttpException{
		endpoint.setUrlPrefix(urlPrefix.get());
		DatarouterHttpRequest datarouterHttpRequest = EndpointTool.toDatarouterHttpRequest(endpoint, jsonSerializer);
		EndpointTool.findEntity(endpoint).ifPresent(entity -> setEntityDto(datarouterHttpRequest, entity));
		Type responseType = EndpointTool.getResponseType(endpoint.getClass());
		return executeChecked(datarouterHttpRequest, endpoint.pathNode, responseType);
	}

	@Override
	public String toUrl(BaseEndpoint<?,ET> endpoint){
		endpoint.setUrlPrefix(urlPrefix.get());
		String finalUrl = URI.create(endpoint.urlPrefix + endpoint.pathNode.toSlashedString())
				.normalize()
				.toString();
		Map<String,String> paramMap = EndpointTool.getParamFields(endpoint, jsonSerializer).getParams;
		String params = paramMap.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("&", "?", ""));
		return finalUrl + params;
	}

	public StandardDatarouterEndpointClient<ET> addDtoToPayload(
			DatarouterHttpRequest request,
			Object dto,
			String dtoType){
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

	protected StandardDatarouterEndpointClient<ET> setEntityDto(DatarouterHttpRequest request, Object dto){
		String serializedDto = jsonSerializer.serialize(dto);
		request.setEntity(serializedDto, ContentType.APPLICATION_JSON);
		return this;
	}

}
