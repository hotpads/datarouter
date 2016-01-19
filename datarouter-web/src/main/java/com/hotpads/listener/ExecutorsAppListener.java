package com.hotpads.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.concurrent.FutureTool;

public class ExecutorsAppListener extends DatarouterAppListener{

	@Inject
	private DatarouterInjector injector;
	
	@Override
	protected void onStartUp(){
	}

	@Override
	protected void onShutDown(){
		for(ExecutorService executor : injector.getInstancesOfType(ExecutorService.class)){
			FutureTool.finishAndShutdown(executor, 5L, TimeUnit.SECONDS);
		}
	}

}
