package com.hotpads.example.config;

import java.util.List;

import com.hotpads.datarouter.inject.guice.GuiceInjectorRetriever;
import com.hotpads.listener.BaseDatarouterServletContextListener;
import com.hotpads.listener.DatarouterAppListener;

public class ExampleServletContextListener
extends BaseDatarouterServletContextListener
implements GuiceInjectorRetriever{

	@Override
	protected List<Class<? extends DatarouterAppListener>> getAppListeners(){
		List<Class<? extends DatarouterAppListener>> appListeners = super.getAppListeners();
		appListeners.add(ExampleAppListener.class);
		return appListeners;
	}

}
