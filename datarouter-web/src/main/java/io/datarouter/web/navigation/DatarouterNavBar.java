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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNavBar extends NavBar{

	@Inject
	public DatarouterNavBar(
			DatarouterWebFiles webFiles,
			Optional<DatarouterAuthenticationConfig> config,
			DatarouterNavBarSupplier navBarSupplier,
			DynamicNavBarItemRegistry dynamicNavBarItemRegistry){
		super(webFiles.jeeAssets.datarouterLogoPng.toSlashedString(), "Datarouter logo", config);
		List<NavBarItem> dynamicNavBarItems = Scanner.of(dynamicNavBarItemRegistry.get())
				.include(item -> item.getType() == NavBarItemType.DATAROUTER)
				.include(DynamicNavBarItem::shouldDisplay)
				.map(DynamicNavBarItem::getNavBarItem)
				.list();
		List<NavBarItem> navBarItems = Scanner.of(navBarSupplier.get())
				.append(dynamicNavBarItems)
				.list();
		Map<NavBarCategory,List<NavBarItem>> itemsByCategory = Scanner.of(navBarItems)
				.groupBy(
						item -> item.category,
						Function.identity(),
						() -> new TreeMap<>(Comparator.comparing(NavBarCategory::sortBy)));
		Scanner.of(itemsByCategory.entrySet())
				.map(this::createMenuItem)
				.forEach(this::addMenuItems);
	}

	private NavBarMenuItem createMenuItem(Entry<NavBarCategory,List<NavBarItem>> entry){
		if(entry.getValue().size() == 1 && entry.getKey().allowSingleItemMenu()){
			var item = new NavBarMenuItem(
					entry.getValue().getFirst().path,
					entry.getKey().display(),
					entry.getValue().getFirst().openInNewTab,
					this);
			entry.getValue().getFirst().dispatchRule.ifPresent(item::setDispatchRule);
			return item;
		}
		List<NavBarMenuItem> menuItems = entry.getValue().stream()
				.sorted(Comparator.comparing((NavBarItem item) -> item.name))
				.map(item -> {
					var menuItem = new NavBarMenuItem(item.path, item.name, item.openInNewTab, this);
					item.dispatchRule.ifPresent(menuItem::setDispatchRule);
					return menuItem;
				})
				.toList();
		return new NavBarMenuItem(entry.getKey().display(), menuItems);
	}

}
