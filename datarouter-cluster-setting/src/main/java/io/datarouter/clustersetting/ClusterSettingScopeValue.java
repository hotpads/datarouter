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

import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.storage.servertype.ServerType;

public class ClusterSettingScopeValue implements Comparable<ClusterSettingScopeValue>{

	private final ClusterSettingScope scope;
	private final String value;
	private final String displayString;

	private static final String EMPTY_STRING = "";

	public ClusterSettingScopeValue(ClusterSettingKey setting){
		this.scope = setting.getScope();
		switch(this.scope){
		case DEFAULT_SCOPE:
			this.value = "";
			this.displayString = "Default Scope";
			break;
		case SERVER_TYPE:
			this.value = setting.getServerType();
			this.displayString = "Server Type: " + setting.getServerType();
			break;
		case SERVER_NAME:
			this.value = setting.getServerName();
			this.displayString = "Server Name: " + setting.getServerName();
			break;
		default:
			throw new RuntimeException("Failed to construct ClusterSettingScopeValue from ClusterSettingKey: "
					+ setting.toString());
		}
	}

	@Override
	public int compareTo(ClusterSettingScopeValue other){
		int scopeCompare = scope.compareTo(other.scope);
		return scopeCompare != 0 ? scopeCompare : value.compareTo(other.value);
	}

	public String getPersistentString(){
		return scope.getPersistentString() + '_' + value;
	}

	public static ClusterSettingScopeValue parse(String persistentString){
		if(persistentString == null){
			return null;
		}
		String[] parts = persistentString.split("_", 2);
		if(parts.length == 0){
			return null;
		}
		ClusterSettingScope scope = ClusterSettingScope.fromPersistentStringStatic(parts[0]);
		switch(scope){
		case DEFAULT_SCOPE:
		case SERVER_TYPE:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, parts[1], null));
		case SERVER_NAME:
			return new ClusterSettingScopeValue(new ClusterSettingKey(null, scope, null, parts[1]));
		default:
			throw new RuntimeException("Failed to parse ClusterSettingScopeValue from string: " + persistentString);
		}
	}

	public ClusterSettingKey toClusterSettingKey(String settingName){
		switch(scope){
		case DEFAULT_SCOPE:
			return new ClusterSettingKey(settingName, scope, ServerType.UNKNOWN.getPersistentString(), EMPTY_STRING);
		case SERVER_TYPE:
			return new ClusterSettingKey(settingName, scope, value, EMPTY_STRING);
		case SERVER_NAME:
			return new ClusterSettingKey(settingName, scope, ServerType.UNKNOWN.getPersistentString(), value);
		default:
			return null;
		}
	}

	public String getDisplayString(){
		return displayString;
	}

}
