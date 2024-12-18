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
package io.datarouter.web.plugins.corsfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.web.config.settings.DatarouterLocalhostCorsFilterSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CorsFilter implements Filter{

	@Inject
	private DatarouterLocalhostCorsFilterSettings settings;
	@Inject
	private CorsOriginFilter originFilter;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		if(originFilter.matchOrigin(origin) && settings.allowed.get()){
			addCorsHeaders(response, origin);
			// preflight request
			if(request.getMethod().equals("OPTIONS")){
				count("preflight");
				response.setStatus(HttpStatus.SC_OK);
				return;
			}
			count("normal");
		}

		chain.doFilter(req, res);
	}

	private void count(String key){
		Metrics.count("CorsFilter " + key);
	}

	private void addCorsHeaders(HttpServletResponse response, String origin){
		response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", settings.methods.get()));
		response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
		response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", settings.headers.get()));
	}

}
