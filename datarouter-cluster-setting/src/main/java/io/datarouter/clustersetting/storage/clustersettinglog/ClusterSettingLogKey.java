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
package io.datarouter.clustersetting.storage.clustersettinglog;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.DateTool;

public class ClusterSettingLogKey extends BaseRegularPrimaryKey<ClusterSettingLogKey>{

	private String name;
	private Long reverseCreatedMs;

	public static class FieldKeys{
		public static final LongFieldKey reverseCreatedMs = new LongFieldKey("reverseCreatedMs");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(ClusterSettingKey.FieldKeys.name, name),
				new LongField(FieldKeys.reverseCreatedMs, reverseCreatedMs));
	}

	public ClusterSettingLogKey(){
	}

	public ClusterSettingLogKey(String name, Instant instant){
		this.name = name;
		this.reverseCreatedMs = DateTool.toReverseInstantLong(instant);
	}

	public static ClusterSettingLogKey createPrefix(String name){
		return new ClusterSettingLogKey(name, null);
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public Long getReverseCreatedMs(){
		return reverseCreatedMs;
	}

	public Date getCreated(){
		return DateTool.fromReverseDateLong(reverseCreatedMs);
	}

}
