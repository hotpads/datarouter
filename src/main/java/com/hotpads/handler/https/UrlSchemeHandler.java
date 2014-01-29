package com.hotpads.handler.https;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.handler.port.PortIdentifier;
import com.hotpads.handler.port.PortIdentifier.TestPortIdentifier;
import com.hotpads.util.core.StringTool;

@Singleton
public class UrlSchemeHandler{

	private PortIdentifier portIdentifier;
	
	
	@Inject
	public UrlSchemeHandler(PortIdentifier portIdentifier){
		this.setPortIdentifier(portIdentifier);
	}
	
	public UrlScheme fromRequest(ServletRequest req){
		String s = req.getScheme();
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

	public String getUriWithScheme(UrlScheme scheme, ServletRequest req) throws MalformedURLException{
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

	private String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation() + "://" + url.getHost()
				+ getRedirectUrlPortStringWithColon(url.getPort(), scheme) + url.getFile();
	}

	private String getRedirectUrlPortStringWithColon(int originalPort, UrlScheme requiredScheme){
		if(originalPort == -1) return "";
		boolean standard = STANDARD_PORTS.contains(originalPort);
		if(UrlScheme.HTTP == requiredScheme){
			return standard ? "" : ":" + PORT_HTTP_DEV;
		}else if(UrlScheme.HTTPS == requiredScheme){ return standard ? "" : ":" +
		getPortIdentifier().getHttpsPort(); }
		throw new IllegalArgumentException("UrlScheme.HTTPS filter is confused.  Terminating request.");
	}

	public PortIdentifier getPortIdentifier(){
		return portIdentifier;
	}

	public void setPortIdentifier(PortIdentifier portIdentifier){
		this.portIdentifier = portIdentifier;
	}

	public static class Tests{
		
		private UrlSchemeHandler urlSchemeHandler = new UrlSchemeHandler(new TestPortIdentifier());
		
		@Test
		public void testGetRedirectUrlPortStringWithColon(){
			Assert.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP));
			Assert.assertEquals(":8080", urlSchemeHandler.getRedirectUrlPortStringWithColon(8080, UrlScheme.HTTP));
			Assert.assertEquals(":8443", urlSchemeHandler.getRedirectUrlPortStringWithColon(8080, UrlScheme.HTTPS));
			Assert.assertEquals("", urlSchemeHandler.getRedirectUrlPortStringWithColon(443, UrlScheme.HTTP));
		}

		@Test public void testGetUriWithScheme() throws Exception{
			Assert.assertEquals("http://x.com/y?z=0", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL("http://x.com/y?z=0")));
			Assert.assertEquals("http://x.com:8080/y?z=0", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL("http://x.com:8080/y?z=0")));
			Assert.assertEquals("https://x.com:8443/y?z=0", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL("http://x.com:8080/y?z=0")));
			Assert.assertEquals("https://x.com/y?z=0", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL("http://x.com/y?z=0")));
			Assert.assertEquals("https://x.com", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTPS, new URL("http://x.com")));
			Assert.assertEquals("http://x.com", 
					urlSchemeHandler.getUriWithScheme(UrlScheme.HTTP, new URL("http://x.com")));
		}
	}
}