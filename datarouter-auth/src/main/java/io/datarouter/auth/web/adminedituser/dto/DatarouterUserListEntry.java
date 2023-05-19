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
package io.datarouter.auth.web.adminedituser.dto;

public class DatarouterUserListEntry{

	public final String id;
	public final String username;
	public final String token;
	public final boolean hasPermissionRequest;
	public final String profileLink;
	public final String profileClass;

	public DatarouterUserListEntry(
			String id,
			String username,
			String token,
			boolean hasPermissionRequest,
			String profileLink){
		this.id = id;
		this.username = username;
		this.token = token;
		this.hasPermissionRequest = hasPermissionRequest;
		this.profileLink = profileLink;
		this.profileClass = profileLink.isEmpty() ? "hidden" : "";
	}
}
