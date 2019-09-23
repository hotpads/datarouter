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
package io.datarouter.web.filter.requestcaching;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.util.http.CachingHttpServletRequest;

public abstract class RequestCachingFilter implements Filter, InjectorRetriever{

	private DatarouterWebSettingRoot webSettingRoot;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		webSettingRoot = injector.getInstance(DatarouterWebSettingRoot.class);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws IOException, ServletException{
		ServletRequest wrappedRequest = wrap(request);
		chain.doFilter(wrappedRequest, response);
	}

	protected ServletRequest wrap(ServletRequest request){
		if(request instanceof HttpServletRequest){
			if(shouldCacheInputStream(request)){
				request = CachingHttpServletRequest.getOrCreate((HttpServletRequest)request);
			}
		}
		return request;
	}

	private boolean shouldCacheInputStream(ServletRequest request){
		return request.getContentLength() != -1
				&& request.getContentLength() < webSettingRoot.maxCacheableContentLength.get();
	}

}
