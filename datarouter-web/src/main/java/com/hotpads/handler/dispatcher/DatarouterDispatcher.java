package com.hotpads.handler.dispatcher;

import com.hotpads.datarouter.DataMigrationHandler;
import com.hotpads.datarouter.batch.web.BatchSizeOptimizerMonitoringHandler;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.test.TestApiHandler;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.ExecutorsMonitoringHandler;
import com.hotpads.handler.MemoryMonitoringHandler;
import com.hotpads.handler.admin.CallsiteHandler;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.admin.StackTracesManagerHandler;
import com.hotpads.handler.admin.client.memory.MemoryHandler;
import com.hotpads.handler.datarouter.ViewNodeDataHandler;
import com.hotpads.handler.logging.LoggingSettingsHandler;
import com.hotpads.handler.setting.ClusterSettingsHandler;
import com.hotpads.job.web.JobToTriggerHandler;
import com.hotpads.notification.NotificationTesterHandler;
import com.hotpads.notification.alias.NotificationAliasHandler;
import com.hotpads.profile.loadtest.LoadTestHandler;
import com.hotpads.shutdown.ShutdownHandler;
import com.hotpads.trace.TraceHandler;

public class DatarouterDispatcher extends BaseDispatcher{

	public static final String ANYTHING = ".*";

	public static final String URL_DATAROUTER = "/datarouter";

	public static final String
			ROUTERS = "/routers",
			STACKTRACES = "/stackTraces",
			CLIENTS = "/clients",
			SETTING = "/settings",
			LOGGING = "/logging",
			MEMORY_STATS = "/memory",
			NOTIFICATION_ALIAS = "/notification/alias",
			NOTIFICATION_TESTER = "/notification/tester",
			DATABEAN_GENERATOR = "/databeanGenerator",
			CALLSITE = "/callsite",
			NODE_BROWSE_DATA = "/nodes/browseData",
			URL_DATAROUTER_API = "/datarouterApi",
			EXECUTORS_MONITORING = "/executors",
			BATCH_SIZE_OPTIMIZER = "/batchSizeOptimizer",
			TRACES = "/traces",
			LOAD_TEST = "/loadTest",
			TRIGGERS = "/triggers",
			SHUTDOWN = "/shutdown",
			DATA_MIGRATION = "/dataMigration"
			;

	private static final String
			MEMORY = "/memory"
			;

	public DatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(URL_DATAROUTER + "*").withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + ROUTERS).withHandler(RoutersHandler.class);
		handle(URL_DATAROUTER + STACKTRACES).withHandler(StackTracesManagerHandler.class);
		handle(URL_DATAROUTER + SETTING).withHandler(ClusterSettingsHandler.class);
		handleDir(URL_DATAROUTER + LOGGING).withHandler(LoggingSettingsHandler.class);
		handleDir(URL_DATAROUTER + MEMORY_STATS).withHandler(MemoryMonitoringHandler.class);
		handleDir(URL_DATAROUTER + NOTIFICATION_ALIAS).withHandler(NotificationAliasHandler.class);
		handleDir(URL_DATAROUTER + NOTIFICATION_TESTER).withHandler(NotificationTesterHandler.class);
		handle(URL_DATAROUTER + NODE_BROWSE_DATA).withHandler(ViewNodeDataHandler.class);
		handle(URL_DATAROUTER + DATABEAN_GENERATOR).withHandler(DatabeanGeneratorHandler.class);
		handle(URL_DATAROUTER + CALLSITE).withHandler(CallsiteHandler.class);
		handleDir(URL_DATAROUTER + EXECUTORS_MONITORING).withHandler(ExecutorsMonitoringHandler.class);
		handleDir(URL_DATAROUTER + BATCH_SIZE_OPTIMIZER).withHandler(BatchSizeOptimizerMonitoringHandler.class);
		handleDir(URL_DATAROUTER + TRACES).withHandler(TraceHandler.class);
		handleDir(URL_DATAROUTER + LOAD_TEST).withHandler(LoadTestHandler.class);
		handle(URL_DATAROUTER + SHUTDOWN).withHandler(ShutdownHandler.class);
		handle(URL_DATAROUTER + DATA_MIGRATION).withHandler(DataMigrationHandler.class);
		handle(URL_DATAROUTER + TRIGGERS).withHandler(JobToTriggerHandler.class);
		handle(URL_DATAROUTER + CLIENTS + MEMORY).withHandler(MemoryHandler.class);
		handle(URL_DATAROUTER + "/testApi[/]?[^/]*").withHandler(TestApiHandler.class);
	}

}
