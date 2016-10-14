package com.hotpads.datarouter.storage.failover;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class FailoverSettings extends SettingNode{

	private final Map<String,Setting<Boolean>> isFailedOverByNodeName;
	public final Setting<Boolean> runRecoveryJob;

	@Inject
	public FailoverSettings(SettingFinder finder){
		super(finder, "datarouter.failover.", "datarouter.");
		this.isFailedOverByNodeName = new HashMap<>();
		this.runRecoveryJob = registerBoolean("runRecoveryJob", false);
	}

	public Setting<Boolean> isFailedOver(String nodeName){
		return isFailedOverByNodeName.computeIfAbsent(nodeName, name -> registerBoolean(nodeName, false));
	}

}
