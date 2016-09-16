package com.hotpads.notification.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.notification.NotificationTesterHandler;
import com.hotpads.notification.alias.NotificationAliasHandler;

//TODO I commented out everything that wasn't a notification-related error in DatarouterCoreDispatcher, which I copied this from
public class NotificationDispatcher extends BaseDispatcher{

	public static final String URL_DATAROUTER = DatarouterWebDispatcher.PATH_datarouter;

	public static final String
			//SETTING = "/settings",
			//LOGGING = "/logging",
			NOTIFICATION_ALIAS = "/notification/alias",
			NOTIFICATION_TESTER = "/notification/tester";
			//BATCH_SIZE_OPTIMIZER = "/batchSizeOptimizer",
			//TRACES = "/traces",
			//LOAD_TEST = "/loadTest",
			//CALLSITE = "/callsite",
			//SHUTDOWN = "/shutdown",
			//DATA_MIGRATION = "/dataMigration",
			//ROWCOUNT_CHART = "/rowCountChart",
			//TABLE_ROW_COUNT = "/tableRowCount",
			//THRESHOLD_SETTINGS = "/threshold",
			//LATENCY_MONITORING = "/latency";

	//public static final String URL_SHUTDOWN = URL_DATAROUTER + SHUTDOWN;


	public NotificationDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, URL_DATAROUTER);
		//handleDir("/system").withHandler(SystemApiHandler.class);
		//handle("|/").withHandler(DatarouterNotificationDefaultHandler.class);

		//All urls must start with URL_DATAROUTER
		//handle(URL_DATAROUTER + SETTING).withHandler(ClusterSettingsHandler.class);
		//handleDir(URL_DATAROUTER + LOGGING).withHandler(LoggingSettingsHandler.class);
		handleDir(URL_DATAROUTER + NOTIFICATION_ALIAS).withHandler(NotificationAliasHandler.class);
		handleDir(URL_DATAROUTER + NOTIFICATION_TESTER).withHandler(NotificationTesterHandler.class);
		//handleDir(URL_DATAROUTER + BATCH_SIZE_OPTIMIZER).withHandler(BatchSizeOptimizerMonitoringHandler.class);
		//handleDir(URL_DATAROUTER + TRACES).withHandler(TraceHandler.class);
		//handleDir(URL_DATAROUTER + LOAD_TEST).withHandler(LoadTestHandler.class);
		//handle(URL_DATAROUTER + CALLSITE).withHandler(CallsiteHandler.class);
		//handle(URL_DATAROUTER + SHUTDOWN).withHandler(ShutdownHandler.class);
		//handle(URL_DATAROUTER + DATA_MIGRATION).withHandler(DataMigrationHandler.class);
		//handle(URL_DATAROUTER + ROWCOUNT_CHART).withHandler(ViewRowCountChartHandler.class);
		//handle(URL_DATAROUTER + TABLE_ROW_COUNT + THRESHOLD_SETTINGS).withHandler(TableSizeAlertThresholdHandler.class);
		//handleDir(URL_DATAROUTER + LATENCY_MONITORING).withHandler(ServiceMonitoringHandler.class);
	}

}
