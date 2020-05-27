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

import io.datarouter.scanner.Scanner;
import io.datarouter.web.navigation.NavBarCategory.SimpleNavBarCategory;
import io.datarouter.web.service.ServiceDocumentationNamesAndLinksSupplier;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

public class AppNavBar extends NavBar{

	private static final Comparator<NavBarMenuItemWrapper> NAVBAR_COMPARATOR = Comparator
			.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.grouping.group)
			.thenComparing(Comparator.comparing((NavBarMenuItemWrapper wrapper) -> wrapper.item.getText()));

	protected AppNavBar(
			Optional<DatarouterAuthenticationConfig> config,
			AppPluginNavBarSupplier pluginSupplier,
			AppNavBarRegistrySupplier registrySupplier,
			ServiceDocumentationNamesAndLinksSupplier docNameAndLinksSupplier){
		super("", "", config);
		List<NavBarItem> readmeLinks = docNameAndLinksSupplier.get().entrySet().stream()
				.map(entry -> new NavBarItem(AppNavBarCategory.README, entry.getValue(), entry.getKey()))
				.collect(Collectors.toList());
		Scanner.concat(
				List.of(new NavBarItem(new SimpleNavBarCategory("Home", AppNavBarCategoryGrouping.HOME), "/", "Home")),
				pluginSupplier.get(),
				registrySupplier.get(),
				readmeLinks)
				.groupBy(item -> item.category)
				.entrySet()
				.stream()
				.map(this::createMenuItem)
				.sorted(NAVBAR_COMPARATOR)
				.map(wrapper -> wrapper.item)
				.forEach(this::addMenuItems);
	}

	private NavBarMenuItemWrapper createMenuItem(Entry<NavBarCategory,List<NavBarItem>> entry){
		if(entry.getValue().size() == 1 && entry.getKey().allowSingleItemMenu()){
			var item = new NavBarMenuItem(entry.getValue().get(0).path, entry.getKey().getDisplay(), this);
			return new NavBarMenuItemWrapper(item, entry.getKey().getGrouping());
		}
		List<NavBarMenuItem> menuItems = entry.getValue().stream()
				.sorted(Comparator.comparing((NavBarItem item) -> item.name))
				.map(item -> new NavBarMenuItem(item.path, item.name, this))
				.collect(Collectors.toList());
		var item = new NavBarMenuItem(entry.getKey().getDisplay(), menuItems);
		return new NavBarMenuItemWrapper(item, entry.getKey().getGrouping());
	}

	private static final class NavBarMenuItemWrapper{

		public final NavBarMenuItem item;
		public final AppNavBarCategoryGrouping grouping;

		public NavBarMenuItemWrapper(NavBarMenuItem item, AppNavBarCategoryGrouping grouping){
			this.item = item;
			this.grouping = grouping;
		}

	}

}
