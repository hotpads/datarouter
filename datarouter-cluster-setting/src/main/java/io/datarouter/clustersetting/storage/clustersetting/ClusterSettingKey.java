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
package io.datarouter.clustersetting.storage.clustersetting;

import java.util.List;
import java.util.Objects;

import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class ClusterSettingKey extends BaseRegularPrimaryKey<ClusterSettingKey>{

	private String name;
	private ClusterSettingScope scope;
	private String serverType;
	private String serverName;

	public static class FieldKeys{
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringEnumFieldKey<ClusterSettingScope> scope = new StringEnumFieldKey<>("scope",
				ClusterSettingScope.class)
				.withSize(20);
		public static final StringFieldKey serverType = new StringFieldKey("serverType")
				.withSize(CommonFieldSizes.LENGTH_50);
		public static final StringFieldKey serverName = new StringFieldKey("serverName")
				.withSize(CommonFieldSizes.LENGTH_50);
	}


	ClusterSettingKey(){
	}

	public ClusterSettingKey(String name, ClusterSettingScope scope, String serverType, String serverName){
		this.name = name;
		this.scope = scope;
		this.serverType = serverType;
		this.serverName = serverName;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.name, name),
				new StringEnumField<>(FieldKeys.scope, scope),
				new StringField(FieldKeys.serverType, serverType),
				new StringField(FieldKeys.serverName, serverName));
	}

	public boolean appliesToWebappInstance(WebappInstance app){
		if(ClusterSettingScope.DEFAULT_SCOPE == scope){
			return true;
		}else if(ClusterSettingScope.SERVER_TYPE == scope){
			return Objects.equals(serverType, app.getServerType());
		}else if(ClusterSettingScope.SERVER_NAME == scope){
			return Objects.equals(serverName, app.getKey().getServerName());
		}
		throw new RuntimeException("unknown key.scope");
	}

	public ClusterSettingScope getScope(){
		return scope;
	}

	public void setScope(ClusterSettingScope scope){
		this.scope = scope;
	}

	public String getServerType(){
		return serverType;
	}

	public void setServerType(String serverType){
		this.serverType = serverType;
	}

	public String getServerName(){
		return serverName;
	}

	public void setServerName(String serverName){
		this.serverName = serverName;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

}
