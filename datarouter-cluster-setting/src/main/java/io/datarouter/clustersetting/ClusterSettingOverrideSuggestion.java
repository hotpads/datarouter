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

public enum ClusterSettingOverrideSuggestion{
	DELETE(
			true,
			"Delete",
			"Delete this override"),
	MOVE_TO_CODE(
			true,
			"Move to code",
			"Declare this value in the code"),
	NOTHING(
			false,
			"Nothing",
			"Everything looks good");

	public final boolean hasSuggestion;
	public final String display;
	public final String description;

	private ClusterSettingOverrideSuggestion(
			boolean hasSuggestion,
			String display,
			String description){
		this.hasSuggestion = hasSuggestion;
		this.display = display;
		this.description = description;
	}

}
