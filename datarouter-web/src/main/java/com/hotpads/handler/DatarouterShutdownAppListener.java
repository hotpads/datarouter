package com.hotpads.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.listener.DatarouterAppListener;

@Singleton
public class DatarouterShutdownAppListener extends DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterShutdownAppListener.class);

	@Inject
	private Datarouter datarouter;

	@Override
	public void onStartUp(){
	}

	@Override
	public void onShutDown(){
		logger.info("datarouter.shutdown()");
		datarouter.shutdown();
	}
	

}
