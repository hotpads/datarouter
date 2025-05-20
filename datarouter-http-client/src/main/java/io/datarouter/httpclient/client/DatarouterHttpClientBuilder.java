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

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import io.datarouter.httpclient.endpoint.java.JavaEndpointType;
import io.datarouter.httpclient.endpoint.link.LinkType;
import io.datarouter.httpclient.link.DatarouterLinkSettings;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.CsrfGenerator.RefreshableCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.httpclient.security.SignatureGenerator;
import io.datarouter.httpclient.security.SignatureGenerator.RefreshableSignatureGenerator;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;
import io.datarouter.json.JsonSerializer;

public class DatarouterHttpClientBuilder{

	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);
	public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;

	private final String clientName;
	private final String simpleClassName;

	private final JsonSerializer jsonSerializer;
	private int timeoutMs; // must be int due to RequestConfig.set*Timeout() methods
	private int connectTimeoutMs;
	private int maxTotalConnections;
	private int maxConnectionsPerRoute;
	private Optional<Integer> validateAfterInactivityMs;
	private Duration fallbackIdleTimeout;
	private final HttpClientBuilder httpClientBuilder;
	private Supplier<Integer> retryCount;
	private CloseableHttpClient customHttpClient;
	private SignatureGenerator signatureGenerator;
	private CsrfGenerator csrfGenerator;
	private Supplier<String> apiKeySupplier;
	private RefreshableSignatureGenerator refreshableSignatureGenerator;
	private RefreshableCsrfGenerator refreshableCsrfGenerator;
	private RefreshableSupplier<String> refreshableApiKeySupplier;
	private DatarouterHttpClientConfig config;
	private boolean ignoreSsl;
	private SSLContext customSslContext;
	private Supplier<URI> urlPrefix;
	private Supplier<Boolean> traceInQueryString;
	private Supplier<Boolean> debugLog;
	private String apiKeyFieldName;

	public DatarouterHttpClientBuilder(String clientName, JsonSerializer jsonSerializer){
		this.clientName = clientName;
		String className = new Throwable().getStackTrace()[1].getClassName();
		this.simpleClassName = className.substring(className.lastIndexOf(".") + 1, className.length());

		this.jsonSerializer = jsonSerializer;
		this.timeoutMs = (int)DEFAULT_TIMEOUT.toMillis();
		this.connectTimeoutMs = (int)Duration.ofSeconds(1).toMillis();
		this.maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
		this.maxConnectionsPerRoute = 200;
		this.validateAfterInactivityMs = Optional.empty();
		this.fallbackIdleTimeout = Duration.ofMinutes(5);
		this.httpClientBuilder = HttpClientBuilder.create()
				.setRedirectStrategy(LaxRedirectStrategy.INSTANCE);
		this.retryCount = () -> HttpRetryTool.DEFAULT_RETRY_COUNT;
		this.traceInQueryString = () -> false;
		this.debugLog = () -> false;
		this.apiKeyFieldName = SecurityParameters.API_KEY;
	}

	private StandardDatarouterHttpClient buildStandardDatarouterHttpClient(){
		httpClientBuilder.setRetryHandler(new DatarouterHttpRetryHandler(retryCount));
		httpClientBuilder.setServiceUnavailableRetryStrategy(new DatarouterServiceUnavailableRetryStrategy(retryCount));
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.STANDARD)
				.setConnectTimeout(connectTimeoutMs)
				.setConnectionRequestTimeout(timeoutMs)
				.setSocketTimeout(timeoutMs)
				.build();
		httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		httpClientBuilder.setKeepAliveStrategy(new DatarouterConnectionKeepAliveStrategy(
				fallbackIdleTimeout,
				clientName));
		SSLConnectionSocketFactory sslsf;
		if(ignoreSsl || customSslContext != null){
			if(ignoreSsl){
				try{
					var ssLContext = new SSLContextBuilder()
							.loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
							.build();
					sslsf = new SSLConnectionSocketFactory(ssLContext, NoopHostnameVerifier.INSTANCE);
				}catch(KeyManagementException | KeyStoreException | NoSuchAlgorithmException e){
					throw new RuntimeException(e);
				}
			}else{
				sslsf = new SSLConnectionSocketFactory(
						customSslContext,
						SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			}
		}else{
			sslsf = SSLConnectionSocketFactory.getSocketFactory();
		}
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
				.<ConnectionSocketFactory>create()
				.register(
						"http",
						new DatarouterConnectionSocketFactory(PlainConnectionSocketFactory.INSTANCE, simpleClassName))
				.register("https", new DatarouterLayeredConnectionSocketFactory(sslsf, simpleClassName))
				.build();
		var connectionManager = new PoolingHttpClientConnectionManager(
				socketFactoryRegistry,
				null,
				new DatarouterHttpClientDnsResolver(simpleClassName));
		connectionManager.setMaxTotal(maxTotalConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		validateAfterInactivityMs.ifPresent(connectionManager::setValidateAfterInactivity);
		httpClientBuilder.setConnectionManager(connectionManager);
		CloseableHttpClient builtHttpClient;
		if(customHttpClient == null){
			builtHttpClient = httpClientBuilder.build();
		}else{
			builtHttpClient = customHttpClient;
		}
		if(config == null){
			config = new DatarouterHttpClientDefaultConfig();
		}
		return new StandardDatarouterHttpClient(
				clientName,
				builtHttpClient,
				this.jsonSerializer,
				this.signatureGenerator,
				this.csrfGenerator,
				this.apiKeySupplier,
				this.refreshableSignatureGenerator,
				this.refreshableCsrfGenerator,
				this.refreshableApiKeySupplier,
				this.config,
				connectionManager,
				simpleClassName,
				urlPrefix,
				traceInQueryString,
				debugLog,
				apiKeyFieldName);
	}

	public DatarouterHttpClient build(){
		return buildStandardDatarouterHttpClient();
	}

	public <ET extends JavaEndpointType> DatarouterEndpointClient<ET> buildEndpointClient(){
		StandardDatarouterHttpClient client = buildStandardDatarouterHttpClient();
		return new StandardDatarouterEndpointClient<>(client);
	}

	public <L extends LinkType> LinkClient<L> buildLinkClient(){
		StandardDatarouterHttpClient client = buildStandardDatarouterHttpClient();
		return new StandardLinkClient<>(client);
	}

	public DatarouterHttpClientBuilder setRetryCount(Supplier<Integer> retryCount){
		if(customHttpClient != null){
			throw new UnsupportedOperationException("You cannot change the retry count of a custom http client");
		}
		this.retryCount = retryCount;
		return this;
	}

	public DatarouterHttpClientBuilder setCustomHttpClient(CloseableHttpClient httpClient){
		this.customHttpClient = httpClient;
		return this;
	}

	public DatarouterHttpClientBuilder setSignatureGenerator(SignatureGenerator signatureGenerator){
		this.signatureGenerator = signatureGenerator;
		return this;
	}

	public DatarouterHttpClientBuilder setCsrfGenerator(CsrfGenerator csrfGenerator){
		this.csrfGenerator = csrfGenerator;
		return this;
	}

	public DatarouterHttpClientBuilder setApiKeySupplier(Supplier<String> apiKeySupplier){
		this.apiKeySupplier = apiKeySupplier;
		return this;
	}

	public DatarouterHttpClientBuilder setRefreshableSignatureGenerator(
			RefreshableSignatureGenerator refreshableSignatureGenerator){
		this.refreshableSignatureGenerator = refreshableSignatureGenerator;
		return this;
	}

	public DatarouterHttpClientBuilder setRefreshableCsrfGenerator(RefreshableCsrfGenerator refreshableCsrfGenerator){
		this.refreshableCsrfGenerator = refreshableCsrfGenerator;
		return this;
	}

	public DatarouterHttpClientBuilder setRefreshableApiKeySupplier(
			RefreshableSupplier<String> refreshableApiKeySupplier){
		this.refreshableApiKeySupplier = refreshableApiKeySupplier;
		return this;
	}

	public DatarouterHttpClientBuilder setConfig(DatarouterHttpClientConfig config){
		this.config = config;
		return this;
	}

	public DatarouterHttpClientBuilder setMaxTotalConnections(int maxTotalConnections){
		this.maxTotalConnections = maxTotalConnections;
		return this;
	}

	public DatarouterHttpClientBuilder setMaxConnectionsPerRoute(int maxConnectionsPerRoute){
		this.maxConnectionsPerRoute = maxConnectionsPerRoute;
		return this;
	}

	// TODO rename to setReadTimeout
	public DatarouterHttpClientBuilder setTimeout(Duration timeout){
		this.timeoutMs = (int)timeout.toMillis();
		return this;
	}

	public DatarouterHttpClientBuilder setConnectTimeoutMs(Duration connectTimeoutMs){
		this.connectTimeoutMs = (int)connectTimeoutMs.toMillis();
		return this;
	}

	public DatarouterHttpClientBuilder setIgnoreSsl(boolean ignoreSsl){
		this.ignoreSsl = ignoreSsl;
		return this;
	}

	public DatarouterHttpClientBuilder setCustomSslContext(SSLContext customSslContext){
		this.customSslContext = customSslContext;
		return this;
	}

	public DatarouterHttpClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy){
		httpClientBuilder.setRedirectStrategy(redirectStrategy);
		return this;
	}

	public DatarouterHttpClientBuilder disableRedirectHandling(){
		httpClientBuilder.disableRedirectHandling();
		return this;
	}

	public DatarouterHttpClientBuilder setValidateAfterInactivityMs(int validateAfterInactivityMs){
		this.validateAfterInactivityMs = Optional.of(validateAfterInactivityMs);
		return this;
	}

	public DatarouterHttpClientBuilder setFallbackIdleTimeout(Duration fallbackIdleTimeout){
		this.fallbackIdleTimeout = fallbackIdleTimeout;
		return this;
	}

	public DatarouterHttpClientBuilder setUrlPrefix(Supplier<URI> urlPrefix){
		this.urlPrefix = urlPrefix;
		return this;
	}

	public DatarouterHttpClientBuilder setTraceInQueryString(Supplier<Boolean> traceInQueryString){
		this.traceInQueryString = traceInQueryString;
		return this;
	}

	public DatarouterHttpClientBuilder setDebugLog(Supplier<Boolean> debugLog){
		this.debugLog = debugLog;
		return this;
	}

	public DatarouterHttpClientBuilder setApiKeyFieldName(String apiKeyFieldName){
		this.apiKeyFieldName = apiKeyFieldName;
		return this;
	}

	public DatarouterHttpClientBuilder forDatarouterHttpClientSettings(SimpleDatarouterHttpClientSettings settings){
		return this
				.setTimeout(settings.getTimeout())
				.setRetryCount(settings.getNumRetries())
				.setTraceInQueryString(settings.getTraceInQueryString())
				.setDebugLog(settings.getDebugLog());
	}

	public DatarouterHttpClientBuilder forDatarouterHttpClientSettings(DatarouterHttpClientSettings settings){
		return this
				.forDatarouterHttpClientSettings((SimpleDatarouterHttpClientSettings)settings)
				.setUrlPrefix(settings::getEndpointUrl);
	}

	public DatarouterHttpClientBuilder forLinkSettings(DatarouterLinkSettings settings, String serviceName){
		return this.setUrlPrefix(settings.getLinkUrl(serviceName));
	}

}
