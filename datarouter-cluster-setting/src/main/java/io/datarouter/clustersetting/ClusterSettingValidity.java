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
package io.datarouter.clustersetting;

import java.util.Arrays;
import java.util.stream.Stream;

import io.datarouter.enums.PersistentString;

public enum ClusterSettingValidity implements PersistentString{
	VALID(
			"valid",
			"Valid",
			"table-default",
			"Valid override",
			ClusterSettingOverrideSuggestion.NOTHING),
	INVALID_SERVER_TYPE(
			"invalidServerType",
			"Invalid Server Type",
			"table-primary",
			"Unknown serverType",
			ClusterSettingOverrideSuggestion.DELETE),
	INVALID_SERVER_NAME(
			"invalidServerName",
			"Invalid Server Name",
			"table-success",
			"Unknown serverName",
			ClusterSettingOverrideSuggestion.DELETE),
	REDUNDANT(
			"redundant",
			"Redundant",
			"table-warning",
			"Value duplicates the default value",
			ClusterSettingOverrideSuggestion.DELETE),
	OLD(
			"old",
			"Old",
			"table-info",
			"Setting hasn't changed in a while",
			ClusterSettingOverrideSuggestion.MOVE_TO_CODE),
	UNREFERENCED(
			"unreferenced",
			"Unknown Setting",
			"table-danger",
			"Setting not in code",
			ClusterSettingOverrideSuggestion.DELETE),
	UNKNOWN(
			"unknown",
			"Unknown Root",
			"table-secondary",
			"Setting root not in code",
			ClusterSettingOverrideSuggestion.DELETE),
	;

	public final String persistentString;
	public final String display;
	public final String color;
	public final String description;
	public final ClusterSettingOverrideSuggestion overrideSuggestion;

	ClusterSettingValidity(
			String persistentString,
			String display,
			String color,
			String description,
			ClusterSettingOverrideSuggestion overrideSuggestion){
		this.persistentString = persistentString;
		this.display = display;
		this.color = color;
		this.description = description;
		this.overrideSuggestion = overrideSuggestion;
	}

	public static Stream<ClusterSettingValidity> stream(){
		return Arrays.stream(values());
	}

	// jsp
	@Override
	public String getPersistentString(){
		return persistentString;
	}

	// jsp
	public String getColor(){
		return color;
	}

	// jsp
	public String getDescription(){
		return description;
	}

}
