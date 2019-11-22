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
import j2html.tags.ContainerTag;

//for bootstrap 3
public class WebappNavbarHtml{

	private MavProperties props;
	private NavBar navbar;
	private String navbarTarget;

	public WebappNavbarHtml(MavProperties props, NavBar navbar){
		this.props = Objects.requireNonNull(props);
		this.navbar = Objects.requireNonNull(navbar);
		this.navbarTarget = props.getIsDatarouterPage() ? "#dr-navbar" : "#mav-navbar";
	}

	public ContainerTag build(){
		var div = TagCreator.div()
				.withClass("navbar navbar-inverse navbar-static-top");
		var container = TagCreator.div()
				.withClass("container-fluid");

		return div.with(container
				.with(makeHeader())
				.with(makeContent()));
	}

	private ContainerTag makeHeader(){
		var header = TagCreator.div()
				.withClass("navbar-header");
		var button = TagCreator.button()
				.withClass("navbar-toggle collapsed")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", navbarTarget);
		var span = TagCreator.span()
				.withClass("icon-bar");
		return header
				.with(button.with(span).with(span).with(span))
				.condWith(props.getIsDatarouterPage(), makeDatarouterLogo());
	}

	private ContainerTag makeDatarouterLogo(){
		var link = TagCreator.a()
				.withStyle("navbar-brand")
				.withHref("#");
		var img = TagCreator.img()
				.withClass("logo-brand")
				.withSrc(props.getContextPath() + navbar.getLogoSrc())
				.withAlt(navbar.getLogoAlt())
				.attr("onclick", "return false");
		return link.with(img);
	}

	private ContainerTag makeContent(){
		var div = TagCreator.div()
				.withId(navbarTarget)
				.withClass("navbar-collapse collapse");
		return div.with(
				makeMenu(),
				makeSignOut());
	}

	private ContainerTag makeMenu(){
		var ul = TagCreator.ul()
				.withClass("nav navbar-nav");
		var menus = navbar.getMenuItems(props.getRequest()).stream()
				.map(menuItem -> menuItem.isDropdown() ? makeDropdownMenuItem(menuItem)
						: makeNonDropdownMenuItem(menuItem))
				.toArray(ContainerTag[]::new);
		return ul.with(menus);
	}

	private ContainerTag makeDropdownMenuItem(NavBarMenuItem menuItem){
		var li = TagCreator.li()
				.withClass("dropdown");
		var span = TagCreator.span()
				.withClass("caret");
		var link = TagCreator.a()
				.withClass("dropdown-toggle")
				.attr("data-toggle", "dropdown")
				.withHref("#")
				.withText(menuItem.getText());
		return li
				.with(link.with(span))
				.with(makeDropdown(menuItem));
	}

	private ContainerTag makeDropdown(NavBarMenuItem menuItem){
		var ul = TagCreator.ul()
				.withClass("dropdown-menu");
		var listItems = menuItem.getSubItems(props.getRequest()).stream()
				.map(this::makeDropdownListItem)
				.toArray(ContainerTag[]::new);
		return ul.with(listItems);
	}

	private ContainerTag makeDropdownListItem(NavBarMenuItem menuItem){
		var li = TagCreator.li();
		var link = TagCreator.a(menuItem.getText())
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li.with(link);
	}

	private ContainerTag makeNonDropdownMenuItem(NavBarMenuItem menuItem){
		var li = TagCreator.li();
		var link = TagCreator.a(menuItem.getText())
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li.with(link);
	}

	private ContainerTag makeSignOut(){
		var ul = TagCreator.ul()
				.withClass("nav navbar-nav navbar-right");
		var li = TagCreator.li();
		var link = TagCreator.a("Sign out")
				.withHref(props.getContextPath() + "/signout");
		return ul.with(li.with(link));
	}

}
