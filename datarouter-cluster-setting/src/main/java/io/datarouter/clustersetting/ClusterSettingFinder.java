/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.clustersetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.string.StringTool;

@Singleton
public class ClusterSettingFinder implements SettingFinder{

	public static final String EMPTY_STRING = "";

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private ClusterSettingFinderConfig clusterSettingFinderConfig;
	@Inject
	private DatarouterClusterSettingDao clusterSettingDao;

	private final List<CachedSetting<?>> allCachedSettings = new ArrayList<>();
	private Boolean started = false;

	@Override
	public void registerCachedSetting(CachedSetting<?> setting){
		synchronized(started){
			if(started){
				//after onStartUp, validate every added node immediately
				validateSetting(setting);
			}else{
				//before onStartUp, save the nodes to validate later
				allCachedSettings.add(setting);
			}
		}
	}

	@Override
	public void validateAllCachedSettings(){
		synchronized(started){
			if(started){
				return;
			}
			started = true;//no more writes to allCachedSettingNodes after this, so no ConccurentModificationException
			allCachedSettings.forEach(ClusterSettingFinder::validateSetting);
			allCachedSettings.clear();//no need to keep validated ones in memory anymore
		}
	}

	private static void validateSetting(CachedSetting<?> setting){
		setting.validateAllCustomValuesCanBeParsed();
	}

	@Override
	public String getEnvironmentType(){
		return datarouterProperties.getEnvironmentType();
	}

	@Override
	public String getEnvironmentName(){
		return datarouterProperties.getEnvironment();
	}

	@Override
	public ServerType getServerType(){
		return datarouterProperties.getServerType();
	}

	@Override
	public String getServerName(){
		return datarouterProperties.getServerName();
	}

	@Override
	public Optional<String> getSettingValue(String name){
		List<ClusterSettingKey> keys = generateKeysForSelection(name);
		List<ClusterSetting> settings = clusterSettingDao.getMulti(keys);
		return ClusterSetting.getMostSpecificSetting(settings).map(ClusterSetting::getValue);
	}

	@Override
	public List<String> getAllCustomSettingValues(String name){
		return IterableTool.map(getAllSettingsWithName(name), ClusterSetting::getValue);
	}

	public List<ClusterSetting> getAllSettingsWithName(String name){
		var prefix = new ClusterSettingKey(name, null, null, null, null);
		return clusterSettingDao.scanWithPrefix(prefix).list();
	}

	private List<ClusterSettingKey> generateKeysForSelection(String name){
		List<ClusterSettingKey> keys = new ArrayList<>();

		//remember to use "" instead of null.  should probably make a new Field type to do that for you
		keys.add(new ClusterSettingKey(name, ClusterSettingScope.DEFAULT_SCOPE, ServerType.UNKNOWN
				.getPersistentString(), EMPTY_STRING, EMPTY_STRING));

		keys.add(new ClusterSettingKey(name, ClusterSettingScope.CLUSTER, ServerType.ALL.getPersistentString(),
				EMPTY_STRING, EMPTY_STRING));

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
		if(serverType == null || serverType.getPersistentString().equals(ServerType.UNKNOWN
				.getPersistentString())){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.SERVER_TYPE, serverType.getPersistentString(),
				EMPTY_STRING, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForServerName(String name){
		String serverName = clusterSettingFinderConfig.getServerName();
		if(StringTool.isEmpty(serverName)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.SERVER_NAME, ServerType.UNKNOWN
				.getPersistentString(), serverName, EMPTY_STRING);
	}

	private ClusterSettingKey getKeyForApplication(String name){
		String application = clusterSettingFinderConfig.getApplication();
		if(StringTool.isEmpty(application)){
			return null;
		}
		return new ClusterSettingKey(name, ClusterSettingScope.APPLICATION, ServerType.UNKNOWN
				.getPersistentString(), EMPTY_STRING, application);
	}

}
