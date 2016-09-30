package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.constant.ConstantBooleanSetting;

@Singleton
public class SettinglessNodeFactory extends BaseNodeFactory{

	@Inject
	private SettinglessNodeFactory(DatarouterClients clients){
		super(clients);
	}

	@Override
	protected Setting<Boolean> getRecordCallsites(){
		return new ConstantBooleanSetting(false);
	}

}
