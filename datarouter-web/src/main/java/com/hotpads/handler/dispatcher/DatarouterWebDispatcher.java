package com.hotpads.handler.dispatcher;


import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.test.TestApiHandler;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.ExecutorsMonitoringHandler;
import com.hotpads.handler.MemoryMonitoringHandler;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.DatarouterHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.client.memory.MemoryHandler;
import com.hotpads.handler.datarouter.DatabeanViewerHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;
import com.hotpads.handler.server.WebAppInstanceHandler;

public class DatarouterWebDispatcher extends BaseDispatcher{

	public static final String
			DATA = "/data";

	public static final String
			PATH_datarouter = "/datarouter",
			PATH_routers = PATH_datarouter + "/routers",
			PATH_stackTraces = PATH_datarouter + "/stackTraces",
			PATH_clients = PATH_datarouter + "/clients",
			PATH_clients_memory = PATH_clients + "/memory",
			PATH_memory = PATH_datarouter + "/memory",
			PATH_databeanGenerator = PATH_datarouter + "/databeanGenerator",
			PATH_nodes_browseData = PATH_datarouter + "/nodes/browseData",
			PATH_executors = PATH_datarouter + "/executors",
			PATH_data = PATH_datarouter + DATA,
			PATH_testApi = PATH_datarouter + "/testApi",
			PATH_webAppInstances = PATH_datarouter + "/webAppInstances";

	public DatarouterWebDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH_datarouter);

		handle(PATH_datarouter).withHandler(DatarouterHandler.class);

		handleDir(PATH_routers).withHandler(RoutersHandler.class);
		handle(PATH_nodes_browseData).withHandler(ViewNodeDataHandler.class);
		handle(PATH_databeanGenerator).withHandler(DatabeanGeneratorHandler.class);
		handle(PATH_clients_memory).withHandler(MemoryHandler.class);

		//example: /testApi or /testApidfadfa  or /testApi/ or /testApi/adfafa
		handle(PATH_testApi + REGEX_ONE_DIRECTORY).withHandler(TestApiHandler.class);

		handle(PATH_stackTraces).withHandler(StackTracesManagerHandler.class);
		handleDir(PATH_memory).withHandler(MemoryMonitoringHandler.class);
		handleDir(PATH_executors).withHandler(ExecutorsMonitoringHandler.class);

		// example: /datarouter/data/fadafa/adfadfafqe/abc or /datarouter/data/fadafa/adfadfafqe/abc.1341 or
		// /datarouter/data/fadafa/adfadfafqe/abbc_2152
		handle(PATH_data + REGEX_TWO_DIRECTORY_PLUS).withHandler(DatabeanViewerHandler.class);

		handle(PATH_webAppInstances).withHandler(WebAppInstanceHandler.class);
	}

}
