package com.hotpads;

import javax.servlet.ServletContext;

public abstract class HotPadsWebAppListener{

	protected ServletContext servletContext;
	
	void setServletContext(ServletContext servletContext){
		this.servletContext = servletContext;
	}

	abstract void onStartUp();

	abstract void onShutDown();

}
