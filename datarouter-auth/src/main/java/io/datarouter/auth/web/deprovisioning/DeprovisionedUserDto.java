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
package io.datarouter.auth.web.deprovisioning;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

public class DeprovisionedUserDto{

	public static final Comparator<DeprovisionedUserDto> COMPARATOR = new DeprovisionedUserDtoComparator();

	public final String username;
	public final List<String> roles;

	public DeprovisionedUserDto(String username, List<String> roles){
		this.username = username;
		this.roles = List.copyOf(roles);
	}

	private static class DeprovisionedUserDtoComparator implements Comparator<DeprovisionedUserDto>{

		//@s in usernames make String.CASE_INSENSITIVE_ORDER behave really weirdly in testing
		private static final Collator COLLATOR = Collator.getInstance();

		@Override
		public int compare(DeprovisionedUserDto first, DeprovisionedUserDto second){
			return COLLATOR.compare(first.username, second.username);
		}

	}

}
