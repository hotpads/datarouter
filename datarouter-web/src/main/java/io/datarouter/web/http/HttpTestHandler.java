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
package io.datarouter.web.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;

import com.google.gson.reflect.TypeToken;

import io.datarouter.httpclient.client.BaseDatarouterHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.proxy.RequestProxySetter;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalString;

public class HttpTestHandler extends BaseHandler{

	@Inject
	private DatarouterWebFiles files;
	@Inject
	private HttpTesterClient testerClient;
	@Inject
	private DatarouterProperties properties;
	@Inject
	private RequestProxySetter proxySetter;

	@Handler(defaultHandler = true)
	public Mav httpTest(OptionalString url, OptionalString method, OptionalString requestBody, OptionalString headers,
			OptionalString contentType, OptionalString useProxy){
		Mav mav = new Mav(files.jsp.http.httpTesterJsp);
		if(url.isEmpty() || method.isEmpty()){
			return mav;
		}
		mav.put("url", url.get());
		HttpRequestMethod requestMethod = "POST".equals(method.get()) ? HttpRequestMethod.POST : HttpRequestMethod.GET;
		mav.put("method", requestMethod.name());
		DatarouterHttpRequest request = new DatarouterHttpRequest(requestMethod, url.get()).setRetrySafe(true);
		if(headers.isPresent()){
			List<Map<String,String>> headerPairsList = GsonTool.GSON.fromJson(headers.get(),
					new TypeToken<List<Map<String,String>>>(){}.getType());
			Map<String,String> headersMap = headerPairsList.stream()
					.flatMap(map -> map.entrySet().stream())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			request.addHeaders(headersMap);
			mav.put("headersMap", GsonTool.GSON.toJson(headersMap));
		}
		if(requestBody.isPresent()){
			ContentType cont = contentType.isPresent() ? ContentType.getByMimeType(contentType.get())
					: ContentType.APPLICATION_JSON;
			request.setEntity(requestBody.get(), cont);
			mav.put("requestBody", requestBody.get());
			mav.put("contentType", cont.getMimeType());
		}
		if(useProxy.isPresent()){
			mav.put("useProxy", true);
			proxySetter.setProxyOnRequest(request);
		}
		Long start = System.currentTimeMillis();
		Conditional<DatarouterHttpResponse> response = testerClient.tryExecute(request);
		Long elapsedMs = System.currentTimeMillis() - start;
		if(response.isFailure() && response.getException() instanceof DatarouterHttpResponseException){
			DatarouterHttpResponseException responseException = (DatarouterHttpResponseException)response
					.getException();
			addResponseToMavModel(mav, url.get(), elapsedMs, Optional.of(responseException.getResponse()), Optional.of(
					responseException));
		}else if(response.isFailure()){
			addResponseToMavModel(mav, url.get(), elapsedMs, Optional.empty(), Optional.of(response.getException()));
		}
		response.ifSuccess(httpResponse -> addResponseToMavModel(mav, url.get(), elapsedMs, Optional.of(httpResponse),
				Optional.empty()));
		return mav;
	}

	public void addResponseToMavModel(Mav mav, String requestUrl, Long responseMs,
			Optional<DatarouterHttpResponse> response, Optional<Exception> exception){
		mav.put("url", requestUrl);
		mav.put("serverName", properties.getServerName());
		mav.put("responseMs", responseMs);
		if(response.isPresent()){
			mav.put("statusCode", response.get().getStatusCode());
			mav.put("responseBody", response.get().getEntity());
			Map<String,String> headerMap = Scanner.of(response.get().getAllHeaders()).toMap(Header::getName,
					Header::getValue);
			mav.put("headers", headerMap);
		}
		if(exception.isPresent()){
			mav.put("cause", exception.get().getCause());
			mav.put("message", exception.get().getMessage());
		}
	}

	@Singleton
	public static class HttpTesterClient extends BaseDatarouterHttpClientWrapper{

		public HttpTesterClient(){
			super(new DatarouterHttpClientBuilder().build());
		}

	}

}
