package com.hotpads.job.dispatcher;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.job.web.JobToTriggerHandler;

public class DatarouterJobDispatcher extends BaseDispatcher{

	private static final String URL_DATAROUTER = DatarouterWebDispatcher.URL_DATAROUTER;

	public static final String
			TRIGGERS = "/triggers";


	public DatarouterJobDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handle(URL_DATAROUTER + TRIGGERS).withHandler(JobToTriggerHandler.class);
	}

}
