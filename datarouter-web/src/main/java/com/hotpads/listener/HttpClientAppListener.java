package com.hotpads.listener;

import javax.inject.Inject;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.util.http.client.HotPadsHttpClient;

public class HttpClientAppListener extends DatarouterAppListener{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private NotificationApiClient notificationApiClient;
	
	@Override
	protected void onStartUp(){
	}

	@Override
	protected void onShutDown(){
		for(HotPadsHttpClient httpClient : injector.getInstancesOfType(HotPadsHttpClient.class)){
			httpClient.shutdown();
		}
		
		notificationApiClient.shutdown();
	}

}
