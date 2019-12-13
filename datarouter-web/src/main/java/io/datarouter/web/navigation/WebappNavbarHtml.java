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

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.img;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import java.util.Objects;

import io.datarouter.web.handler.mav.MavProperties;
import j2html.tags.ContainerTag;

//for bootstrap 3
public class WebappNavbarHtml{

	private final MavProperties props;
	private final NavBar navbar;
	private final String navbarTarget;

	public WebappNavbarHtml(MavProperties props, NavBar navbar){
		this.props = Objects.requireNonNull(props);
		this.navbar = Objects.requireNonNull(navbar);
		this.navbarTarget = props.getIsDatarouterPage() ? "#dr-navbar" : "#mav-navbar";
	}

	public ContainerTag build(){
		var container = div(makeHeader(), makeContent())
				.withClass("container-fluid");
		return div(container)
				.withClass("navbar navbar-inverse navbar-static-top");
	}

	private ContainerTag makeHeader(){
		var iconBar = span()
				.withClass("icon-bar");
		var button = button(iconBar, iconBar, iconBar)
				.withClass("navbar-toggle collapsed")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", navbarTarget);
		return div(button)
				.withClass("navbar-header")
				.condWith(props.getIsDatarouterPage(), makeDatarouterLogo());
	}

	private ContainerTag makeDatarouterLogo(){
		var img = img()
				.withClass("logo-brand")
				.withSrc(props.getContextPath() + navbar.getLogoSrc())
				.withAlt(navbar.getLogoAlt())
				.attr("onclick", "return false");
		return a(img)
				.withStyle("navbar-brand")
				.withHref("#");
	}

	private ContainerTag makeContent(){
		return div(makeMenu(), makeSignOut())
				.withId(navbarTarget)
				.withClass("navbar-collapse collapse");
	}

	private ContainerTag makeMenu(){
		var menus = navbar.getMenuItems(props.getRequest()).stream()
				.map(menuItem -> menuItem.isDropdown() ? makeDropdownMenuItem(menuItem)
						: makeNonDropdownMenuItem(menuItem))
				.toArray(ContainerTag[]::new);
		return ul(menus)
				.withClass("nav navbar-nav");
	}

	private ContainerTag makeDropdownMenuItem(NavBarMenuItem menuItem){
		var caret = span()
				.withClass("caret");
		var link = a(caret, text(menuItem.getText()))
				.withClass("dropdown-toggle")
				.attr("data-toggle", "dropdown")
				.withHref("#");
		return li(link, makeDropdown(menuItem))
				.withClass("dropdown");
	}

	private ContainerTag makeDropdown(NavBarMenuItem menuItem){
		return ul(each(menuItem.getSubItems(props.getRequest()), this::makeDropdownListItem))
				.withClass("dropdown-menu");
	}

	private ContainerTag makeDropdownListItem(NavBarMenuItem menuItem){
		var link = a(menuItem.getText())
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li(link);
	}

	private ContainerTag makeNonDropdownMenuItem(NavBarMenuItem menuItem){
		var link = a(menuItem.getText())
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li(link);
	}

	private ContainerTag makeSignOut(){
		var link = a("Sign out")
				.withHref(props.getContextPath() + "/signout");
		return ul(li(link))
				.withClass("nav navbar-nav navbar-right");
	}

}
