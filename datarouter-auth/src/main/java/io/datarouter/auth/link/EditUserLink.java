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
package io.datarouter.auth.link;

import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;

public class EditUserLink extends DatarouterLink{

	public static final String
			P_username = "username",
			P_userId = "userId";

	public Optional<String> username = Optional.empty();
	public Optional<String> userId = Optional.empty();

	public EditUserLink(){
		super(new DatarouterAuthPaths().admin.editUser);
	}

	public EditUserLink withUsername(String username){
		this.username = Optional.of(username);
		return this;
	}

	public EditUserLink withUserId(Long userId){
		this.userId = Optional.of(userId).map(String::valueOf);
		return this;
	}

}
