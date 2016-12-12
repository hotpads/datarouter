package com.hotpads.clustersetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.server.databean.WebAppInstance;
import com.hotpads.webappinstance.WebAppInstanceDao;

@Singleton
public class ClusterSettingService{

	@Inject
	private WebAppInstanceDao webAppInstanceDao;
	@Inject
	private ClusterSettingFinder clusterSettingFinder;

	public <T> T getSettingValueForWebAppInstance(Setting<T> parserAndDefault, WebAppInstance webAppInstance){
		List<ClusterSetting> settingsWithName = clusterSettingFinder.getAllSettingsWithName(parserAndDefault.getName());
		List<ClusterSetting> settingsForWebAppInstance = ClusterSetting.filterForWebAppInstance(settingsWithName,
				webAppInstance);
		Optional<ClusterSetting> mostSpecificSetting = ClusterSetting.getMostSpecificSetting(settingsForWebAppInstance);
		return ClusterSetting.getTypedValueOrUseDefaultFrom(mostSpecificSetting, parserAndDefault);
	}

	public <T> Map<WebAppInstance,T> getSettingValueByWebAppInstance(Setting<T> parserAndDefault){
		Map<WebAppInstance,T> result = new HashMap<>();
		List<WebAppInstance> allWebAppInstances = webAppInstanceDao.getAll();
		List<ClusterSetting> settingsWithName = clusterSettingFinder.getAllSettingsWithName(parserAndDefault.getName());
		for(WebAppInstance webAppInstance : allWebAppInstances){
			Optional<ClusterSetting> dbSettingForInstance = ClusterSetting.getMostSpecificSettingForWebAppInstance(
					settingsWithName, webAppInstance);
			T valueForInstance = ClusterSetting.getTypedValueOrUseDefaultFrom(dbSettingForInstance, parserAndDefault);
			result.put(webAppInstance, valueForInstance);
		}
		return result;
	}

}
