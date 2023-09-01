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
import java.util.List;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeReversedFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.MilliTimeReversed;

public class ClusterSettingLogKey extends BaseRegularPrimaryKey<ClusterSettingLogKey>{

	private String name;
	private MilliTimeReversed reverseCreatedMs;

	public static class FieldKeys{
		public static final LongEncodedFieldKey<MilliTimeReversed> reverseCreatedMs = new LongEncodedFieldKey<>(
				"reverseCreatedMs", new MilliTimeReversedFieldCodec());
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(ClusterSettingKey.FieldKeys.name, name),
				new LongEncodedField<>(FieldKeys.reverseCreatedMs, reverseCreatedMs));
	}

	public ClusterSettingLogKey(){
	}

	public ClusterSettingLogKey(String name, MilliTimeReversed reverseCreatedMs){
		this.name = name;
		this.reverseCreatedMs = reverseCreatedMs;
	}

	public static ClusterSettingLogKey forInstant(String name, Instant instant){
		return new ClusterSettingLogKey(name, MilliTimeReversed.of(instant));
	}

	public static ClusterSettingLogKey prefix(String name){
		return new ClusterSettingLogKey(name, null);
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public MilliTimeReversed getMilliTimeReversed(){
		return reverseCreatedMs;
	}

}
