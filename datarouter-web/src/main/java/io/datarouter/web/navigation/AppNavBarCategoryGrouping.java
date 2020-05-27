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
package io.datarouter.web.navigation;

public enum AppNavBarCategoryGrouping{
	HOME(1),
	ADMIN(2),
	README(3),
	API_DOCS(4),
	PLATFORM(5),
	APP(6),
	PLUGINS(7),
	MISC(8),
	;

	public final int group;

	AppNavBarCategoryGrouping(int group){
		this.group = group;
	}

}
