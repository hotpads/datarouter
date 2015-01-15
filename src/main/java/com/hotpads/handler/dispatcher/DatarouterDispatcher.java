package com.hotpads.handler.dispatcher;

import com.google.inject.Injector;
import com.hotpads.datarouter.test.TestApiHandler;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.MemoryMonitoringHandler;
import com.hotpads.handler.admin.CallsiteHandler;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.client.hbase.HBaseHandler;
import com.hotpads.handler.admin.client.hibernate.HibernateHandler;
import com.hotpads.handler.admin.client.memchached.MemcachedHandler;
import com.hotpads.handler.admin.client.memory.MemoryHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;
import com.hotpads.handler.logging.LoggingSettingsHandler;
import com.hotpads.handler.setting.ClusterSettingsHandler;
import com.hotpads.notification.alias.NotificationAliasHandler;

public class DatarouterDispatcher extends BaseDispatcher{

	public static final String URL_DATAROUTER = "/datarouter";

	public static final String
			ROUTERS = "/routers",
			STACKTRACES = "/stackTraces",
			CLIENTS = "/clients",
			SETTING = "/settings",
			LOGGING = "/logging",
			MEMORY_STATS = "/memory",
			NOTIFICATION_ALIAS = "/notification/alias",
			DATABEAN_GENERATOR = "/databeanGenerator",
			CALLSITE = "/callsite",
			NODE_BROWSE_DATA = "/nodes/browseData",
			URL_DATAROUTER_API = "/datarouterApi";

	private static final String
			URL_HTTP_CLIENT = URL_DATAROUTER_API + "/httpNode";

	private static final String
			HBASE = "/hbase",
			HIBERNATE = "/hibernate",
			MEMORY = "/memory",
			MEMCACHED = "/memcached";

	public DatarouterDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(URL_DATAROUTER + "*").withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + ROUTERS).withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + STACKTRACES).withHandler(StackTracesManagerHandler.class);
		handle(URL_DATAROUTER + SETTING).withHandler(ClusterSettingsHandler.class);
		handle(URL_DATAROUTER + LOGGING + ".*").withHandler(LoggingSettingsHandler.class);
		handle(URL_DATAROUTER + MEMORY_STATS + ".*").withHandler(MemoryMonitoringHandler.class);
		handle(URL_DATAROUTER + NOTIFICATION_ALIAS + ".*").withHandler(NotificationAliasHandler.class);
//		handle(URL_HTTP_CLIENT, DataRouterHttpClientHandler.class);
		handle(URL_DATAROUTER + NODE_BROWSE_DATA).withHandler(ViewNodeDataHandler.class);
		handle(URL_DATAROUTER + DATABEAN_GENERATOR).withHandler(DatabeanGeneratorHandler.class);
		handle(URL_DATAROUTER + CALLSITE).withHandler(CallsiteHandler.class);

		handle(URL_DATAROUTER + CLIENTS + HBASE).withHandler(HBaseHandler.class);
		handle(URL_DATAROUTER + CLIENTS + HIBERNATE).withHandler(HibernateHandler.class);
		handle(URL_DATAROUTER + CLIENTS + MEMORY).withHandler(MemoryHandler.class);
		handle(URL_DATAROUTER + CLIENTS + MEMCACHED).withHandler(MemcachedHandler.class);

		handle(URL_DATAROUTER + "/testApi[/]?[^/]*").withHandler(TestApiHandler.class);
	}

}
