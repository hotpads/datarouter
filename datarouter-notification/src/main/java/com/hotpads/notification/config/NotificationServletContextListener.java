package com.hotpads.notification.config;

import java.util.List;

import com.hotpads.datarouter.inject.guice.GuiceInjectorRetriever;
import com.hotpads.listener.BaseDatarouterServletContextListener;
import com.hotpads.listener.DatarouterAppListener;
import com.hotpads.notification.NotificationAppListener;

public class NotificationServletContextListener
extends BaseDatarouterServletContextListener
implements GuiceInjectorRetriever{

	@Override
	protected List<Class<? extends DatarouterAppListener>> getAppListeners(){
		List<Class<? extends DatarouterAppListener>> appListeners = super.getAppListeners();
		appListeners.add(NotificationAppListener.class);
		return appListeners;
	}

}
