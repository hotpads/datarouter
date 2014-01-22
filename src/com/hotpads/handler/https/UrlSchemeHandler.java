package com.hotpads.handler.https;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.handler.port.AbstractPortIdentificator;
import com.hotpads.util.core.StringTool;

public class UrlSchemeHandler{

	protected static AbstractPortIdentificator abstractPortIdentificator;

	public static UrlScheme fromRequest(ServletRequest req, AbstractPortIdentificator portIdentificator){
		String s = req.getScheme();
		abstractPortIdentificator = portIdentificator;
		if(UrlScheme.HTTPS.stringRepresentation.equals(s)){ return UrlScheme.HTTPS; }
		if(UrlScheme.HTTP.stringRepresentation.equals(s)){ return UrlScheme.HTTP; }
		return null;
	}

	public static final int PORT__STANDARD = 80, PORT_HTTPS_STANDARD = 443, PORT_HTTP_DEV = 8080;

	protected static Set<Integer> STANDARD_PORTS = new HashSet<Integer>();
	static{
		STANDARD_PORTS.add(80);
		STANDARD_PORTS.add(443);
	}

	public static String getUriWithScheme(UrlScheme scheme, ServletRequest req) throws MalformedURLException{
		HttpServletRequest request = (HttpServletRequest)req;
		int port = request.getServerPort();
		String servletContextName = request.getContextPath();
		String path = request.getServletPath();
		String pathInfo = StringTool.nullSafe(request.getPathInfo());
		String queryString = StringTool.nullSafe(request.getQueryString());

		String contextSpecificPath = path + pathInfo;
		if(StringTool.notEmpty(queryString)){
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

	private static String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation() + "://" + url.getHost()
				+ getRedirectUrlPortStringWithColon(url.getPort(), scheme) + url.getFile();
	}

	private static String getRedirectUrlPortStringWithColon(int originalPort, UrlScheme requiredScheme){
		if(originalPort == -1) return "";
		boolean standard = STANDARD_PORTS.contains(originalPort);
		if(UrlScheme.HTTP == requiredScheme){
			return standard ? "" : ":" + PORT_HTTP_DEV;
		}else if(UrlScheme.HTTPS == requiredScheme){ return standard ? "" : ":" +
				// "8445";}//
		abstractPortIdentificator.getHttpsPort(); }
		throw new IllegalArgumentException("UrlScheme.HTTPS filter is confused.  Terminating request.");
	}

	public static class Tests{
		@Test
		public void testGetRedirectUrlPortStringWithColon(){
			Assert.assertEquals("", getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP));
			Assert.assertEquals(":8080", getRedirectUrlPortStringWithColon(8080, UrlScheme.HTTP));
			Assert.assertEquals(":8443", getRedirectUrlPortStringWithColon(8080, UrlScheme.HTTPS));
			Assert.assertEquals("", getRedirectUrlPortStringWithColon(443, UrlScheme.HTTP));
		}

		@Test
		public void testGetUriWithScheme() throws Exception{
			Assert.assertEquals("UrlScheme.HTTP://x.com/y?z=0", getUriWithScheme(UrlScheme.HTTP, new URL(
					"UrlScheme.HTTP://x.com/y?z=0")));
			Assert.assertEquals("UrlScheme.HTTP://x.com:8080/y?z=0", getUriWithScheme(UrlScheme.HTTP, new URL(
					"UrlScheme.HTTP://x.com:8080/y?z=0")));
			Assert.assertEquals("UrlScheme.HTTPs://x.com:8443/y?z=0", getUriWithScheme(UrlScheme.HTTPS, new URL(
					"UrlScheme.HTTP://x.com:8080/y?z=0")));
			Assert.assertEquals("UrlScheme.HTTPs://x.com/y?z=0", getUriWithScheme(UrlScheme.HTTPS, new URL(
					"UrlScheme.HTTP://x.com/y?z=0")));
			Assert.assertEquals("UrlScheme.HTTPs://x.com", getUriWithScheme(UrlScheme.HTTPS, new URL(
					"UrlScheme.HTTP://x.com")));
			Assert.assertEquals("UrlScheme.HTTP://x.com", getUriWithScheme(UrlScheme.HTTP, new URL(
					"UrlScheme.HTTP://x.com")));
		}
	}

}
