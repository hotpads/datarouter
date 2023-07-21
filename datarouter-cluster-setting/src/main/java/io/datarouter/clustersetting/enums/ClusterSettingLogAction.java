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

import io.datarouter.enums.StringMappedEnum;

public enum ClusterSettingLogAction{
	INSERTED("inserted"),
	UPDATED("updated"),
	DELETED("deleted");

	public static final StringMappedEnum<ClusterSettingLogAction> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString, 20);

	public final String persistentString;

	ClusterSettingLogAction(String value){
		this.persistentString = value;
	}

}
