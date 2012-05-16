package com.hotpads.handler.datarouter.dispatcher;

import com.google.inject.Injector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;
import com.hotpads.handler.mav.Mav;


public class RootDispatcher extends BaseDispatcher{
	
	public static final String 
		PATH_viewNodeData = "viewNodeData";

	
	public RootDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handle(PATH_viewNodeData, ViewNodeDataHandler.class);
		
	}
	
}
