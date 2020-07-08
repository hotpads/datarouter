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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.web.dispatcher.BaseRouteSetTests;
import io.datarouter.web.dispatcher.DispatcherServletTestServlet;
import io.datarouter.web.user.role.DatarouterUserRole;

public class NavBarTests{

	private static final String SRC = "src";
	private static final String ALT = "alt";

	public static NavBar getNoAuthNavBar(){
		return new NavBar(SRC, ALT, false){};
	}

	public static NavBar getAuthNavBar(){
		return new NavBar(SRC, ALT, true){
			{
				dispatcherServlets = List.of(DispatcherServletTestServlet.getTestServlet());
			}
		};
	}

	public static NavBar getEmptyOptionalAuthNavBar(){
		return new NavBar(SRC, ALT, Optional.empty()){
			{
				dispatcherServlets = List.of(DispatcherServletTestServlet.getTestServlet());
			}
		};
	}

	@Test
	private void testConstruction(){
		NavBar navBar = getNoAuthNavBar();
		Assert.assertEquals(navBar.getLogoSrc(), SRC);
		Assert.assertEquals(navBar.getLogoAlt(), ALT);
		Assert.assertFalse(navBar.useDatarouterAuthentication);
		Assert.assertEquals(navBar.menuItems, new ArrayList<>());

		navBar = getAuthNavBar();
		Assert.assertTrue(navBar.useDatarouterAuthentication);

		navBar = getEmptyOptionalAuthNavBar();
		Assert.assertFalse(navBar.useDatarouterAuthentication);
	}

	@Test
	private void testGetDispatchRule(){
		NavBar noAuth = getNoAuthNavBar();
		Assert.assertEquals(noAuth.getDispatchRule(null), Optional.empty());
		Assert.assertEquals(noAuth.getDispatchRule(URI.create("")), Optional.empty());

		NavBar auth = getAuthNavBar();
		Assert.assertThrows(NullPointerException.class, () -> auth.getDispatchRule(null));

		for(DatarouterUserRole role : DatarouterUserRole.values()){
			String pathForRole = BaseRouteSetTests.getPathForRole(role);
			Assert.assertTrue(auth.getDispatchRule(URI.create(pathForRole))
					.orElse(null).getAllowedRoles().contains(role.getRole()));
		}
		String path = BaseRouteSetTests.ANON_PATH;
		Assert.assertTrue(auth.getDispatchRule(URI.create(path)).orElse(null)
				.getAllowAnonymous());
	}

}
