package com.hotpads.handler.port;

import javax.servlet.ServletContext;

public abstract class AbstractPortIdentificator{

	protected ServletContext context;
	protected Integer httpsPort;

	public AbstractPortIdentificator(ServletContext context){
		this.context = context;
		this.setHttpsPort(initializeHttpsPort());
	}

	protected Integer initializeHttpsPort(){
		return new Integer(8443);
	}

	public Integer getHttpsPort(){
		return httpsPort;
	}

	public void setHttpsPort(Integer httpsPort){
		this.httpsPort = httpsPort;
	}
}
