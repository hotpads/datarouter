package com.hotpads.util.http.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.hotpads.util.http.client.json.GsonJsonSerializer;
import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SignatureValidator;

public class HotPadsHttpClientBuilder{
	
	private static final int DEFAULT_TIMEOUT = 3000;
	private static final int SOCKET_TIMEOUT = DEFAULT_TIMEOUT;
	private static final int CONNECTION_REQUEST_TIMEOUT = DEFAULT_TIMEOUT;
	private static final int CONNECTION_TIMEOUT = DEFAULT_TIMEOUT;
	private static final int MAX_TOTAL_CONNECTION = 50;
	private static final int MAX_CONNECTION_PER_ROUTE = 50;
	
	private HttpClientBuilder httpClientBuilder;
	private HotPadsRetryHandler retryHandler;
	private JsonSerializer jsonSerializer;
	private HttpClient customHttpClient;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	
	public HotPadsHttpClient createInstance(){
		return this.create().build();
	}

	public HotPadsHttpClientBuilder create(){
		retryHandler = new HotPadsRetryHandler();
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(CONNECTION_TIMEOUT)
				.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT)
				.build();
		httpClientBuilder = HttpClientBuilder.create()
				.setDefaultRequestConfig(defaultRequestConfig )
				.setMaxConnTotal(MAX_TOTAL_CONNECTION)
				.setMaxConnPerRoute(MAX_CONNECTION_PER_ROUTE)
				.setConnectionManager(new PoolingHttpClientConnectionManager())
				.setRetryHandler(retryHandler)
				.setRedirectStrategy(new LaxRedirectStrategy());
		return this;
	}
	
	public HotPadsHttpClient build(){
		HttpClient builtHttpClient;
		if(customHttpClient == null){
			builtHttpClient = httpClientBuilder.build();
		} else {
			builtHttpClient = customHttpClient;
		}
		if(config == null){
			config = new HotpadsHttpClientDefaultConfig();
		}
		if(jsonSerializer == null){
			jsonSerializer = new GsonJsonSerializer();
		}
		HotPadsHttpClient httpClient = new HotPadsHttpClient(builtHttpClient, 
				this.jsonSerializer, 
				this.signatureValidator, 
				this.csrfValidator,
				this.apiKeyPredicate,
				this.config);
		httpClient.setRetryHandler(retryHandler);
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

	public HotPadsHttpClientBuilder setApiKeyPredicate(ApiKeyPredicate apiKeyPredicate){
		this.apiKeyPredicate = apiKeyPredicate;
		return this;
	}
	
	public HotPadsHttpClientBuilder setConfig(HotPadsHttpClientConfig config){
		this.config = config;
		return this;
	}

}
