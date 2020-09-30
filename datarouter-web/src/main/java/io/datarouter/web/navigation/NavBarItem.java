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
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.dispatcher.DispatchRule;

public class NavBarItem{

	public final NavBarCategory category;
	public final String path;
	public final String name;
	public final boolean openInNewTab;
	public final Optional<DispatchRule> dispatchRule;

	public NavBarItem(NavBarCategory category, String path, String name){
		this(category, path, name, false, null);
	}

	public NavBarItem(NavBarCategory category, PathNode pathNode, String name){
		this(category, pathNode.toSlashedString(), name, false, null);
	}

	private NavBarItem(NavBarCategory category, String path, String name, boolean openInNewTab,
			DispatchRule dispatchRule){
		this.category = category;
		this.path = path;
		this.name = name;
		this.openInNewTab = openInNewTab;
		this.dispatchRule = Optional.ofNullable(dispatchRule);
	}

	public static class NavBarItemBuilder{

		private final NavBarCategory category;
		private final String path;
		private final String name;

		private boolean openInNewTab = false;
		private DispatchRule dispatchRule;

		public NavBarItemBuilder(NavBarCategory category, PathNode pathNode, String name){
			this(category, pathNode.toSlashedString(), name);
		}

		public NavBarItemBuilder(NavBarCategory category, String path, String name){
			this.category = category;
			this.path = path;
			this.name = name;
		}

		public NavBarItemBuilder openInNewTab(){
			openInNewTab = true;
			return this;
		}

		public NavBarItemBuilder setDispatchRule(DispatchRule dispatchRule){
			this.dispatchRule = dispatchRule;
			return this;
		}

		public NavBarItem build(){
			return new NavBarItem(category, path, name, openInNewTab, dispatchRule);
		}

	}

	public static class NavBarItemGroup{

		public final NavBarCategory category;
		public final List<NavBarItem> items;

		private NavBarItemGroup(NavBarCategory category, List<NavBarItem> items){
			this.category = category.toDto();
			this.items = items;
		}

		public static List<NavBarItemGroup> fromNavBarItems(List<NavBarItem> items){
			return Scanner.of(items)
					.groupBy(item -> item.category)
					.entrySet()
					.stream()
					.map(group -> new NavBarItemGroup(group.getKey(), group.getValue()))
					.sorted(Comparator.comparing(NavBarItemGroup::getCategoryDisplay))
					.collect(Collectors.toList());
		}

		private String getCategoryDisplay(){
			return category.getDisplay();
		}

	}

}
