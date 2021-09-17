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

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.img;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.span;
import static j2html.TagCreator.ul;

import java.util.Objects;

import io.datarouter.web.handler.mav.MavProperties;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.LiTag;
import j2html.tags.specialized.NavTag;
import j2html.tags.specialized.UlTag;

//for bootstrap 4
public class WebappNavbarV2Html{

	private MavProperties props;
	private NavBar navbar;

	public WebappNavbarV2Html(MavProperties props, NavBar navbar){
		this.props = Objects.requireNonNull(props);
		this.navbar = Objects.requireNonNull(navbar);
	}

	public NavTag build(){
		var span = span()
				.withClass("navbar-toggler-icon");
		var button = button(span)
				.withClass("navbar-toggler")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#app-navbar-content");
		return nav()
				.attr(Attr.ID, "app-navbar")
				.withClass("navbar navbar-expand-md navbar-dark bg-dark")
				.condWith(props.getIsDatarouterPage(), makeDatarouterLogo())
				.with(button)
				.with(makeAppNavbarContent());
	}

	private ATag makeDatarouterLogo(){
		var img = img()
				.withClass("align-top")
				.withStyle("height: 1.5rem")
				.withSrc(props.getContextPath() + navbar.getLogoSrc())
				.withAlt(navbar.getLogoAlt());
		return a(img)
				.withStyle("navbar-brand d-inline-flex")
				.withHref(props.getContextPath() + "/datarouter");
	}

	private DivTag makeAppNavbarContent(){
		return div(makeAppNavBarList(), makeSignOut())
				.withId("app-navbar-content")
				.withClass("collapse navbar-collapse");
	}

	private UlTag makeAppNavBarList(){
		var menus = navbar.getMenuItems(props.getRequest()).stream()
				.map(menuItem -> menuItem.isDropdown() ? makeDropdownMenuItem(menuItem)
						: makeNonDropdownMenuItem(menuItem))
				.toArray(ContainerTag[]::new);
		return ul(menus)
				.withClass("navbar-nav mr-auto");
	}

	private LiTag makeDropdownMenuItem(NavBarMenuItem menuItem){
		var span = span()
				.withClass("caret");
		var link = a()
				.withClass("nav-link dropdown-toggle")
				.attr("data-toggle", "dropdown")
				.withHref("#")
				.withText(menuItem.getText())
				.with(span);
		return li(link, makeDropdown(menuItem))
				.withClass("nav-item dropdown");
	}

	private DivTag makeDropdown(NavBarMenuItem menuItem){
		var links = menuItem.getSubItems(props.getRequest()).stream()
				.map(this::makeDropdownLink)
				.toArray(ContainerTag[]::new);
		return div(links)
				.withClass("dropdown-menu");
	}

	private ATag makeDropdownLink(NavBarMenuItem menuItem){
		String target = menuItem.openInNewTab() ? "_blank" : "";
		return a(menuItem.getText())
				.withClass("dropdown-item")
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString())
				.withTarget(target);
	}

	private LiTag makeNonDropdownMenuItem(NavBarMenuItem menuItem){
		var link = a(menuItem.getText())
				.withClass("nav-link")
				.withHref(menuItem.getAbsoluteHref(props.getRequest()).toString());
		return li(link)
				.withClass("nav-item");
	}

	private UlTag makeSignOut(){
		var link = a("Sign out")
				.withClass("nav-link")
				.withHref(props.getContextPath() + "/signout");
		var li = li(link)
				.withClass("nav-item");
		return ul(li)
				.withClass("navbar-nav");
	}

}
