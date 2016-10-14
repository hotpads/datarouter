package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.setting.Setting;

@Singleton
public class NodeFactory extends BaseNodeFactory{

	private final DatarouterSettings datarouterSettings;

	@Inject
	private NodeFactory(DatarouterClients clients, DatarouterSettings datarouterSettings){
		super(clients);
		this.datarouterSettings = datarouterSettings;
	}

	/***************** private **************************/

	@Override
	protected Setting<Boolean> getRecordCallsites(){
		return datarouterSettings.getRecordCallsites();
	}
}
