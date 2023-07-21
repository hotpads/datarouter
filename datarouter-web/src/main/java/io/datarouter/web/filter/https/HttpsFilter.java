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
package io.datarouter.web.filter.https;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.HttpsConfiguration;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HttpsFilter implements Filter{

	@Inject
	protected HttpsConfiguration httpsConfiguration;
	@Inject
	protected UrlSchemeRedirector urlSchemeHandler;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		/*------------------------- parse request ---------------------------*/

		UrlScheme scheme = urlSchemeHandler.fromRequest(req);

		/*------- catch bogus http requests and redirect if necessary -------*/

		UrlScheme requiredScheme = httpsConfiguration.getRequiredScheme(RequestTool.getPath(request));

		if(requiredScheme != null && requiredScheme != UrlScheme.ANY && requiredScheme != scheme){
			String redirectUrl = urlSchemeHandler.getUriWithScheme(requiredScheme, req);
			redirectUrl = response.encodeRedirectURL(redirectUrl);
			redirect(request, response, redirectUrl);
			return;
		}

		if(httpsConfiguration.shouldSetHsts()){
			// browsers will force https after receiving this header - expires after 31536000 seconds (1 year)
			response.setHeader("Strict-Transport-Security", "max-age=31536000");
		}

		/*---------- no bogus requests found... continue normally -----------*/
		fc.doFilter(req, res);
	}

	protected void redirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException{
		if(!"GET".equals(request.getMethod()) && getHttpVersion(request) >= 1.1d){
			// preserve request type
			sendTemporaryRedirect(response, url);
		}else{
			sendTemporaryGetRedirect(response, url);
		}
	}

	protected void sendTemporaryGetRedirect(HttpServletResponse response, String url) throws IOException{
		response.sendRedirect(url);
	}

	protected void sendTemporaryRedirect(HttpServletResponse response, String url){
		response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
		response.setHeader("Location", url);
	}

	protected void sendPermanentRedirect(HttpServletResponse response, String url){
		response.setStatus(308);
		response.setHeader("Location", url);
	}

	private double getHttpVersion(HttpServletRequest request){
		String protocol = request.getProtocol();
		String version = StringTool.getStringAfterLastOccurrence("/", protocol);
		return NumberTool.getDoubleNullSafe(version, 0d);
	}

}
