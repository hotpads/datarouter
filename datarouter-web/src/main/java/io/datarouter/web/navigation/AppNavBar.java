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

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.navigation.DynamicNavBarItem.DynamicNavBarItemType;
import io.datarouter.web.navigation.NavBarCategory.SimpleNavBarCategory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

public class AppNavBar extends NavBar{

	private static final Comparator<NavBarMenuItemWrapper> NAVBAR_COMPARATOR = Comparator
			.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.grouping)
			.thenComparing(Comparator.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.item.getText()));

	protected AppNavBar(
			Optional<DatarouterAuthenticationConfig> config,
			AppPluginNavBarSupplier pluginSupplier,
			AppNavBarRegistrySupplier registrySupplier,
			DynamicNavBarItemRegistry dynamicNavBarItemRegistry,
			DatarouterInjector injector){
		super("", "", config);
		List<NavBarItem> dynamicNavBarItems = Scanner.of(dynamicNavBarItemRegistry.items)
				.map(injector::getInstance)
				.include(item -> item.getType() == DynamicNavBarItemType.APP)
				.include(DynamicNavBarItem::shouldDisplay)
				.map(DynamicNavBarItem::getNavBarItem)
				.list();
		Scanner.concat(
				List.of(new NavBarItem(new SimpleNavBarCategory("Home", AppNavBarCategoryGrouping.HOME, true), "/",
						"Home")),
				pluginSupplier.get(),
				registrySupplier.get(),
				dynamicNavBarItems)
				.groupBy(item -> item.category.toDto())
				.entrySet()
				.stream()
				.map(this::createMenuItem)
				.sorted(NAVBAR_COMPARATOR)
				.map(wrapper -> wrapper.item)
				.forEach(this::addMenuItems);
	}

	private NavBarMenuItemWrapper createMenuItem(Entry<SimpleNavBarCategory,List<NavBarItem>> entry){
		if(entry.getValue().size() == 1 && entry.getKey().allowSingleItemMenu()){
			var item = new NavBarMenuItem(
					entry.getValue().get(0).path,
					entry.getKey().getDisplay(),
					entry.getValue().get(0).openInNewTab,
					this);
			entry.getValue().get(0).dispatchRule.ifPresent(item::setDispatchRule);
			return new NavBarMenuItemWrapper(item, entry.getKey().getGrouping().group);
		}
		List<NavBarMenuItem> menuItems = entry.getValue().stream()
				.sorted(Comparator.comparing((NavBarItem item) -> item.name))
				.map(item -> {
					var menuItem = new NavBarMenuItem(item.path, item.name, item.openInNewTab, this);
					item.dispatchRule.ifPresent(menuItem::setDispatchRule);
					return menuItem;
				})
				.collect(Collectors.toList());
		var item = new NavBarMenuItem(entry.getKey().getDisplay(), menuItems);
		return new NavBarMenuItemWrapper(item, entry.getKey().getGrouping().group);
	}

	private static final class NavBarMenuItemWrapper{

		public final NavBarMenuItem item;
		public final int grouping;

		public NavBarMenuItemWrapper(NavBarMenuItem item, int grouping){
			this.item = item;
			this.grouping = grouping;
		}

	}

}
