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
package io.datarouter.web.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.httpclient.client.BaseDatarouterHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.proxy.RequestProxySetter;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.RunNativeDto;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.link.DnsLookupLink;
import io.datarouter.web.link.HttpTestLink;
import io.datarouter.web.util.ExceptionTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class HttpTestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(HttpTestHandler.class);

	@Inject
	private DatarouterWebFiles files;
	@Inject
	private HttpTesterClient httpTesterClient;
	@Inject
	private HttpTesterWithoutRedirectClient httpTesterWithoutRedirectClient;
	@Inject
	private ServerName serverName;
	@Inject
	private RequestProxySetter proxySetter;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav httpTest(HttpTestLink link){
		Mav mav = new Mav(files.jsp.http.httpTesterJsp);
		mav.put("path", paths.datarouter.http.tester.toSlashedString());
		if(link.url.isEmpty() || link.method.isEmpty()){
			return mav;
		}
		mav.put("url", link.url.get());
		HttpRequestMethod requestMethod = switch(link.method.get()){
			case "POST" -> HttpRequestMethod.POST;
			case "HEAD" -> HttpRequestMethod.HEAD;
			default -> HttpRequestMethod.GET;
		};

		mav.put("method", requestMethod.name());
		DatarouterHttpRequest request = new DatarouterHttpRequest(requestMethod, link.url.get()).setRetrySafe(true);
		if(link.headers.isPresent()){
			Map<String,String> headersMap = DatarouterGsons.withUnregisteredEnums().fromJson(link.headers.get(),
					new TypeToken<Map<String,String>>(){}.getType());
			request.addHeaders(headersMap);
			mav.put("headersMap", DatarouterGsons.withUnregisteredEnums().toJson(headersMap));
		}
		if(link.requestBody.isPresent() && requestMethod != HttpRequestMethod.HEAD){
			ContentType cont = link.contentType.isPresent() ? ContentType.getByMimeType(link.contentType.get())
					: ContentType.APPLICATION_JSON;
			request.setEntity(link.requestBody.get(), cont);
			mav.put("requestBody", link.requestBody.get());
			mav.put("contentType", cont.getMimeType());
		}
		if(link.requestBody.isPresent() && requestMethod == HttpRequestMethod.HEAD){
			mav.put("requestBody", "Not Allowed for HEAD requests");
		}
		if(link.useProxy.isPresent()){
			mav.put("useProxy", true);
			proxySetter.setProxyOnRequest(request);
		}
		Long start = System.currentTimeMillis();
		Conditional<DatarouterHttpResponse> response;
		if(link.followRedirects.isPresent()){
			mav.put("followRedirects", true);
			response = httpTesterClient.tryExecute(request);
		}else{
			response = httpTesterWithoutRedirectClient.tryExecute(request);
		}
		Long elapsedMs = System.currentTimeMillis() - start;
		if(response.isFailure()
				&& response.getException() instanceof DatarouterHttpResponseException responseException){
			logger.warn("", response.getException());
			addResponseToMavModel(mav, link.url.get(), elapsedMs, Optional.of(responseException.getResponse()));
		}else if(response.isFailure()){
			mav.put("stackTrace", ExceptionTool.getStackTraceAsString(response.getException()));
			addResponseToMavModel(mav, link.url.get(), elapsedMs, Optional.empty());
		}
		response.ifSuccess(httpResponse -> addResponseToMavModel(mav, link.url.get(), elapsedMs,
				Optional.of(httpResponse)));
		return mav;
	}

	@Handler
	public Mav dnsLookup(DnsLookupLink link){
		Optional<String> hostname = link.hostname;
		Mav mav = new Mav(files.jsp.http.dnsLookupJsp);
		mav.put("path", paths.datarouter.http.dnsLookup.toSlashedString());
		mav.put("caching", Security.getProperty("networkaddress.cache.ttl"));
		if(hostname.isEmpty()){
			return mav;
		}
		mav.put("hostname", hostname.get());
		DatarouterDuration elapsed;
		String javaResult;
		Long startNs = System.nanoTime();
		try{
			InetAddress[] ipAddresses = InetAddress.getAllByName(hostname.get());
			elapsed = DatarouterDuration.ageNs(startNs);
			String ipAddressesFormatted = Scanner.of(ipAddresses)
					.map(InetAddress::getHostAddress)
					.collect(Collectors.joining("\n"));
			javaResult = "ips:\n" + ipAddressesFormatted;
		}catch(UnknownHostException e){
			elapsed = DatarouterDuration.ageNs(startNs);
			javaResult = "error:\n" + ExceptionTool.getStackTraceAsString(e);
		}
		mav.put("javaDuration", elapsed.toString(TimeUnit.MICROSECONDS));
		mav.put("javaResult", javaResult);

		startNs = System.nanoTime();
		RunNativeDto digResult = DigRunner.lookup(hostname.get());
		mav.put("digDuration", DatarouterDuration.ageNs(startNs).toString(TimeUnit.MICROSECONDS));
		mav.put("digExitVal", digResult.exitVal());
		mav.put("digResultStdout", digResult.stdout());
		mav.put("digResultStderr", digResult.stderr());
		List<DnsAnswer> parsedAnswers = DigRunner.parse(digResult.stdout());
		mav.put("parsedDig", parsedAnswers.stream().map(DnsAnswer::toString).collect(Collectors.joining("\n")));

		return mav;
	}

	public void addResponseToMavModel(Mav mav, String requestUrl, Long responseMs,
			Optional<DatarouterHttpResponse> response){
		mav.put("url", requestUrl);
		mav.put("serverName", serverName.get());
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
			super(new DatarouterHttpClientBuilder("http-tester-client", GsonJsonSerializer.DEFAULT)
					.setTimeout(Duration.ofMinutes(5))
					.build());
		}

	}

	@Singleton
	public static class HttpTesterWithoutRedirectClient extends BaseDatarouterHttpClientWrapper{

		public HttpTesterWithoutRedirectClient(){
			super(new DatarouterHttpClientBuilder("http-tester-client", GsonJsonSerializer.DEFAULT)
					.setTimeout(Duration.ofMinutes(5))
					.disableRedirectHandling()
					.build());
		}

	}

}
