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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.ul;

import io.datarouter.web.html.nav.Subnav;
import io.datarouter.web.html.nav.Subnav.Dropdown;
import io.datarouter.web.html.nav.Subnav.DropdownItem;
import j2html.tags.ContainerTag;

public class Bootstrap4SubnavHtml{

	public static ContainerTag render(Subnav subnav){
		var title = a(subnav.name)
				.withClass("navbar-brand mb-0 h1")
				.withHref(subnav.href);
		var ul = ul(each(subnav.dropdowns, Bootstrap4SubnavHtml::makeDropdown))
				.withClass("navbar-nav mr-auto");
		var div = div(ul)
				.withClass("collapse navbar-collapse")
				.withId("joblets-navbar");
		return nav(title, div)
				.withClass("navbar navbar-light bg-light navbar-expand-md border-bottom");
	}

	private static ContainerTag makeDropdown(Dropdown dropdown){
		if(dropdown.items.isEmpty()){
			return null;
		}
		var navLink = a(dropdown.name)
				.attr("data-toggle", "dropdown")
				.withClass("nav-link dropdown-toggle")
				.withHref("#");
		var menu = div(each(dropdown.items, Bootstrap4SubnavHtml::makeItem))
				.withClass("dropdown-menu");
		return li(navLink, menu)
				.withClass("nav-item dropdown");
	}

	private static ContainerTag makeItem(DropdownItem item){
		return a(item.name)
				.condAttr(item.confirm, "onclick", "return confirm('Are you sure?');")
				.withClass("dropdown-item")
				.withHref(item.href);
	}

}
