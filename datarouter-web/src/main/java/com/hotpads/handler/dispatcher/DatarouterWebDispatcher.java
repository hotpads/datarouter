package com.hotpads.handler.dispatcher;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.test.TestApiHandler;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.ExecutorsMonitoringHandler;
import com.hotpads.handler.MemoryMonitoringHandler;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.client.memory.MemoryHandler;
import com.hotpads.handler.datarouter.DataBeanViewerHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;

public class DatarouterWebDispatcher extends BaseDispatcher{

	public static final String ANYTHING = ".*";

	public static final String URL_DATAROUTER = "/datarouter";

	public static final String
			ROUTERS = "/routers",
			STACKTRACES = "/stackTraces",
			CLIENTS = "/clients",
			MEMORY_STATS = "/memory",
			DATABEAN_GENERATOR = "/databeanGenerator",
			NODE_BROWSE_DATA = "/nodes/browseData",
			EXECUTORS_MONITORING = "/executors",
			MEMORY = "/memory";


	public DatarouterWebDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handle(URL_DATAROUTER + "*").withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + ROUTERS).withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + NODE_BROWSE_DATA).withHandler(ViewNodeDataHandler.class);
		handle(URL_DATAROUTER + DATABEAN_GENERATOR).withHandler(DatabeanGeneratorHandler.class);
		handle(URL_DATAROUTER + CLIENTS + MEMORY).withHandler(MemoryHandler.class);
		handle(URL_DATAROUTER + "/testApi[/]?[^/]*").withHandler(TestApiHandler.class);
		handle(URL_DATAROUTER + STACKTRACES).withHandler(StackTracesManagerHandler.class);
		handleDir(URL_DATAROUTER + MEMORY_STATS).withHandler(MemoryMonitoringHandler.class);
		handleDir(URL_DATAROUTER + EXECUTORS_MONITORING).withHandler(ExecutorsMonitoringHandler.class);
		handle(URL_DATAROUTER + "/\\w+/.+/.+").withHandler(DataBeanViewerHandler.class);
	}

}
