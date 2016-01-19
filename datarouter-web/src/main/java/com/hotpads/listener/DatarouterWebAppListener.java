package com.hotpads.listener;

import javax.servlet.ServletContext;

public abstract class DatarouterWebAppListener
extends DatarouterAppListener{

	protected ServletContext servletContext;
	
	
	protected void setServletContext(ServletContext servletContext){
		this.servletContext = servletContext;
	}

}
