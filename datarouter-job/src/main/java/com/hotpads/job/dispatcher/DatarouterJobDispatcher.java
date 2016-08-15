package com.hotpads.job.dispatcher;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.job.record.LongRunningTasksHandler;
import com.hotpads.job.web.JobHandler;
import com.hotpads.joblet.handler.JobletHandler;
import com.hotpads.joblet.handler.JobletScalingHandler;

public class DatarouterJobDispatcher extends BaseDispatcher{

	public static final String URL_DATAROUTER = DatarouterWebDispatcher.PATH_datarouter;

	public static final String
			TRIGGERS = "/triggers",
			LONG_RUNNING_TASKS = "/longRunningTasks",
			JOBLETS = "/joblets",
			JOBLET_SCALING = "/jobletScaling";


	public DatarouterJobDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, URL_DATAROUTER);

		//All urls must start with URL_DATAROUTER
		handle(URL_DATAROUTER + TRIGGERS).withHandler(JobHandler.class);
		handle(URL_DATAROUTER + LONG_RUNNING_TASKS).withHandler(LongRunningTasksHandler.class);
		handleDir(URL_DATAROUTER + JOBLETS).withHandler(JobletHandler.class);
		handle(URL_DATAROUTER + JOBLET_SCALING).withHandler(JobletScalingHandler.class);
	}

}
