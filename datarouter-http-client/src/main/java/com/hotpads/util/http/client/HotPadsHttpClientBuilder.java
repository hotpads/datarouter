package com.hotpads.util.http.client;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import com.hotpads.util.http.client.json.GsonJsonSerializer;
import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.client.security.SignatureValidator;

public class HotPadsHttpClientBuilder{
	
	private static final int DEFAULT_TIMEOUT_MS = 3000;
	private static final int DEFAULT_MAX_TOTAL_CONNECTION = 20;
	private static final int MAX_CONNECTION_PER_ROUTE = 2;
	
	private int timeoutMs;
	private int maxTotalConnections;
	private HttpClientBuilder httpClientBuilder;
	private HotPadsRetryHandler retryHandler;
	private JsonSerializer jsonSerializer;
	private HttpClient customHttpClient;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private DefaultApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	
	public HotPadsHttpClient createInstance(){
		return this.create().build();
	}

	public HotPadsHttpClientBuilder create(){
		retryHandler = new HotPadsRetryHandler();
		timeoutMs = DEFAULT_TIMEOUT_MS;
		maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTION;
		httpClientBuilder = HttpClientBuilder.create()
				.setRetryHandler(retryHandler)
				.setRedirectStrategy(new LaxRedirectStrategy())
				.setMaxConnPerRoute(MAX_CONNECTION_PER_ROUTE)
				.setMaxConnTotal(DEFAULT_MAX_TOTAL_CONNECTION);
		return this;
	}
	
	public HotPadsHttpClient build(){
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMs)
				.setConnectionRequestTimeout(timeoutMs) 
				.setSocketTimeout(timeoutMs)
				.build();
		httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		HttpClient builtHttpClient;
		if(customHttpClient == null){
			builtHttpClient = httpClientBuilder.build();
		} else {
			builtHttpClient = customHttpClient;
		}
		if(config == null){
			config = new HotPadsHttpClientDefaultConfig();
		}
		if(jsonSerializer == null){
			jsonSerializer = new GsonJsonSerializer();
		}
		HotPadsHttpClient httpClient = new HotPadsHttpClient(builtHttpClient, 
				this.jsonSerializer, 
				this.signatureValidator, 
				this.csrfValidator,
				this.apiKeyPredicate,
				this.config,
				new ThreadPoolExecutor(maxTotalConnections, maxTotalConnections, 1000L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>(maxTotalConnections),
						new ThreadPoolExecutor.CallerRunsPolicy()),
						this.timeoutMs);
		return httpClient;
	}
	
	public HotPadsHttpClientBuilder setRetryCount(int retryCount){
		if(customHttpClient != null){
			throw new UnsupportedOperationException("You cannot change the retry count of a custom http client");
		}
		this.retryHandler.setRetryCount(retryCount);
		return this;
	}
	
	public HotPadsHttpClientBuilder setJsonSerializer(JsonSerializer jsonSerializer){
		this.jsonSerializer = jsonSerializer;
		return this;
	}

	public HotPadsHttpClientBuilder setCustomHttpClient(HttpClient httpClient){
		this.customHttpClient = httpClient;
		return this;
	}
	
	public HotPadsHttpClientBuilder setSignatureValidator(SignatureValidator signatureValidator){
		this.signatureValidator = signatureValidator;
		return this;
	}
	
	public HotPadsHttpClientBuilder setCsrfValidator(CsrfValidator csrfValidator){
		this.csrfValidator = csrfValidator;
		return this;
	}

	public HotPadsHttpClientBuilder setApiKeyPredicate(DefaultApiKeyPredicate apiKeyPredicate){
		this.apiKeyPredicate = apiKeyPredicate;
		return this;
	}
	
	public HotPadsHttpClientBuilder setConfig(HotPadsHttpClientConfig config){
		this.config = config;
		return this;
	}
	
	public HotPadsHttpClientBuilder setMaxTotalConnections(int maxTotalConnections){
		this.httpClientBuilder.setMaxConnTotal(maxTotalConnections);
		this.maxTotalConnections = maxTotalConnections;
		return this;
	}
	
	public HotPadsHttpClientBuilder setMaxConnectionsPerRoute(int maxConnectionsPerRoute){
		this.httpClientBuilder.setMaxConnPerRoute(maxConnectionsPerRoute);
		return this;
	}
	
	public HotPadsHttpClientBuilder setTimeoutMs(int timeoutMs){
		this.timeoutMs = timeoutMs;
		return this;
	}

}
