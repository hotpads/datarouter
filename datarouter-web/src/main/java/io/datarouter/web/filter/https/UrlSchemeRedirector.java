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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.web.port.PortIdentifier;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class UrlSchemeRedirector{

	private static final Set<Integer> STANDARD_PORTS = Set.of(
			UrlConstants.PORT_HTTP_STANDARD,
			UrlConstants.PORT_HTTPS_STANDARD);

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

	protected String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation() + "://" + url.getHost() + getRedirectUrlPortStringWithColon(url
				.getPort(), scheme) + url.getFile();
	}

	protected String getRedirectUrlPortStringWithColon(int originalPort, UrlScheme requiredScheme){
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

}
