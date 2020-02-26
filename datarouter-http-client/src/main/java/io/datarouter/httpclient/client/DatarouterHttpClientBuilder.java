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
package io.datarouter.httpclient.client;

import java.lang.StackWalker.Option;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.httpclient.json.HttpClientGsonTool;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.SignatureGenerator;

public class DatarouterHttpClientBuilder{

	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);

	private static final JsonSerializer DEFAULT_SERIALIZER = new GsonJsonSerializer(HttpClientGsonTool.GSON);
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

	private int timeoutMs; // must be int due to RequestConfig.set*Timeout() methods
	private int maxTotalConnections;
	private int maxConnectionsPerRoute;
	private Optional<Integer> validateAfterInactivityMs;
	private HttpClientBuilder httpClientBuilder;
	private Supplier<Integer> retryCount;
	private JsonSerializer jsonSerializer;
	private CloseableHttpClient customHttpClient;
	private SignatureGenerator signatureGenerator;
	private CsrfGenerator csrfGenerator;
	private Supplier<String> apiKeySupplier;
	private DatarouterHttpClientConfig config;
	private boolean ignoreSsl;
	private SSLContext customSslContext;
	private String name;
	private Supplier<Boolean> enableBreakers;

	public DatarouterHttpClientBuilder(){
		this.timeoutMs = (int)DEFAULT_TIMEOUT.toMillis();
		this.maxTotalConnections = 100;
		this.maxConnectionsPerRoute = 100;
		this.validateAfterInactivityMs = Optional.empty();
		this.httpClientBuilder = HttpClientBuilder.create()
				.setRedirectStrategy(LaxRedirectStrategy.INSTANCE);
		this.retryCount = () -> HttpRetryTool.DEFAULT_RETRY_COUNT;
		this.name = STACK_WALKER.getCallerClass().getSimpleName();
	}

	public DatarouterHttpClient build(){
		httpClientBuilder.setRetryHandler(new DatarouterHttpRetryHandler(retryCount));
		httpClientBuilder.setServiceUnavailableRetryStrategy(new DatarouterServiceUnavailableRetryStrategy(retryCount));
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.STANDARD)
				.setConnectTimeout(timeoutMs)
				.setConnectionRequestTimeout(timeoutMs)
				.setSocketTimeout(timeoutMs)
				.build();
		httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		httpClientBuilder.setKeepAliveStrategy(new DatarouterConnectionKeepAliveStrategy(Duration.ofHours(1)));
		PoolingHttpClientConnectionManager connectionManager;
		if(ignoreSsl || customSslContext != null){
			SSLConnectionSocketFactory sslsf;
			if(ignoreSsl){
				try{
					SSLContextBuilder builder = new SSLContextBuilder();
					builder.loadTrustMaterial(null, (chain, authType) -> true);
					sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
				}catch(KeyManagementException | KeyStoreException | NoSuchAlgorithmException e){
					throw new RuntimeException(e);
				}
			}else{
				sslsf = new SSLConnectionSocketFactory(customSslContext, SSLConnectionSocketFactory
						.getDefaultHostnameVerifier());
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
		if(validateAfterInactivityMs.isPresent()){
			connectionManager.setValidateAfterInactivity(validateAfterInactivityMs.get());
		}
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
		if(jsonSerializer == null){
			jsonSerializer = DEFAULT_SERIALIZER;
		}
		if(enableBreakers == null){
			enableBreakers = () -> false;
		}
		return new StandardDatarouterHttpClient(builtHttpClient, this.jsonSerializer, this.signatureGenerator,
				this.csrfGenerator, this.apiKeySupplier, this.config, connectionManager, name, enableBreakers);
	}

	public DatarouterHttpClientBuilder setRetryCount(Supplier<Integer> retryCount){
		if(customHttpClient != null){
			throw new UnsupportedOperationException("You cannot change the retry count of a custom http client");
		}
		this.retryCount = retryCount;
		return this;
	}

	public DatarouterHttpClientBuilder setJsonSerializer(JsonSerializer jsonSerializer){
		this.jsonSerializer = jsonSerializer;
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

	public DatarouterHttpClientBuilder setTimeout(Duration timeout){
		this.timeoutMs = (int)timeout.toMillis();
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

	public DatarouterHttpClientBuilder setValidateAfterInactivityMs(int validateAfterInactivityMs){
		this.validateAfterInactivityMs = Optional.of(validateAfterInactivityMs);
		return this;
	}

	public DatarouterHttpClientBuilder setEnableBreakers(Supplier<Boolean> enableBreakers){
		this.enableBreakers = enableBreakers;
		return this;
	}

	public DatarouterHttpClientBuilder forDatarouterHttpClientSettings(DatarouterHttpClientSettings settings){
		return this
				.setTimeout(settings.getTimeout())
				.setRetryCount(settings.getNumRetries())
				.setEnableBreakers(settings.getEnableBreakers());
	}

}
