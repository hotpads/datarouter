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
package io.datarouter.web.filter.https;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.web.port.PortIdentifier;
import io.datarouter.web.port.PortIdentifier.TestPortIdentifier;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class UrlSchemeRedirector{

	private final PortIdentifier portIdentifier;

	@Inject
	public UrlSchemeRedirector(@Named(CompoundPortIdentifier.COMPOUND_PORT_IDENTIFIER) PortIdentifier portIdentifier){
		this.portIdentifier = portIdentifier;
	}

	public UrlScheme fromRequest(ServletRequest req){
		String scheme = req.getScheme();
		if(UrlScheme.HTTPS.getStringRepresentation().equals(scheme)){
			return UrlScheme.HTTPS;
		}
		if(UrlScheme.HTTP.getStringRepresentation().equals(scheme)){
			return UrlScheme.HTTP;
		}
		return null;
	}

	private static final Set<Integer> STANDARD_PORTS = new HashSet<>();
	static{
		STANDARD_PORTS.add(UrlConstants.PORT_HTTP_STANDARD);
		STANDARD_PORTS.add(UrlConstants.PORT_HTTPS_STANDARD);
	}

	public String getUriWithScheme(UrlScheme scheme, ServletRequest req) throws MalformedURLException{
		HttpServletRequest request = (HttpServletRequest)req;
		int port = request.getServerPort();
		String servletContextName = request.getContextPath();
		String queryString = StringTool.nullSafe(request.getQueryString());

		String contextSpecificPath = RequestTool.getPath(request);
		if(StringTool.notEmpty(queryString)){
			contextSpecificPath += "?" + queryString;
		}

		URL url = new URL(scheme.getStringRepresentation(), req.getServerName(), port, servletContextName
				+ contextSpecificPath);
		return getUriWithScheme(scheme, url);
		// return scheme.getStringRepresentation()+"://"
		// + req.getServerName()
		// + getRedirectUrlPortStringWithColon(port, scheme)
		// + servletContextName
		// + contextSpecificPath;
	}

	private String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation() + "://" + url.getHost() + getRedirectUrlPortStringWithColon(url
				.getPort(), scheme) + url.getFile();
	}

	private String getRedirectUrlPortStringWithColon(int originalPort, UrlScheme requiredScheme){
		if(originalPort == -1){
			return "";
		}
		boolean standard = STANDARD_PORTS.contains(originalPort);
		if(UrlScheme.HTTP == requiredScheme){
			return standard ? "" : ":" + portIdentifier.getHttpPort();
		}else if(UrlScheme.HTTPS == requiredScheme){
			return standard ? "" : ":" + portIdentifier.getHttpsPort();
		}
		throw new IllegalArgumentException("UrlScheme.HTTPS filter is confused.  Terminating request.");
	}

	public static class UrlSchemeRedirectorTests{
		private final UrlSchemeRedirector urlSchemeHandler = new UrlSchemeRedirector(new TestPortIdentifier());

		private final String urlHttp = "http://x.com",
				urlHttps = "https://x.com",
				param = "/y?z=0",
				urlWithHttpPort = urlHttp + ":" + UrlConstants.PORT_HTTP_DEV + param,
				urlWithHttpsPort = urlHttps + ":" + UrlConstants.PORT_HTTPS_DEV + param;

		@Test
		public void testGetRedirectUrlPortStringWithColon(){
			Assert.assertEquals(urlSchemeHandler.getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP), "");
			Assert.assertEquals(urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTP_DEV,
					UrlScheme.HTTP), ":" + UrlConstants.PORT_HTTP_DEV);
			Assert.assertEquals(urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTP_DEV,
					UrlScheme.HTTPS), ":" + UrlConstants.PORT_HTTPS_DEV);
			Assert.assertEquals(urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTPS_STANDARD,
					UrlScheme.HTTP), "");
		}

		@Test
		public void testGetUriWithScheme() throws Exception{
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp + param)), urlHttp
					+ param);
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlWithHttpPort)),
					urlWithHttpPort);
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlWithHttpPort)),
					urlWithHttpsPort);
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp + param)), urlHttps
					+ param);
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp)), urlHttps);
			Assert.assertEquals(urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp)), urlHttp);
		}
	}
}