package com.hotpads.handler.dispatcher;

import com.google.inject.Injector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.client.hbase.HBaseHandler;
import com.hotpads.handler.admin.client.hibernate.HibernateHandler;
import com.hotpads.handler.admin.client.memchached.MemcachedHandler;
import com.hotpads.handler.admin.client.memory.MemoryHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;

public class DataRouterDispatcher extends BaseDispatcher{

	private static final String ROUTERS = "/routers";
	public static final String URL_STACKTRACES = "/stackTraces";
	public static final String URL_DATAROUTER = "/datarouter";
	public static final String URL_DATAROUTER_API = "/datarouterApi";
	public static final String URL_DATAROUTER_VIEW_NODE_DATA = URL_DATAROUTER + "/viewNodeData";
	public static final String URL_HTTP_CLIENT = URL_DATAROUTER_API + "/httpNode";

	private static final String HBASE = "/hbase";
	private static final String HIBERNATE = "/hibernate";
	private static final String MEMORY = "/memory";
	private static final String MEMCACHED = "/memcached";
	
	public static final String URL_DATABEAN_CLASS_GENERATOR = URL_DATAROUTER + "/databeanGenerator";


	public DataRouterDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		// DataRouter
//		handle(URL_HTTP_CLIENT, DataRouterHttpClientHandler.class);
		handle(URL_DATAROUTER_VIEW_NODE_DATA, ViewNodeDataHandler.class);
		handle(URL_DATAROUTER + URL_STACKTRACES, StackTracesManagerHandler.class);
		handle(URL_DATABEAN_CLASS_GENERATOR, DatabeanGeneratorHandler.class);
		
		handle(URL_DATAROUTER + ROUTERS + HBASE, HBaseHandler.class);
		handle(URL_DATAROUTER + ROUTERS + HIBERNATE, HibernateHandler.class);
		handle(URL_DATAROUTER + ROUTERS + MEMORY, MemoryHandler.class);
		handle(URL_DATAROUTER + ROUTERS + MEMCACHED, MemcachedHandler.class);
		handle(URL_DATAROUTER + ROUTERS, RoutersHandler.class);
		handle(URL_DATAROUTER + "*", RoutersHandler.class);
	}

}
