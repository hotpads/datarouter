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

import org.junit.Assert;
import org.junit.Test;

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
		String s = req.getScheme();
		if(UrlScheme.HTTPS.getStringRepresentation().equals(s)){ return UrlScheme.HTTPS; }
		if(UrlScheme.HTTP.getStringRepresentation().equals(s)){ return UrlScheme.HTTP; }
		return null;
	}

	protected static Set<Integer> STANDARD_PORTS = new HashSet<Integer>();
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

		URL u = new URL(scheme.getStringRepresentation(), req.getServerName(), port, servletContextName
				+ contextSpecificPath);
		return getUriWithScheme(scheme, u);
		// return scheme.getStringRepresentation()+"://"
		// + req.getServerName()
		// + getRedirectUrlPortStringWithColon(port, scheme)
		// + servletContextName
		// + contextSpecificPath;
	}

	private String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation() + "://" + url.getHost()
				+ getRedirectUrlPortStringWithColon(url.getPort(), scheme) + url.getFile();
	}

	private String getRedirectUrlPortStringWithColon(int originalPort, UrlScheme requiredScheme){
		if(originalPort == -1) return "";
		boolean standard = STANDARD_PORTS.contains(originalPort);
		if(UrlScheme.HTTP == requiredScheme){
			return standard ? "" : ":" + UrlScheme.PORT_HTTP_DEV;
		}else if(UrlScheme.HTTPS == requiredScheme){ return standard ? "" : ":" +
		getPortIdentifier().getHttpsPort(); }
		throw new IllegalArgumentException("UrlScheme.HTTPS filter is confused.  Terminating request.");
	}

	public PortIdentifier getPortIdentifier(){
		return portIdentifier;
	}

	public static class Tests{
		
		private UrlSchemeRedirector urlSchemeHandler = new UrlSchemeRedirector(new TestPortIdentifier());
		
		String urlHttp = "http://x.com";
		String urlHttps = "https://x.com";
		String param = "/y?z=0";
		String urlWithHttpPort = urlHttp + ":" + UrlScheme.PORT_HTTP_DEV + param;
		String urlWithHttpsPort = urlHttps + ":" + UrlScheme.PORT_HTTPS_DEV + param;
		
		@Test
		public void testGetRedirectUrlPortStringWithColon(){
			Assert.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP));
			Assert.assertEquals(":" + UrlScheme.PORT_HTTP_DEV, urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlScheme.PORT_HTTP_DEV, UrlScheme.HTTP));
			Assert.assertEquals(":" + UrlScheme.PORT_HTTPS_DEV, urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlScheme.PORT_HTTP_DEV, UrlScheme.HTTPS));
			Assert.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(UrlScheme.PORT_HTTPS_STANDARD, UrlScheme.HTTP));
		}

		@Test public void testGetUriWithScheme() throws Exception{
			Assert.assertEquals(urlHttp + param, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp + param)));
			Assert.assertEquals(urlWithHttpPort, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlWithHttpPort)));
			Assert.assertEquals(urlWithHttpsPort, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlWithHttpPort)));
			Assert.assertEquals(urlHttps + param, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp + param)));
			Assert.assertEquals(urlHttps, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp)));
			Assert.assertEquals(urlHttp, 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp)));
		}
	}
}