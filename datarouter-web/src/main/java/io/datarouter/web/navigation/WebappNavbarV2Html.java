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

import java.util.Objects;

import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.nav.NavBar;
import io.datarouter.web.handler.mav.nav.NavBarMenuItem;
import j2html.TagCreator;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;

//for bootstrap 4
public class WebappNavbarV2Html{

	private MavProperties props;
	private NavBar navbar;

	public WebappNavbarV2Html(MavProperties props, NavBar navbar){
		this.props = Objects.requireNonNull(props);
		this.navbar = Objects.requireNonNull(navbar);
	}

	public ContainerTag build(){
		var nav = TagCreator.nav()
				.attr(Attr.ID, "app-navbar")
				.withClass("navbar navbar-expand-md navbar-dark bg-dark");
		var button = TagCreator.button()
				.withClass("navbar-toggler")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#app-navbar-content");
		var span = TagCreator.span()
				.withClass("navbar-toggler-icon");
		return nav
				.condWith(props.getIsDatarouterPage(), makeDatarouterLogo())
				.with(button.with(span))
				.with(makeAppNavbarContent());
	}

	private ContainerTag makeDatarouterLogo(){
		var link = TagCreator.a()
				.withStyle("navbar-brand d-inline-flex")
				.withHref(props.getContextPath() + "/datarouter");
		var img = TagCreator.img()
				.withClass("align-top")
				.withStyle("height: 1.5rem")
				.withSrc(props.getContextPath() + navbar.getLogoSrc())
				.withAlt(navbar.getLogoAlt());
		return link.with(img);
	}

	private ContainerTag makeAppNavbarContent(){
		var div = TagCreator.div()
				.withId("app-navbar-content")
				.withClass("collapse navbar-collapse");
		return div.with(
				makeAppNavBarList(),
				makeSignOut());
	}

	private ContainerTag makeAppNavBarList(){
		var ul = TagCreator.ul()
				.withClass("navbar-nav mr-auto");
		var menus = navbar.getMenuItems(props.getRequest()).stream()
				.map(menuItem -> menuItem.isDropdown() ? makeDropdownMenuItem(menuItem)
						: makeNonDropdownMenuItem(menuItem))
				.toArray(ContainerTag[]::new);
		return ul.with(menus);
	}

	private ContainerTag makeDropdownMenuItem(NavBarMenuItem menuItem){
		var li = TagCreator.li()
				.withClass("nav-item dropdown");
		var span = TagCreator.span()
				.withClass("caret");
		var link = TagCreator.a()
				.withClass("nav-link dropdown-toggle")
				.attr("data-toggle", "dropdown")
				.withHref("#")
				.withText(menuItem.getText())
				.with(span);
		return li
				.with(link)
				.with(makeDropdown(menuItem));
	}

	private ContainerTag makeDropdown(NavBarMenuItem menuItem){
		var div = TagCreator.div()
				.withClass("dropdown-menu");
		var links = menuItem.getSubItems(props.getRequest()).stream()
				.map(this::makeDropdownLink)
				.toArray(ContainerTag[]::new);
		return div.with(links);
	}

	private ContainerTag makeDropdownLink(NavBarMenuItem menuItem){
		return TagCreator.a(menuItem.getText())
				.withClass("dropdown-item")
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
	}

	private ContainerTag makeNonDropdownMenuItem(NavBarMenuItem menuItem){
		var li = TagCreator.li()
				.withClass("nav-item");
		var link = TagCreator.a(menuItem.getText())
				.withClass("nav-link")
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li.with(link);
	}

	private ContainerTag makeSignOut(){
		var ul = TagCreator.ul()
				.withClass("navbar-nav");
		var li = TagCreator.li()
				.withClass("nav-item");
		var link = TagCreator.a("Sign out")
				.withClass("nav-link")
				.withHref(props.getContextPath() + "/signout");
		return ul.with(li.with(link));
	}

}
