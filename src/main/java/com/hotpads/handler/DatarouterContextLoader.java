package com.hotpads.handler;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.hotpads.HotPadsWebAppListener;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class DatarouterContextLoader extends HotPadsWebAppListener{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterContextLoader.class);

	@Inject
	private DataRouterContext dataRouterContext;

	@Override
	public void onStartUp(){
	}

	@Override
	public void onShutDown(){
		logger.warn("dataRouterContext.shutdown()");
		dataRouterContext.shutdown();
	}

}
