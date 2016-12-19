package com.hotpads.clustersetting;

import com.hotpads.datarouter.setting.ServerType;

public interface ClusterSettingFinderConfig{

	ServerType getServerType();

	String getServerName();

	String getApplication();

}
