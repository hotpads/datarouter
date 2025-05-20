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

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.security.CsrfGenerator;
import io.datarouter.httpclient.security.CsrfGenerator.RefreshableCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.httpclient.security.SignatureGenerator;
import io.datarouter.httpclient.security.SignatureGenerator.RefreshableSignatureGenerator;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.refreshable.RefreshableSupplier;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.json.JsonSerializer;

public abstract class BaseHttpClient implements HttpConfig{
	private static final Logger logger = LoggerFactory.getLogger(BaseHttpClient.class);

	private static final String METRIC_PREFIX_EXCEPTION = "Exception";
	private static final String METRIC_SCOPE_ALL = "all";
	private static final String METRIC_SCOPE_NAME = "name";
	private static final String EXCEPTION_CATEGORY_CLIENT = "CLIENT";

	protected final String clientName;
	protected final CloseableHttpClient httpClient;
	protected final JsonSerializer jsonSerializer;
	protected final SignatureGenerator signatureGenerator;
	protected final CsrfGenerator csrfGenerator;
	protected final Supplier<String> apiKeySupplier;
	protected final RefreshableSignatureGenerator refreshableSignatureGenerator;
	protected final RefreshableCsrfGenerator refreshableCsrfGenerator;
	protected final RefreshableSupplier<String> refreshableApiKeySupplier;
	protected final DatarouterHttpClientConfig config;
	protected final PoolingHttpClientConnectionManager connectionManager;
	protected final Supplier<URI> urlPrefix;
	protected final Supplier<Boolean> traceInQueryString;
	protected final Supplier<Boolean> debugLog;
	protected final String apiKeyFieldName;
	protected final String simpleClassName;

	BaseHttpClient(
			String clientName,
			CloseableHttpClient httpClient,
			JsonSerializer jsonSerializer,
			SignatureGenerator signatureGenerator,
			CsrfGenerator csrfGenerator,
			Supplier<String> apiKeySupplier,
			RefreshableSignatureGenerator refreshableSignatureGenerator,
			RefreshableCsrfGenerator refreshableCsrfGenerator,
			RefreshableSupplier<String> refreshableApiKeySupplier,
			DatarouterHttpClientConfig config,
			PoolingHttpClientConnectionManager connectionManager,
			String simpleClassName,
			Supplier<URI> urlPrefix,
			Supplier<Boolean> traceInQueryString,
			Supplier<Boolean> debugLog,
			String apiKeyFieldName){
		this.clientName = clientName;
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureGenerator = signatureGenerator;
		this.csrfGenerator = csrfGenerator;
		this.apiKeySupplier = apiKeySupplier;
		this.refreshableSignatureGenerator = refreshableSignatureGenerator;
		this.refreshableCsrfGenerator = refreshableCsrfGenerator;
		this.refreshableApiKeySupplier = refreshableApiKeySupplier;
		this.config = config;
		this.connectionManager = connectionManager;
		this.simpleClassName = simpleClassName;
		this.urlPrefix = urlPrefix;
		this.traceInQueryString = traceInQueryString;
		this.debugLog = debugLog;
		this.apiKeyFieldName = apiKeyFieldName;
	}

	protected DatarouterHttpResponse executeCheckedInternal(
			DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException{
		setSecurityProperties(request);

		HttpClientContext context = new HttpClientContext();
		context.setAttribute(HttpRetryTool.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		CookieStore cookieStore = new BasicCookieStore();
		for(BasicClientCookie cookie : request.getCookies()){
			cookieStore.addCookie(cookie);
		}
		context.setCookieStore(cookieStore);
		return DatarouterHttpCallTool.call(
				simpleClassName,
				httpClient,
				request,
				httpEntityConsumer,
				context,
				traceInQueryString,
				debugLog);
	}

	protected void setSecurityProperties(DatarouterHttpRequest request){
		if(request.getShouldSkipSecurity()){
			//the only case from below that is relevant without security is populating the entity
			if(request.canHaveEntity() && request.getEntity() == null){
				request.setEntity(request.getFirstPostParams());
			}
			return;
		}
		SignatureGenerator signatureGenerator = chooseSignatureGenerator();
		CsrfGenerator csrfGenerator = chooseCsrfGenerator();
		Supplier<String> apiKeySupplier = chooseApiKeySupplier();

		Map<String,String> params = new HashMap<>();
		if(csrfGenerator != null){
			String csrfIv = csrfGenerator.generateCsrfIv();
			params.put(SecurityParameters.CSRF_IV, csrfIv);
			params.put(SecurityParameters.CSRF_TOKEN, csrfGenerator.generateCsrfToken(csrfIv));
		}

		if(apiKeySupplier != null){
			params.put(apiKeyFieldName, apiKeySupplier.get());
		}
		if(request.canHaveEntity() && request.getEntity() == null){
			params = request.addPostParams(params).getFirstPostParams();
			if(signatureGenerator != null && !params.isEmpty()){
				String signature = signatureGenerator.getHexSignature(request.getFirstPostParams()).signature;
				request.addPostParam(SecurityParameters.SIGNATURE, signature);
			}
			request.setEntity(request.getFirstPostParams());
		}else if(request.getMethod() == HttpRequestMethod.GET){
			params = request.addGetParams(params).getFirstGetParams();
			if(signatureGenerator != null && !params.isEmpty()){
				String signature = signatureGenerator.getHexSignature(request.getFirstGetParams()).signature;
				request.addGetParam(SecurityParameters.SIGNATURE, signature);
			}
		}else{
			request.addHeaders(params);
			if(signatureGenerator != null && request.getEntity() != null){
				String signature = signatureGenerator.getHexSignature(
						request.getFirstGetParams(),
						request.getEntity()).signature;
				request.addHeader(SecurityParameters.SIGNATURE, signature);
			}
		}
	}

	private Supplier<String> chooseApiKeySupplier(){
		return refreshableApiKeySupplier != null ? refreshableApiKeySupplier : apiKeySupplier;
	}

	private SignatureGenerator chooseSignatureGenerator(){
		return refreshableSignatureGenerator != null ? refreshableSignatureGenerator : signatureGenerator;
	}

	private CsrfGenerator chooseCsrfGenerator(){
		return refreshableCsrfGenerator != null ? refreshableCsrfGenerator : csrfGenerator;
	}

	protected boolean shouldRerun40x(Instant previous, int statusCode, boolean shouldSkipSecurity){
		if(HttpStatus.SC_UNAUTHORIZED != statusCode && HttpStatus.SC_FORBIDDEN != statusCode || shouldSkipSecurity){
			return false;
		}
		return refreshableSignatureGenerator != null && !refreshableSignatureGenerator.refresh().isBefore(previous)
				|| refreshableCsrfGenerator != null && !refreshableCsrfGenerator.refresh().isBefore(previous)
				|| refreshableApiKeySupplier != null && !refreshableApiKeySupplier.refresh().isBefore(previous);
	}

	@Override
	public void shutdown(){
		try{
			httpClient.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	private static void forceAbortRequestUnchecked(HttpRequestBase internalHttpRequest){
		try{
			internalHttpRequest.abort();
		}catch(Exception e){
			logger.error("aborting internal http request failed", e);
		}
	}

	public PoolStats getPoolStats(){
		return connectionManager.getTotalStats();
	}

	@Override
	public CloseableHttpClient getApacheHttpClient(){
		return httpClient;
	}

	@Override
	public JsonSerializer getJsonSerializer(){
		return jsonSerializer;
	}

	protected <E> E deserializeEntity(String entity, Type deserializeToType){
		long length = entity == null ? 0 : entity.length();
		try(var _ = TracerTool.startSpan("JsonSerializer deserialize", TraceSpanGroupType.SERIALIZATION)){
			TracerTool.appendToSpanInfo("characters", length);
			return jsonSerializer.deserialize(entity, deserializeToType);
		}
	}

	protected void onException(){
		logger.warn("error clientName={}", clientName);

		String scopeAll = String.join(
				" ",
				METRIC_PREFIX_EXCEPTION,
				EXCEPTION_CATEGORY_CLIENT,
				METRIC_SCOPE_ALL);
		Metrics.count(scopeAll);

		String scopeName = String.join(
				" ",
				METRIC_PREFIX_EXCEPTION,
				EXCEPTION_CATEGORY_CLIENT,
				METRIC_SCOPE_NAME,
				clientName);
		Metrics.count(scopeName);
	}

}
