package com.hotpads.util.http.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;

import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SignatureValidator;

public class HotPadsHttpClientBuilder{

	private static final int DEFAULT_TIMEOUT_MS = 3000;
	private static final int DEFAULT_MAX_TOTAL_CONNECTION = 20;
	private static final int MAX_CONNECTION_PER_ROUTE = 2;

	private int timeoutMs; // must be int due to RequestConfig.set*Timeout() methods
	private Long futureTimeoutMs;
	private int maxTotalConnections;
	private HttpClientBuilder httpClientBuilder;
	private HotPadsRetryHandler retryHandler;
	private JsonSerializer jsonSerializer;
	private CloseableHttpClient customHttpClient;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private DefaultApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	private boolean ignoreSsl;

	public HotPadsHttpClientBuilder(){
		retryHandler = new HotPadsRetryHandler();
		timeoutMs = DEFAULT_TIMEOUT_MS;
		maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTION;
		httpClientBuilder = HttpClientBuilder.create()
				.setRetryHandler(retryHandler)
				.setRedirectStrategy(new LaxRedirectStrategy())
				.setMaxConnPerRoute(MAX_CONNECTION_PER_ROUTE)
				.setMaxConnTotal(DEFAULT_MAX_TOTAL_CONNECTION);
	}

	public HotPadsHttpClient build(){
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMs)
				.setConnectionRequestTimeout(timeoutMs)
				.setSocketTimeout(timeoutMs)
				.build();
		httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		if(ignoreSsl){
			try{
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, (chain, authType) -> true);
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(),
						NoopHostnameVerifier.INSTANCE);
				httpClientBuilder.setSSLSocketFactory(sslsf);
			}catch(KeyManagementException | KeyStoreException | NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}
		}
		CloseableHttpClient builtHttpClient;
		if(customHttpClient == null){
			builtHttpClient = httpClientBuilder.build();
		}else{
			builtHttpClient = customHttpClient;
		}
		if(config == null){
			config = new HotPadsHttpClientDefaultConfig();
		}
		if(jsonSerializer == null){
			jsonSerializer = new GsonJsonSerializer();
		}
		ExecutorService executor = new ThreadPoolExecutor(maxTotalConnections, maxTotalConnections, 1000L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(maxTotalConnections),
				new ThreadPoolExecutor.CallerRunsPolicy());
		return new HotPadsHttpClient(builtHttpClient, this.jsonSerializer, this.signatureValidator, this.csrfValidator,
				this.apiKeyPredicate, this.config, executor, this.timeoutMs, this.futureTimeoutMs, retryHandler
						.getRetryCount());
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

	public HotPadsHttpClientBuilder setCustomHttpClient(CloseableHttpClient httpClient){
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

	public HotPadsHttpClientBuilder setFutureTimeoutMs(long futureTimeoutMs){
		this.futureTimeoutMs = futureTimeoutMs;
		return this;
	}

	public HotPadsHttpClientBuilder setIgnoreSsl(boolean ignoreSsl){
		this.ignoreSsl = ignoreSsl;
		return this;
	}

	public HotPadsHttpClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy){
		httpClientBuilder.setRedirectStrategy(redirectStrategy);
		return this;
	}

	public HotPadsHttpClientBuilder setLogOnRetry(boolean logOnRetry){
		retryHandler.setLogOnRetry(logOnRetry);
		return this;
	}

}