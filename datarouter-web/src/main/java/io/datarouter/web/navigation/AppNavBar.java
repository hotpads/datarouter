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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.web.navigation.NavBarCategory.SimpleNavBarCategory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

public class AppNavBar extends NavBar{

	protected AppNavBar(Optional<DatarouterAuthenticationConfig> config, AppPluginNavBarSupplier pluginSupplier,
			AppNavBarRegistrySupplier registrySupplier){
		super("", "", config);
		List<NavBarItem> items = new ArrayList<>();
		items.add(new NavBarItem(new SimpleNavBarCategory("Home", 0), "/", "Home"));
		items.addAll(pluginSupplier.get());
		items.addAll(registrySupplier.get());
		items.stream()
				.collect(Collectors.groupingBy(item -> item.category))
				.entrySet()
				.stream()
				.map(this::createMenuItem)
				.sorted(Comparator.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.priority)
						.thenComparing(Comparator.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.item.getText())))
				.map(wrapper -> wrapper.item)
				.forEach(this::addMenuItems);
	}

	private NavBarMenuItemWrapper createMenuItem(Entry<NavBarCategory,List<NavBarItem>> entry){
		if(entry.getValue().size() == 1 && entry.getKey().allowSingleItemMenu()){
			var item = new NavBarMenuItem(entry.getValue().get(0).path, entry.getKey().getDisplay(), this);
			return new NavBarMenuItemWrapper(item, entry.getKey().getPriority());
		}
		List<NavBarMenuItem> menuItems = entry.getValue().stream()
				.sorted(Comparator.comparing((NavBarItem item) -> item.name))
				.map(item -> new NavBarMenuItem(item.path, item.name, this))
				.collect(Collectors.toList());
		var item = new NavBarMenuItem(entry.getKey().getDisplay(), menuItems);
		return new NavBarMenuItemWrapper(item, entry.getKey().getPriority());
	}

	private static final class NavBarMenuItemWrapper{

		public final NavBarMenuItem item;
		public final Integer priority;

		public NavBarMenuItemWrapper(NavBarMenuItem item, Integer priority){
			this.item = item;
			this.priority = priority;
		}

	}

}
