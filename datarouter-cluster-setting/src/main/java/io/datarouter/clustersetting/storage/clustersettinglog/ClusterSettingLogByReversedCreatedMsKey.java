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

import java.util.List;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLogKey.FieldKeys;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.MilliTimeReversed;

public class ClusterSettingLogByReversedCreatedMsKey
extends BaseRegularPrimaryKey<ClusterSettingLogByReversedCreatedMsKey>
implements FieldlessIndexEntryPrimaryKey<
		ClusterSettingLogByReversedCreatedMsKey,
		ClusterSettingLogKey,
		ClusterSettingLog>{

	private MilliTimeReversed reverseCreatedMs;
	private String name;

	public ClusterSettingLogByReversedCreatedMsKey(){
	}

	public ClusterSettingLogByReversedCreatedMsKey(MilliTimeReversed reverseCreatedMs, String name){
		this.reverseCreatedMs = reverseCreatedMs;
		this.name = name;
	}

	public static ClusterSettingLogByReversedCreatedMsKey prefix(MilliTimeReversed reverseCreatedMs){
		return new ClusterSettingLogByReversedCreatedMsKey(reverseCreatedMs, null);
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new LongEncodedField<>(FieldKeys.reverseCreatedMs, reverseCreatedMs),
				new StringField(ClusterSettingKey.FieldKeys.name, name));
	}

	@Override
	public ClusterSettingLogKey getTargetKey(){
		return new ClusterSettingLogKey(name, reverseCreatedMs);
	}

	@Override
	public FieldlessIndexEntry<ClusterSettingLogByReversedCreatedMsKey,ClusterSettingLogKey,ClusterSettingLog>
	createFromDatabean(ClusterSettingLog target){
		return new FieldlessIndexEntry<>(
				ClusterSettingLogByReversedCreatedMsKey::new,
				new ClusterSettingLogByReversedCreatedMsKey(
						target.getKey().getMilliTimeReversed(),
						target.getKey().getName()));
	}

}
