package com.hotpads.handler.dispatcher;

import com.google.inject.Injector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.admin.DatarouterRoutersAndClientsHandler;
import com.hotpads.handler.admin.DrDefaultHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;
import com.hotpads.handler.httpclient.DataRouterHttpClientHandler;

public class DataRouterDispatcher extends BaseDispatcher{

	private static final String ROUTERS = "/routers";
	public static final String URL_STACKTRACES = "/stackTraces";
	public static final String URL_DR = "/datarouter";
	public static final String URL_DATAROUTER_VIEW_NODE_DATA = URL_DR + "/viewNodeData";
	public static final String URL_HTTP_CLIENT = URL_DR + "/httpNode";


	public DataRouterDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		// DataRouter
		handle(URL_HTTP_CLIENT, DataRouterHttpClientHandler.class);
		handle(URL_DATAROUTER_VIEW_NODE_DATA, ViewNodeDataHandler.class);
		handle(URL_DR + URL_STACKTRACES, StackTracesManagerHandler.class);
		handle(URL_DR + ROUTERS, DatarouterRoutersAndClientsHandler.class);
		handle(URL_DR + "*", DrDefaultHandler.class);

	}

}
