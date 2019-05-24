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
package io.datarouter.web.handler.mav.nav;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSession.DatarouterSessionMock;

public class NavBarMenuItem{
	private final URI href;//this is what will appear in HTML
	private final URI path;//this is used to look up dispatch rules (no query params)
	private final String text;
	private final List<NavBarMenuItem> subItems;

	private Lazy<Optional<DispatchRule>> dispatchRule;

	public NavBarMenuItem(String text, NavBarMenuItem... subItems){
		this.href = URI.create("");
		this.path = href;
		this.text = text;
		this.subItems = Collections.unmodifiableList(Arrays.asList(subItems));
		this.dispatchRule = Lazy.of(Optional::empty);
	}

	public NavBarMenuItem(String path, String text, NavBar parentNavBar){
		this(path, "", text, parentNavBar);
	}

	public NavBarMenuItem(String path, String queryParamStr, String text, NavBar parentNavBar){
		this.href = URI.create(path + queryParamStr);
		this.path = URI.create(path);
		this.text = text;
		this.subItems = null;
		this.dispatchRule = Lazy.of(() -> parentNavBar.getDispatchRule(this.path));
	}

	public NavBarMenuItem(PathNode pathNode, String text, NavBar parentNavBar){
		this(pathNode.toSlashedString(), text, parentNavBar);
	}

	public NavBarMenuItem(PathNode pathNode, String queryParamString, String text, NavBar parentNavBar){
		this(pathNode.toSlashedString(), queryParamString, text, parentNavBar);
	}

	public Boolean isDropdown(){
		return subItems != null;
	}

	public boolean isAllowed(HttpServletRequest request){
		boolean hasAllowedSubItem = isDropdown() && !getSubItems(request).isEmpty();
		//if dispatchRule is not present, then assume permission
		boolean isAllowedItem = !isDropdown() && dispatchRule.get().map(rule -> rule.checkRoles(request)).orElse(true);
		return hasAllowedSubItem || isAllowedItem;
	}

	public URI getHref(){
		return href;
	}

	public URI getAbsoluteHref(HttpServletRequest request){
		if(href.isAbsolute()){
			return getHref();
		}
		return URI.create(request.getContextPath() + href.toString());
	}

	public String getText(){
		return text;
	}

	public List<NavBarMenuItem> getSubItems(HttpServletRequest request){
		return IterableTool.filter(subItems, item -> item.isAllowed(request));
	}

	public static class NavBarMenuItemTests{
		//test/mock objects
		private static final NavBar noAuthNavBar = NavBar.NavBarTests.getNoAuthNavBar();
		private static final NavBar authNavBar = NavBar.NavBarTests.getAuthNavBar();

		private static final HttpServletRequest anonRequest = DatarouterSessionMock.getAnonymousHttpServletRequest();
		private static final HttpServletRequest userRequest = DatarouterSession.DatarouterSessionMock
				.getHttpServletRequestWithSessionRoles(DatarouterUserRole.USER);
		private static final HttpServletRequest allDatarouterRolesRequest = DatarouterSessionMock
				.getAllDatarouterUserRolesHttpServletRequest();

		//constants
		private static final String ANON_REQ_HREF = BaseRouteSet.BaseRouteSetTests.ANON_PATH;
		private static final String USER_REQ_HREF = BaseRouteSet.BaseRouteSetTests.getPathForRole(DatarouterUserRole
				.USER);
		private static final String DR_ADMIN_REQ_HREF = BaseRouteSet.BaseRouteSetTests.getPathForRole(
				DatarouterUserRole.DATAROUTER_ADMIN);

		private static final String SINGLE_HREF = "href";
		private static final String SINGLE_TEXT = "text";
		private static final String PARENT_HREF = "";
		private static final String PARENT_TEXT = "parText";

		//helpers
		private static NavBarMenuItem getParentItem(NavBar navBar){
			return new NavBarMenuItem(PARENT_TEXT, getSingleItem(navBar));
		}

		public static NavBarMenuItem getSingleItem(NavBar navBar){
			return getSingleItemWithHref(SINGLE_HREF, navBar);
		}

		private static NavBarMenuItem getSingleItemWithHref(String href, NavBar navBar){
			return new NavBarMenuItem(href, SINGLE_TEXT, navBar);
		}

		//tests
		@Test
		private void testConstruction(){
			NavBarMenuItem parent = getParentItem(noAuthNavBar);
			Assert.assertEquals(parent.getHref().toString(), PARENT_HREF);
			Assert.assertEquals(parent.getText(), PARENT_TEXT);
			Assert.assertEquals(parent.subItems.size(), 1);
			Assert.assertFalse(parent.dispatchRule.get().isPresent());
			Assert.assertTrue(parent.isDropdown());

			NavBarMenuItem child = parent.subItems.get(0);
			Assert.assertEquals(child.getHref().toString(), SINGLE_HREF);
			Assert.assertEquals(child.getText(), SINGLE_TEXT);
			Assert.assertNull(child.subItems);
			Assert.assertFalse(child.dispatchRule.get().isPresent());
			Assert.assertFalse(child.isDropdown());

			//test lazy initialization when auth is used
			NavBarMenuItem auth = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
			Assert.assertTrue(auth.dispatchRule.get().isPresent());
			Assert.assertEquals(auth.dispatchRule.get().orElse(null).getPattern().toString(), auth.href.toString());
		}

		@Test
		private void testIsAllowed(){
			//request isn't checked when no auth or when no handler exists
			Assert.assertTrue(getSingleItem(noAuthNavBar).isAllowed(null));
			Assert.assertThrows(IllegalStateException.class, () -> getSingleItem(authNavBar).isAllowed(null));

			//everyone can access anon
			NavBarMenuItem anonItem = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
			Assert.assertTrue(anonItem.isAllowed(anonRequest));
			Assert.assertTrue(anonItem.isAllowed(userRequest));
			Assert.assertTrue(anonItem.isAllowed(allDatarouterRolesRequest));

			//anon can't access other roles, but correct roles can
			NavBarMenuItem userItem = getSingleItemWithHref(USER_REQ_HREF, authNavBar);
			Assert.assertFalse(userItem.isAllowed(anonRequest));
			Assert.assertTrue(userItem.isAllowed(userRequest));

			//having multiple roles, including the allowed one, also works
			Assert.assertTrue(userItem.isAllowed(allDatarouterRolesRequest));
		}

		@Test
		private void testGetSubItemsWithoutAuth(){
			Assert.assertThrows(NullPointerException.class, () -> new NavBarMenuItem("", (NavBarMenuItem[])null));

			NavBarMenuItem subItem1 = getSingleItem(noAuthNavBar);
			NavBarMenuItem subItem2 = getSingleItem(noAuthNavBar);
			NavBarMenuItem parentItem = new NavBarMenuItem("", subItem1, subItem2);

			Assert.assertEquals(parentItem.subItems.size(), 2);
			Assert.assertEquals(parentItem.subItems.get(0), subItem1);
			Assert.assertEquals(parentItem.subItems.get(1), subItem2);
			Assert.assertEquals(parentItem.getSubItems(null), parentItem.subItems);
		}

		@Test
		private void testGetSubItemsWithAuth(){
			NavBarMenuItem subItem1 = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
			NavBarMenuItem subItem2 = getSingleItemWithHref(USER_REQ_HREF, authNavBar);
			NavBarMenuItem subItem3 = getSingleItemWithHref(DR_ADMIN_REQ_HREF,
					authNavBar);
			NavBarMenuItem parentItem = new NavBarMenuItem("", subItem1, subItem2, subItem3);

			Assert.assertEquals(parentItem.getSubItems(anonRequest).size(), 1);
			Assert.assertEquals(parentItem.getSubItems(anonRequest).get(0), parentItem.subItems.get(0));
			Assert.assertEquals(parentItem.getSubItems(userRequest).size(), 2);
			Assert.assertEquals(parentItem.getSubItems(userRequest).get(1), parentItem.subItems.get(1));
			Assert.assertEquals(parentItem.getSubItems(allDatarouterRolesRequest).size(), 3);
			Assert.assertEquals(parentItem.getSubItems(allDatarouterRolesRequest), parentItem.subItems);
		}
	}
}
