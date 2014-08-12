package com.hotpads;

import javax.servlet.ServletContext;

public abstract class HotPadsWebAppListener{

	protected ServletContext servletContext;
	
	protected void setServletContext(ServletContext servletContext){
		this.servletContext = servletContext;
	}

	protected abstract void onStartUp();

	protected abstract void onShutDown();

}
