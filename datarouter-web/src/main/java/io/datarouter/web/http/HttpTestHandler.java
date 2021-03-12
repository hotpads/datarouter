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

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.util.ExceptionTool;

public class HttpTestHandler extends BaseHandler{

	@Inject
	private DatarouterWebFiles files;
	@Inject
	private HttpTesterClient httpTesterClient;
	@Inject
	private HttpTesterWithoutRedirectClient httpTesterWithoutRedirectClient;
	@Inject
	private DatarouterProperties properties;
	@Inject
	private RequestProxySetter proxySetter;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav httpTest(OptionalString url, OptionalString method, OptionalString requestBody, OptionalString headers,
			OptionalString contentType, OptionalString useProxy, OptionalString followRedirects){
		Mav mav = new Mav(files.jsp.http.httpTesterJsp);
		mav.put("path", paths.datarouter.http.tester.toSlashedString());
		if(url.isEmpty() || method.isEmpty()){
			return mav;
		}
		mav.put("url", url.get());
		HttpRequestMethod requestMethod = "POST".equals(method.get()) ? HttpRequestMethod.POST : HttpRequestMethod.GET;
		mav.put("method", requestMethod.name());
		DatarouterHttpRequest request = new DatarouterHttpRequest(requestMethod, url.get()).setRetrySafe(true);
		if(headers.isPresent()){
			Map<String,String> headersMap = GsonTool.GSON.fromJson(headers.get(),
					new TypeToken<Map<String,String>>(){}.getType());
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
		Conditional<DatarouterHttpResponse> response;
		if(followRedirects.isPresent()){
			mav.put("followRedirects", true);
			response = httpTesterClient.tryExecute(request);
		}else{
			response = httpTesterWithoutRedirectClient.tryExecute(request);
		}
		Long elapsedMs = System.currentTimeMillis() - start;
		if(response.isFailure() && response.getException() instanceof DatarouterHttpResponseException){
			DatarouterHttpResponseException responseException = (DatarouterHttpResponseException)response
					.getException();
			addResponseToMavModel(mav, url.get(), elapsedMs, Optional.of(responseException.getResponse()));
		}else if(response.isFailure()){
			mav.put("stackTrace", ExceptionTool.getStackTraceAsString(response.getException()));
			addResponseToMavModel(mav, url.get(), elapsedMs, Optional.empty());
		}
		response.ifSuccess(httpResponse -> addResponseToMavModel(mav, url.get(), elapsedMs, Optional.of(httpResponse)));
		return mav;
	}

	@Handler
	public Mav dnsLookup(OptionalString hostname){
		Mav mav = new Mav(files.jsp.http.dnsLookupJsp);
		mav.put("path", paths.datarouter.http.dnsLookup.toSlashedString());
		if(hostname.isEmpty()){
			return mav;
		}
		try{
			mav.put("hostname", hostname.get());
			Long start = System.currentTimeMillis();
			InetAddress[] ipAddresses = InetAddress.getAllByName(hostname.get());
			Long elapsedMs = System.currentTimeMillis() - start;
			String ipAddressesFormatted = Scanner.of(ipAddresses)
					.map(InetAddress::getHostAddress)
					.collect(Collectors.joining("\n"));
			String formattedResponse = "duration: " + elapsedMs + " ms" + "\n\n" + "ips: " + ipAddressesFormatted;
			mav.put("ipAddresses", formattedResponse);
		}catch(UnknownHostException e){
			mav.put("error", ExceptionTool.getStackTraceAsString(e));
		}
		return mav;
	}

	public void addResponseToMavModel(Mav mav, String requestUrl, Long responseMs,
			Optional<DatarouterHttpResponse> response){
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
	}

	@Singleton
	public static class HttpTesterClient extends BaseDatarouterHttpClientWrapper{

		public HttpTesterClient(){
			super(new DatarouterHttpClientBuilder().build());
		}

	}

	@Singleton
	public static class HttpTesterWithoutRedirectClient extends BaseDatarouterHttpClientWrapper{

		public HttpTesterWithoutRedirectClient(){
			super(new DatarouterHttpClientBuilder().disableRedirectHandling().build());
		}

	}


}
