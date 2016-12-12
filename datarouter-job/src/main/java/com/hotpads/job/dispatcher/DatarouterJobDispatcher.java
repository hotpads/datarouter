package com.hotpads.job.dispatcher;

import com.hotpads.clustersetting.ClusterSettingsHandler;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.job.record.LongRunningTasksHandler;
import com.hotpads.job.web.TriggerHandler;
import com.hotpads.joblet.handler.JobletHandler;
import com.hotpads.webappinstance.WebAppInstanceHandler;

public class DatarouterJobDispatcher extends BaseDispatcher{

	public static final String URL_DATAROUTER = DatarouterWebDispatcher.PATH_datarouter;

	public static final String
			WEB_APP_INSTANCES = "/webAppInstances",
			SETTINGS = "/settings",
			TRIGGERS = "/triggers",
			LONG_RUNNING_TASKS = "/longRunningTasks",
			JOBLETS = "/joblets",
			JOBLET_SCALING = "/jobletScaling";


	public DatarouterJobDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, URL_DATAROUTER);

		//All urls must start with URL_DATAROUTER
		handle(URL_DATAROUTER + WEB_APP_INSTANCES).withHandler(WebAppInstanceHandler.class);
		handle(URL_DATAROUTER + SETTINGS).withHandler(ClusterSettingsHandler.class);
		handle(URL_DATAROUTER + TRIGGERS).withHandler(TriggerHandler.class);
		handle(URL_DATAROUTER + LONG_RUNNING_TASKS).withHandler(LongRunningTasksHandler.class);
		handleDir(URL_DATAROUTER + JOBLETS).withHandler(JobletHandler.class);
	}

}
