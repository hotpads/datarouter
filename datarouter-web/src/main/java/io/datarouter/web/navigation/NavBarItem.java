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
import java.util.stream.Collectors;

import io.datarouter.httpclient.path.PathNode;

public class NavBarItem{

	public final NavBarCategory category;
	public final String path;
	public final String name;

	public NavBarItem(NavBarCategory category, PathNode pathNode, String name){
		this(category, pathNode.toSlashedString(), name);
	}

	public NavBarItem(NavBarCategory category, String path, String name){
		this.category = category;
		this.path = path;
		this.name = name;
	}

	public static class NavBarItemGroup{

		public final NavBarCategory category;
		public final List<NavBarItem> items;

		private NavBarItemGroup(NavBarCategory category, List<NavBarItem> items){
			this.category = category;
			this.items = items;
		}

		public static List<NavBarItemGroup> fromNavBarItems(List<NavBarItem> items){
			return items.stream()
					.collect(Collectors.groupingBy(item -> item.category)).entrySet().stream()
					.map(group -> new NavBarItemGroup(group.getKey(), group.getValue()))
					.sorted(Comparator.comparing(NavBarItemGroup::getCategoryDisplay))
					.collect(Collectors.toList());
		}

		private String getCategoryDisplay(){
			return category.getDisplay();
		}

	}
}
