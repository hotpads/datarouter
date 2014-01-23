package com.hotpads.handler.https;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.StringTool;

public enum UrlScheme {
	HTTP("http"), 
	HTTPS("https"),
	ANY("any");
	
	protected final String stringRepresentation;
	
	private UrlScheme(String stringRepresentation){
		this.stringRepresentation = stringRepresentation;
	}
	
	public static UrlScheme fromRequest(ServletRequest req){
		String s = req.getScheme();
		if(HTTPS.stringRepresentation.equals(s)){ return HTTPS; }
		if(HTTP.stringRepresentation.equals(s)){ return HTTP; }
		return null;
	}

	public String getStringRepresentation() {
		return stringRepresentation;
	}
	
	public static final int 
	PORT_HTTP_STANDARD = 80,
	PORT_HTTPS_STANDARD = 443,
	PORT_HTTP_DEV = 8080,
	PORT_HTTPS_DEV = 8443;

	protected static Set<Integer> STANDARD_PORTS = new HashSet<Integer>();
	static{
		STANDARD_PORTS.add(80);
		STANDARD_PORTS.add(443);
	}
	
	public static String getUriWithScheme(
			UrlScheme scheme, ServletRequest req) throws MalformedURLException{
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

		URL u = new URL(scheme.getStringRepresentation(), 
						req.getServerName(), 
						port, 
						servletContextName+contextSpecificPath);
		return getUriWithScheme(scheme,u);
//		return scheme.getStringRepresentation()+"://" 
//			+ req.getServerName()
//			+ getRedirectUrlPortStringWithColon(port, scheme) 
//			+ servletContextName
//			+ contextSpecificPath;
	}
	private static String getUriWithScheme(UrlScheme scheme, URL url){
		return scheme.getStringRepresentation()+"://" 
			+ url.getHost()
			+ getRedirectUrlPortStringWithColon(url.getPort(), scheme) 
			+ url.getFile();
	}
	private static String getRedirectUrlPortStringWithColon(
			int originalPort, UrlScheme requiredScheme){
		if(originalPort==-1) return "";
		boolean standard = STANDARD_PORTS.contains(originalPort);
		if(HTTP == requiredScheme){
			return standard ? "" : ":"+PORT_HTTP_DEV;
		}else if(HTTPS == requiredScheme){
			return standard ? "" : ":"+PORT_HTTPS_DEV;
		}
		throw new IllegalArgumentException(
				"HTTPS filter is confused.  Terminating request.");
	}
	
	public static class Tests {
		@Test public void testGetRedirectUrlPortStringWithColon(){
			Assert.assertEquals("",
					getRedirectUrlPortStringWithColon(80, HTTP));
			Assert.assertEquals(":8080",
					getRedirectUrlPortStringWithColon(8080, HTTP));
			Assert.assertEquals(":8443",
					getRedirectUrlPortStringWithColon(8080, HTTPS));
			Assert.assertEquals("",
					getRedirectUrlPortStringWithColon(443, HTTP));
		}
		@Test public void testGetUriWithScheme() throws Exception{
			Assert.assertEquals("http://x.com/y?z=0", 
					getUriWithScheme(HTTP, new URL("http://x.com/y?z=0")));
			Assert.assertEquals("http://x.com:8080/y?z=0", 
					getUriWithScheme(HTTP, new URL("http://x.com:8080/y?z=0")));
			Assert.assertEquals("https://x.com:8443/y?z=0", 
					getUriWithScheme(HTTPS, new URL("http://x.com:8080/y?z=0")));
			Assert.assertEquals("https://x.com/y?z=0", 
					getUriWithScheme(HTTPS, new URL("http://x.com/y?z=0")));
			Assert.assertEquals("https://x.com", 
					getUriWithScheme(HTTPS, new URL("http://x.com")));
			Assert.assertEquals("http://x.com", 
					getUriWithScheme(HTTP, new URL("http://x.com")));
		}
	}
}