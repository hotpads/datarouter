package com.hotpads.handler.port;

import com.hotpads.util.http.security.UrlScheme;


public interface PortIdentifier{
	
	Integer getHttpPort();
	Integer getHttpsPort();
	
	
	
	
	/************ fixed class for testing ***************/
	
	public static class TestPortIdentifier implements PortIdentifier{
		@Override
		public Integer getHttpPort(){
			return UrlScheme.PORT_HTTP_DEV;
		}
		@Override
		public Integer getHttpsPort(){
			return UrlScheme.PORT_HTTPS_DEV;
		}
	}
	
	
}
