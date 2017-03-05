package com.hotpads.clustersetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.util.core.DrStringTool;

@Singleton
public class ClusterSettingFinder implements SettingFinder{
	private static final Logger logger = LoggerFactory.getLogger(SettingFinder.class);

	public static final String EMPTY_STRING = "";

	@Inject
	private ClusterSettingFinderConfig clusterSettingFinderConfig;
	@Inject
	private ClusterSettingNodes clusterSettingNodes;


	public List<ClusterSetting> getAllSettingsWithName(String name){
		ClusterSettingKey prefix = new ClusterSettingKey(name, null, null, null, null);
		return clusterSettingNodes.clusterSetting().streamWithPrefix(prefix, null)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> getSettingValue(String name){
		List<ClusterSettingKey> keys = generateKeysForSelection(name);
		List<ClusterSetting> settings = clusterSettingNodes.clusterSetting().getMulti(keys, null);
		return ClusterSetting.getMostSpecificSetting(settings).map(ClusterSetting::getValue);
	}

	private List<ClusterSettingKey> generateKeysForSelection(String name){
		List<ClusterSettingKey> keys = new ArrayList<>();

		//remember to use "" instead of null.  should probably make a new Field type to do that for you
		keys.add(new ClusterSettingKey(name, ClusterSettingScope.defaultScope, ServerType.UNKNOWN, EMPTY_STRING,
				EMPTY_STRING));

		keys.add(new ClusterSettingKey(name, ClusterSettingScope.cluster, ServerType.ALL, EMPTY_STRING, EMPTY_STRING));

		ClusterSettingKey serverTypeSetting = getKeyForServerType(name);
		if(serverTypeSetting != null){
			keys.add(serverTypeSetting);
		}

		ClusterSettingKey serverNameSetting = getKeyForServerName(name);
		if(serverNameSetting != null){
			keys.add(serverNameSetting);
		}

		ClusterSettingKey applicationSetting = getKeyForApplication(name);
		if(applicationSetting != null){
			keys.add(applicationSetting);
		}

		return keys;
	}

	private ClusterSettingKey getKeyForServerType(String name){
		ServerType serverType = clusterSettingFinderConfig.getServerType();
		if(serverType == null || serverType.getPersistentString().equals(ServerType.UNKNOWN)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.serverType, serverType.getPersistentString(),
				EMPTY_STRING, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForServerName(String name){
		String serverName = clusterSettingFinderConfig.getServerName();
		if(DrStringTool.isEmpty(serverName)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.serverName, ServerType.UNKNOWN, serverName,
				EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForApplication(String name){
		String application = clusterSettingFinderConfig.getApplication();
		if(DrStringTool.isEmpty(application)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.application, ServerType.UNKNOWN, EMPTY_STRING,
				application);
	}

}
