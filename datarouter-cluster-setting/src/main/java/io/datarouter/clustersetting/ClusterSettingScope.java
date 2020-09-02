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

import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;

public enum ClusterSettingScope implements StringEnum<ClusterSettingScope>{
	DEFAULT_SCOPE("defaultScope", 1000),
	SERVER_TYPE("serverType", 100),
	SERVER_NAME("serverName", 10);

	private final String persistentString;
	private final int specificity;

	ClusterSettingScope(String persistentString, int specificity){
		this.persistentString = persistentString;
		this.specificity = specificity;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	public static ClusterSettingScope fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public ClusterSettingScope fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static ClusterSettingScope fromParams(ServerType serverType, String serverName){
		return fromParams(serverType.getPersistentString(), serverName);
	}

	public static ClusterSettingScope fromParams(String serverTypePersistentString, String serverName){
		if(StringTool.notEmpty(serverName)){
			return ClusterSettingScope.SERVER_NAME;
		}else if(ObjectTool.notEquals(ServerType.UNKNOWN.getPersistentString(), serverTypePersistentString)
				&& ObjectTool.notEquals(ServerType.ALL.getPersistentString(), serverTypePersistentString)){
			return ClusterSettingScope.SERVER_TYPE;
		}
		return ClusterSettingScope.DEFAULT_SCOPE;
	}

	public int getSpecificity(){
		return specificity;
	}

}
