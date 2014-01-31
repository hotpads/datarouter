package com.hotpads.handler.port;


public interface PortIdentifier{
	
	Integer getHttpPort();
	Integer getHttpsPort();
	
	
	
	
	/************ fixed class for testing ***************/
	
	public static class TestPortIdentifier implements PortIdentifier{
		@Override
		public Integer getHttpPort(){
			return 8080;
		}
		@Override
		public Integer getHttpsPort(){
			return 8443;
		}
	}
	
	
}
