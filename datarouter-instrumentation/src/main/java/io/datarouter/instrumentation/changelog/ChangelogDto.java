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
package io.datarouter.instrumentation.changelog;

public class ChangelogDto{

	public final String serviceName;
	public final String changelogType;
	public final String name;
	public final long dateMs;
	public final String action;
	public final String username;
	public final String userToken;
	public final String comment;

	public ChangelogDto(String serviceName, String changelogType, String name, long dateMs, String action,
			String username, String userToken, String comment){
		this.serviceName = serviceName;
		this.changelogType = changelogType;
		this.name = name;
		this.dateMs = dateMs;
		this.action = action;
		this.username = username;
		this.userToken = userToken;
		this.comment = comment;
	}

	public long getReversedDateMs(){
		return Long.MAX_VALUE - dateMs;
	}

}
