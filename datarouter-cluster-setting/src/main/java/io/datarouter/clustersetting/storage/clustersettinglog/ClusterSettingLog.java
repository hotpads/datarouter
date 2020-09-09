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
package io.datarouter.clustersetting.storage.clustersettinglog;

import java.time.Instant;
import java.util.List;

import io.datarouter.clustersetting.ClusterSettingLogAction;
import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.custom.LongDateFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;

public class ClusterSettingLog extends BaseDatabean<ClusterSettingLogKey,ClusterSettingLog>{

	private ClusterSettingScope scope;
	private String serverType;
	private String serverName;
	private String value;
	private ClusterSettingLogAction action;
	private String changedBy;
	private String comment;

	public static class FieldKeys{
		public static final LongDateFieldKey timestamp = new LongDateFieldKey("timestamp");
		public static final StringFieldKey changedBy = new StringFieldKey("changedBy")
				.withSize(CommonFieldSizes.LENGTH_50);
		public static final StringEnumFieldKey<ClusterSettingLogAction> action = new StringEnumFieldKey<>("action",
				ClusterSettingLogAction.class)
				.withSize(20);
		public static final StringFieldKey comment = new StringFieldKey("comment");
	}

	public static class ClusterSettingLogFielder extends BaseDatabeanFielder<ClusterSettingLogKey,ClusterSettingLog>{

		public ClusterSettingLogFielder(){
			super(ClusterSettingLogKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ClusterSettingLog databean){
			return List.of(
					new StringEnumField<>(ClusterSettingKey.FieldKeys.scope, databean.scope),
					new StringField(ClusterSettingKey.FieldKeys.serverType, databean.serverType),
					new StringField(ClusterSettingKey.FieldKeys.serverName, databean.serverName),
					new StringField(ClusterSetting.FieldKeys.value, databean.value),
					new StringEnumField<>(FieldKeys.action, databean.action),
					new StringField(FieldKeys.changedBy, databean.changedBy),
					new StringField(FieldKeys.comment, databean.comment));
		}

	}

	public ClusterSettingLog(){
		super(new ClusterSettingLogKey(null, null));
	}

	public ClusterSettingLog(
			ClusterSetting clusterSetting,
			ClusterSettingLogAction action,
			String changedBy,
			String comment){
		super(new ClusterSettingLogKey(clusterSetting.getName(), Instant.now()));
		this.scope = clusterSetting.getScope();
		this.serverType = clusterSetting.getServerType();
		this.serverName = clusterSetting.getServerName();
		this.value = clusterSetting.getValue();
		this.action = action;
		this.changedBy = changedBy;
		this.comment = comment;
	}

	@Override
	public Class<ClusterSettingLogKey> getKeyClass(){
		return ClusterSettingLogKey.class;
	}

	@Override
	public String toString(){
		return getKey().toString() + ":" + value;
	}

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
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

	public ClusterSettingLogAction getAction(){
		return action;
	}

	public String getChangedBy(){
		return changedBy;
	}

	public String getComment(){
		return comment;
	}

}
