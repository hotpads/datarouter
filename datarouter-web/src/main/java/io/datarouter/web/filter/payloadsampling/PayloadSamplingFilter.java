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
package io.datarouter.web.filter.payloadsampling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.conveyor.InMemorySampledPayloadPaths;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSample;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSampleDao;
import io.datarouter.web.storage.payloadsampling.response.ResponsePayloadSample;
import io.datarouter.web.storage.payloadsampling.response.ResponsePayloadSampleDao;
import io.datarouter.web.util.http.CachingHttpServletRequest;
import io.datarouter.web.util.http.CachingHttpServletResponse;

public abstract class PayloadSamplingFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(PayloadSamplingFilter.class);

	public static final int REQUEST_PARAM_MAP_JSON_LEN = 1_000;

	private DatarouterWebSettingRoot webSettingRoot;
	private InMemorySampledPayloadPaths buffer;
	private RequestPayloadSampleDao requestPayloadSampleDao;
	private ResponsePayloadSampleDao responsePayloadSampleDao;
	private ServerName serverName;
	private Gson gson;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		webSettingRoot = injector.getInstance(DatarouterWebSettingRoot.class);
		buffer = injector.getInstance(InMemorySampledPayloadPaths.class);
		requestPayloadSampleDao = injector.getInstance(RequestPayloadSampleDao.class);
		responsePayloadSampleDao = injector.getInstance(ResponsePayloadSampleDao.class);
		serverName = injector.getInstance(ServerName.class);
		gson = injector.getInstance(Gson.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
	throws IOException, ServletException{
		if(!webSettingRoot.enablePayloadSampling.get()){
			chain.doFilter(req, res);
			return;
		}

		boolean shouldSampleRequest = false;
		HttpServletRequest requestWrapper = (HttpServletRequest)req;
		if(req.getContentLength() < webSettingRoot.maxCacheableContentLength.get()){
			requestWrapper = CachingHttpServletRequest.getOrCreate(requestWrapper);
			shouldSampleRequest = true;
		}else{
			logger.warn("request content size too big, uri={}, size={}", requestWrapper.getRequestURI(), requestWrapper
					.getContentLength());
		}

		var responseWrapper = new CachingHttpServletResponse((HttpServletResponse)res);
		boolean errored = true;
		try{
			chain.doFilter(requestWrapper, responseWrapper);
			responseWrapper.flushBuffer();
			errored = false;
		}finally{
			if(!errored){
				String requestUri = requestWrapper.getRequestURI().toString();
				var key = new PayloadSampleKey(requestUri, serverName.get());
				String requestParamMap = gson.toJson(requestWrapper.getParameterMap());
				boolean added = buffer.add(requestUri);
				if(added && shouldSampleRequest && responseWrapper.isOutputStreamCached() && requestParamMap
						.length() < REQUEST_PARAM_MAP_JSON_LEN){
					String requestEncoding = Optional.ofNullable(requestWrapper.getCharacterEncoding())
							.orElse(StandardCharsets.UTF_8.name());
					requestPayloadSampleDao.put(new RequestPayloadSample(
							key,
							requestParamMap,
							((CachingHttpServletRequest)requestWrapper).getContent(),
							requestEncoding));
					responsePayloadSampleDao.put(new ResponsePayloadSample(
							key,
							responseWrapper.getCopy(),
							responseWrapper.getCharacterEncoding()));
				}
				if(!responseWrapper.isOutputStreamCached()){
					logger.warn("response body size too bigger than 4MB, uri={}", requestWrapper.getRequestURI());
				}
			}
		}
	}

}
