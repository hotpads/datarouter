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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.auth.role.DatarouterUserRole;
import io.datarouter.web.dispatcher.BaseRouteSetTests;

public class NavBarMenuItemTests{

	//test/mock objects
	private static final NavBar noAuthNavBar = NavBarTests.getNoAuthNavBar();
	private static final NavBar authNavBar = NavBarTests.getAuthNavBar();

	// TODO braydonh: uncomment these
//	private static final HttpServletRequest anonRequest = DatarouterSessionMock.getAnonymousHttpServletRequest();
//	private static final HttpServletRequest userRequest = DatarouterSession.DatarouterSessionMock
//			.getHttpServletRequestWithSessionRoles(DatarouterUserRole.USER);
//	private static final HttpServletRequest allDatarouterRolesRequest = DatarouterSessionMock
//			.getAllDatarouterUserRolesHttpServletRequest();

	// constants
	private static final String ANON_REQ_HREF = BaseRouteSetTests.ANON_PATH;
	private static final String USER_REQ_HREF = BaseRouteSetTests.getPathForRole(DatarouterUserRole.USER);
	private static final String DR_ADMIN_REQ_HREF = BaseRouteSetTests.getPathForRole(
			DatarouterUserRole.DATAROUTER_ADMIN);

	private static final String SINGLE_HREF = "href";
	private static final String SINGLE_TEXT = "text";
	private static final String PARENT_HREF = "";
	private static final String PARENT_TEXT = "parText";

	// helpers
	private static NavBarMenuItem getParentItem(NavBar navBar){
		return new NavBarMenuItem(PARENT_TEXT, List.of(getSingleItem(navBar)));
	}

	public static NavBarMenuItem getSingleItem(NavBar navBar){
		return getSingleItemWithHref(SINGLE_HREF, navBar);
	}

	private static NavBarMenuItem getSingleItemWithHref(String href, NavBar navBar){
		return new NavBarMenuItem(href, SINGLE_TEXT, false, navBar);
	}

	// tests
	@Test
	private void testConstruction(){
		NavBarMenuItem parent = getParentItem(noAuthNavBar);
		Assert.assertEquals(parent.getHref().toString(), PARENT_HREF);
		Assert.assertEquals(parent.getText(), PARENT_TEXT);
		Assert.assertEquals(parent.subItems.size(), 1);
		Assert.assertFalse(parent.dispatchRule.get().isPresent());
		Assert.assertTrue(parent.isDropdown());

		NavBarMenuItem child = parent.subItems.getFirst();
		Assert.assertEquals(child.getHref().toString(), SINGLE_HREF);
		Assert.assertEquals(child.getText(), SINGLE_TEXT);
		Assert.assertNull(child.subItems);
		Assert.assertFalse(child.dispatchRule.get().isPresent());
		Assert.assertFalse(child.isDropdown());

		//test lazy initialization when auth is used
		NavBarMenuItem auth = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
		Assert.assertTrue(auth.dispatchRule.get().isPresent());
		Assert.assertEquals(auth.dispatchRule.get().orElse(null).getPattern().toString(), auth.getHref().toString());
	}

//	@Test
//	private void testIsAllowed(){
//		// request isn't checked when no auth or when no handler exists
//		Assert.assertTrue(getSingleItem(noAuthNavBar).isAllowed(null));
//
//		// everyone can access anon
//		NavBarMenuItem anonItem = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
//		Assert.assertTrue(anonItem.isAllowed(anonRequest));
//		Assert.assertTrue(anonItem.isAllowed(userRequest));
//		Assert.assertTrue(anonItem.isAllowed(allDatarouterRolesRequest));
//
//		// anon can't access other roles, but correct roles can
//		NavBarMenuItem userItem = getSingleItemWithHref(USER_REQ_HREF, authNavBar);
//		Assert.assertFalse(userItem.isAllowed(anonRequest));
//		Assert.assertTrue(userItem.isAllowed(userRequest));
//
//		// having multiple roles, including the allowed one, also works
//		Assert.assertTrue(userItem.isAllowed(allDatarouterRolesRequest));
//	}

	@Test
	private void testGetSubItemsWithoutAuth(){
		NavBarMenuItem subItem1 = getSingleItem(noAuthNavBar);
		NavBarMenuItem subItem2 = getSingleItem(noAuthNavBar);
		NavBarMenuItem parentItem = new NavBarMenuItem("", List.of(subItem1, subItem2));

		Assert.assertEquals(parentItem.subItems.size(), 2);
		Assert.assertEquals(parentItem.subItems.get(0), subItem1);
		Assert.assertEquals(parentItem.subItems.get(1), subItem2);
		Assert.assertEquals(parentItem.getSubItems(null), parentItem.subItems);
	}

//	@Test
//	private void testGetSubItemsWithAuth(){
//		NavBarMenuItem subItem1 = getSingleItemWithHref(ANON_REQ_HREF, authNavBar);
//		NavBarMenuItem subItem2 = getSingleItemWithHref(USER_REQ_HREF, authNavBar);
//		NavBarMenuItem subItem3 = getSingleItemWithHref(DR_ADMIN_REQ_HREF, authNavBar);
//		NavBarMenuItem parentItem = new NavBarMenuItem("", List.of(subItem1, subItem2, subItem3));
//
//		Assert.assertEquals(parentItem.getSubItems(anonRequest).size(), 1);
//		Assert.assertEquals(parentItem.getSubItems(anonRequest).get(0), parentItem.subItems.get(0));
//		Assert.assertEquals(parentItem.getSubItems(userRequest).size(), 2);
//		Assert.assertEquals(parentItem.getSubItems(userRequest).get(1), parentItem.subItems.get(1));
//		Assert.assertEquals(parentItem.getSubItems(allDatarouterRolesRequest).size(), 3);
//		Assert.assertEquals(parentItem.getSubItems(allDatarouterRolesRequest), parentItem.subItems);
//	}

}
