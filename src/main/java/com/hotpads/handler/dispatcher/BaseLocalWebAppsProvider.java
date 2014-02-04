package com.hotpads.handler.dispatcher;

import javax.servlet.ServletContext;

import com.google.inject.Provider;
import com.hotpads.handler.BaseLocalWebapps;

public class BaseLocalWebAppsProvider implements Provider<BaseLocalWebapps>{

	private ServletContext context;
	public BaseLocalWebAppsProvider(ServletContext context){
		this.context=context;
	}
	@Override
	public BaseLocalWebapps get(){
		return new BaseLocalWebapps(context);
	}

}
