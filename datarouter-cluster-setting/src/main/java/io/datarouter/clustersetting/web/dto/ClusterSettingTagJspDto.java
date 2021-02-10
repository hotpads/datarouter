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
package io.datarouter.clustersetting.web.dto;

public class ClusterSettingTagJspDto{

	private final String settingTag;
	private final String value;
	private final boolean winner;

	public ClusterSettingTagJspDto(
			String settingTag,
			String value,
			boolean winner){
		this.settingTag = settingTag;
		this.value = value;
		this.winner = winner;
	}

	public boolean getWinner(){
		return winner;
	}

	public String getSettingTag(){
		return settingTag;
	}

	public String getValue(){
		return value;
	}

}
