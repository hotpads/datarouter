package com.hotpads.webappinstance;

import javax.inject.Inject;

import com.hotpads.listener.DatarouterAppListener;

public class WebAppInstanceAppListener extends DatarouterAppListener{

	@Inject
	private WebAppInstanceNodes webAppInstanceNodes;
	@Inject
	private WebAppInstanceDao webAppInstanceDao;

	@Override
	protected void onStartUp(){
		webAppInstanceDao.updateWebAppInstanceTable();
	}

	@Override
	protected void onShutDown(){
		webAppInstanceNodes.getWebAppInstance().delete(webAppInstanceDao.getWebAppInstanceKey(), null);
	}

}
