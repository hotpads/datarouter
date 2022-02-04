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

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;

public interface DynamicNavBarItem extends PluginConfigValue<DynamicNavBarItem>{

	PluginConfigKey<DynamicNavBarItem> KEY = new PluginConfigKey<>("dynamicNavBarItems", PluginConfigType.CLASS_LIST);

	NavBarItem getNavBarItem();
	Boolean shouldDisplay();
	NavBarItemType getType();

	@Override
	default PluginConfigKey<DynamicNavBarItem> getKey(){
		return KEY;
	}

	default boolean changesFrequently(){
		return false;
	}

}
