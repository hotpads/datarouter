package com.hotpads.listener;

import javax.inject.Inject;

import com.hotpads.datarouter.config.Config;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.session.DatarouterUserService;

public class DatarouterUserConfigAppListener extends DatarouterAppListener{

	@Inject
	private DatarouterUserNodes userNodes;
	@Inject
	private DatarouterUserService datarouterUserService;

	@Override
	protected void onStartUp(){
		if(userNodes.getUserNode().stream(null, new Config().setLimit(1)).findAny().isPresent()){
			return;
		}
		datarouterUserService.createAdminUser();
	}

	@Override
	protected void onShutDown(){

	}

}
