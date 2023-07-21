/*
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
package io.datarouter.clustersetting.enums;

import java.util.Comparator;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.enums.StringMappedEnum;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;

public enum ClusterSettingScope{
	DEFAULT_SCOPE("defaultScope", "Default", 1000),
	SERVER_TYPE("serverType", "Server Type", 100),
	SERVER_NAME("serverName", "Server Name", 10);

	public static final StringMappedEnum<ClusterSettingScope> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString, 20);

	public final String persistentString;
	public final String display;
	public final int specificity;

	ClusterSettingScope(String persistentString, String display, int specificity){
		this.persistentString = persistentString;
		this.display = display;
		this.specificity = specificity;
	}

	public static ClusterSettingScope fromParams(ServerType serverType, String serverName){
		return fromParams(serverType.getPersistentString(), serverName);
	}

	public static ClusterSettingScope fromParams(String serverTypePersistentString, String serverName){
		if(StringTool.notEmpty(serverName)){
			return ClusterSettingScope.SERVER_NAME;
		}
		if(serverTypePersistentString != null
				&& ObjectTool.notEquals(ServerType.UNKNOWN.getPersistentString(), serverTypePersistentString)
				&& ObjectTool.notEquals(ServerType.ALL.getPersistentString(), serverTypePersistentString)){
			return ClusterSettingScope.SERVER_TYPE;
		}
		return ClusterSettingScope.DEFAULT_SCOPE;
	}


	public static class ClusterSettingScopeComparator implements Comparator<ClusterSetting>{

		@Override
		public int compare(ClusterSetting first, ClusterSetting second){
			if(ObjectTool.bothNull(first, second)){
				return 0;
			}
			if(ObjectTool.isOneNullButNotTheOther(first, second)){
				return first == null ? -1 : 1;
			}

			ClusterSettingScope firstScope = first.getScope();
			ClusterSettingScope secondScope = second.getScope();
			if(ObjectTool.bothNull(firstScope, secondScope)){
				return 0;
			}
			if(ObjectTool.isOneNullButNotTheOther(firstScope, secondScope)){
				return firstScope == null ? -1 : 1;
			}

			int difference = firstScope.specificity - secondScope.specificity;
			if(difference == 0){
				return 0;
			}
			return difference < 0 ? -1 : 1;
		}

	}

}
