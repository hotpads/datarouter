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

import io.datarouter.util.enums.Displayable;

// TODO add support for parent category
public interface NavBarCategory extends Displayable{

	default Integer getPriority(){
		return 1_000;
	}

	static class SimpleNavBarCategory implements NavBarCategory{

		private final String display;
		private final Integer priority;

		public SimpleNavBarCategory(String display, Integer priority){
			this.display = display;
			this.priority = priority;
		}

		@Override
		public String getDisplay(){
			return display;
		}

		@Override
		public Integer getPriority(){
			return priority;
		}

	}

}
