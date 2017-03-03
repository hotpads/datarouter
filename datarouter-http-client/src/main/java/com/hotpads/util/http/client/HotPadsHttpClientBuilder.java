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
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;
import com.hotpads.util.http.security.DefaultCsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.DefaultSignatureValidator;

public class HotPadsHttpClientBuilder{

	private static final int DEFAULT_TIMEOUT_MS = 3000;
	private static final int DEFAULT_MAX_TOTAL_CONNECTION = 20;
	private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 2;

	private int timeoutMs; // must be int due to RequestConfig.set*Timeout() methods
	private Long futureTimeoutMs;
	private int maxTotalConnections;
	private int maxConnectionsPerRoute;
	private HttpClientBuilder httpClientBuilder;
	private HotPadsRetryHandler retryHandler;
	private JsonSerializer jsonSerializer;
	private CloseableHttpClient customHttpClient;
	private DefaultSignatureValidator signatureValidator;
	private DefaultCsrfValidator csrfValidator;
	private DefaultApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	private boolean ignoreSsl;

	public HotPadsHttpClientBuilder(){
		this.retryHandler = new HotPadsRetryHandler();
		this.timeoutMs = DEFAULT_TIMEOUT_MS;
		this.maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTION;
		this.maxConnectionsPerRoute = DEFAULT_MAX_CONNECTION_PER_ROUTE;
		this.httpClientBuilder = HttpClientBuilder.create()
				.setRetryHandler(retryHandler)
				.setRedirectStrategy(new LaxRedirectStrategy());
	}

	public HotPadsHttpClient build(){
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMs)
				.setConnectionRequestTimeout(timeoutMs)
				.setSocketTimeout(timeoutMs)
				.build();
		httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		PoolingHttpClientConnectionManager connectionManager;
		if(ignoreSsl){
			SSLConnectionSocketFactory sslsf;
			try{
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, (chain, authType) -> true);
				sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
			}catch(KeyManagementException | KeyStoreException | NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslsf)
					.build();
			connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		}else{
			connectionManager = new PoolingHttpClientConnectionManager();
		}
		connectionManager.setMaxTotal(maxTotalConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		httpClientBuilder.setConnectionManager(connectionManager);
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
				.getRetryCount(), connectionManager);
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

	public HotPadsHttpClientBuilder setSignatureValidator(DefaultSignatureValidator signatureValidator){
		this.signatureValidator = signatureValidator;
		return this;
	}

	public HotPadsHttpClientBuilder setCsrfValidator(DefaultCsrfValidator csrfValidator){
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
		this.maxTotalConnections = maxTotalConnections;
		return this;
	}

	public HotPadsHttpClientBuilder setMaxConnectionsPerRoute(int maxConnectionsPerRoute){
		this.maxConnectionsPerRoute = maxConnectionsPerRoute;
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
