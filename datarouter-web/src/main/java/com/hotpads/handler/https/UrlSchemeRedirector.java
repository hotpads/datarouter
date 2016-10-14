package com.hotpads.handler.https;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.port.CompoundPortIdentifier;
import com.hotpads.handler.port.PortIdentifier;
import com.hotpads.handler.port.PortIdentifier.TestPortIdentifier;
import com.hotpads.util.http.security.UrlScheme;

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
		STANDARD_PORTS.add(UrlScheme.PORT_HTTP_STANDARD);
		STANDARD_PORTS.add(UrlScheme.PORT_HTTPS_STANDARD);
	}

	public String getUriWithScheme(UrlScheme scheme, ServletRequest req) throws MalformedURLException{
		HttpServletRequest request = (HttpServletRequest)req;
		int port = request.getServerPort();
		String servletContextName = request.getContextPath();
		String path = request.getServletPath();
		String pathInfo = DrStringTool.nullSafe(request.getPathInfo());
		String queryString = DrStringTool.nullSafe(request.getQueryString());

		String contextSpecificPath = path + pathInfo;
		if(DrStringTool.notEmpty(queryString)){
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

	public static class Tests{
		private final UrlSchemeRedirector urlSchemeHandler = new UrlSchemeRedirector(new TestPortIdentifier());

		private final String urlHttp = "http://x.com",
				urlHttps = "https://x.com",
				param = "/y?z=0",
				urlWithHttpPort = urlHttp + ":" + UrlScheme.PORT_HTTP_DEV + param,
				urlWithHttpsPort = urlHttps + ":" + UrlScheme.PORT_HTTPS_DEV + param;

		@Test
		public void testGetRedirectUrlPortStringWithColon(){
			AssertJUnit.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP));
			AssertJUnit.assertEquals(":" + UrlScheme.PORT_HTTP_DEV, urlSchemeHandler.getRedirectUrlPortStringWithColon(
					UrlScheme.PORT_HTTP_DEV, UrlScheme.HTTP));
			AssertJUnit.assertEquals(":" + UrlScheme.PORT_HTTPS_DEV, urlSchemeHandler.getRedirectUrlPortStringWithColon(
					UrlScheme.PORT_HTTP_DEV, UrlScheme.HTTPS));
			AssertJUnit.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(
					UrlScheme.PORT_HTTPS_STANDARD, UrlScheme.HTTP));
		}

		@Test
		public void testGetUriWithScheme() throws Exception{
			AssertJUnit.assertEquals(urlHttp + param, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp
					+ param)));
			AssertJUnit.assertEquals(urlWithHttpPort, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(
					urlWithHttpPort)));
			AssertJUnit.assertEquals(urlWithHttpsPort, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(
					urlWithHttpPort)));
			AssertJUnit.assertEquals(urlHttps + param, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(
					urlHttp + param)));
			AssertJUnit.assertEquals(urlHttps, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp)));
			AssertJUnit.assertEquals(urlHttp, urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp)));
		}
	}
}