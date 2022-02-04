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
package io.datarouter.storage.setting;

import java.util.Objects;

import io.datarouter.enums.Displayable;

public interface SettingCategory extends Displayable{

	default SimpleSettingCategory toSimpleSettingCategory(){
		return new SimpleSettingCategory(getDisplay());
	}

	class SimpleSettingCategory implements SettingCategory{

		private final String display;

		public SimpleSettingCategory(String display){
			this.display = display;
		}

		@Override
		public String getDisplay(){
			return display;
		}

		public String getHref(){
			return "category-" + getDisplay().replace(" ", "-");
		}

		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof SimpleSettingCategory)){
				return false;
			}
			SimpleSettingCategory that = (SimpleSettingCategory) other;
			return this.getDisplay().equals(that.getDisplay());
		}

		@Override
		public int hashCode(){
			return Objects.hash(display);
		}

	}

}
