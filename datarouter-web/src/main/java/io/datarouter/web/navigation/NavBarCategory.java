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
package io.datarouter.web.navigation;

import java.util.Objects;

import io.datarouter.enums.Displayable;

public interface NavBarCategory extends Displayable{

	default AppNavBarCategoryGrouping getGrouping(){
		return AppNavBarCategoryGrouping.MISC;
	}

	default boolean allowSingleItemMenu(){
		return true;
	}

	default SimpleNavBarCategory toDto(){
		return new SimpleNavBarCategory(getDisplay(), getGrouping(), allowSingleItemMenu());
	}

	default NavBarItemType getType(){
		return NavBarItemType.APP;
	}

	enum NavBarItemType{
		APP,
		DATAROUTER,
		;
	}

	class SimpleNavBarCategory implements NavBarCategory{

		private final String display;
		private final AppNavBarCategoryGrouping grouping;
		private final boolean allowSingleItemMenu;

		public SimpleNavBarCategory(String display, AppNavBarCategoryGrouping grouping, boolean allowSingleItemMenu){
			this.display = display;
			this.grouping = grouping;
			this.allowSingleItemMenu = allowSingleItemMenu;
		}

		@Override
		public String getDisplay(){
			return display;
		}

		@Override
		public AppNavBarCategoryGrouping getGrouping(){
			return grouping;
		}

		@Override
		public boolean allowSingleItemMenu(){
			return allowSingleItemMenu;
		}

		@Override
		public NavBarItemType getType(){
			return NavBarItemType.APP;
		}

		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof NavBarCategory)){
				return false;
			}
			NavBarCategory that = (NavBarCategory) other;
			return this.getDisplay().equals(that.getDisplay())
					&& this.getGrouping().group == that.getGrouping().group
					&& this.allowSingleItemMenu() == that.allowSingleItemMenu();
		}

		@Override
		public int hashCode(){
			return Objects.hash(display, grouping.group, allowSingleItemMenu);
		}

	}

}
