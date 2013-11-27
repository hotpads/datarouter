package com.hotpads.handler.dispatcher;

import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.http.DataRouterHttpClientHandler;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.admin.DatarouterRoutersAndClientsHandler;
import com.hotpads.handler.admin.DrDefaultHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.hbase.HBaseHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;

public class DataRouterDispatcher extends BaseDispatcher{

	private static final String ROUTERS = "/routers";
	public static final String URL_STACKTRACES = "/stackTraces";
	public static final String URL_DATAROUTER = "/datarouter";
	public static final String URL_DATAROUTER_API = "/datarouterApi";
	public static final String URL_DATAROUTER_VIEW_NODE_DATA = URL_DATAROUTER + "/viewNodeData";
	public static final String URL_HTTP_CLIENT = URL_DATAROUTER_API + "/httpNode";


	public DataRouterDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		// DataRouter
//		handle(URL_HTTP_CLIENT, DataRouterHttpClientHandler.class);
		handle(URL_DATAROUTER_VIEW_NODE_DATA, ViewNodeDataHandler.class);
		handle(URL_DATAROUTER + URL_STACKTRACES, StackTracesManagerHandler.class);
		handle(URL_DATAROUTER + ROUTERS, DatarouterRoutersAndClientsHandler.class);
		handle(URL_DATAROUTER + "*", DrDefaultHandler.class);

	}

}
