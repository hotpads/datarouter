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
import java.util.function.Supplier;

import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.setting.Setting;

public class ClusterSetting extends BaseDatabean<ClusterSettingKey,ClusterSetting>{

	private String value;

	public static class FieldKeys{
		public static final StringFieldKey value = new StringFieldKey("value")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
	}

	public static class ClusterSettingFielder extends BaseDatabeanFielder<ClusterSettingKey,ClusterSetting>{

		public ClusterSettingFielder(){
			super(ClusterSettingKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ClusterSetting databean){
			return List.of(new StringField(FieldKeys.value, databean.value));
		}
	}

	public ClusterSetting(){
		super(new ClusterSettingKey(null, null, null, null));
	}

	public ClusterSetting(ClusterSettingKey key, String value){
		super(key);
		this.value = value;
	}

	public ClusterSetting(
			String name,
			ClusterSettingScope scope,
			String serverType,
			String serverName,
			String value){
		super(new ClusterSettingKey(name, scope, serverType, serverName));
		this.value = value;
	}

	@Override
	public Supplier<ClusterSettingKey> getKeySupplier(){
		return ClusterSettingKey::new;
	}

	@Override
	public String toString(){
		return getKey().toString() + ":" + value;
	}

	public <T> T getTypedValue(Setting<T> parser){
		return parser.parseStringValue(value);
	}

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

	public ClusterSettingScope getScope(){
		return getKey().getScope();
	}

	public void setScope(ClusterSettingScope scope){
		getKey().setScope(scope);
	}

	public String getServerType(){
		return getKey().getServerType();
	}

	public void setServerType(String serverType){
		getKey().setServerType(serverType);
	}

	public String getServerName(){
		return getKey().getServerName();
	}

	public void setServerName(String serverName){
		getKey().setServerName(serverName);
	}

	public String getName(){
		return getKey().getName();
	}

	public void setName(String name){
		getKey().setName(name);
	}

}
