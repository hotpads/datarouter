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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.DispatcherServlet;
import io.datarouter.web.dispatcher.DispatcherServletListener;
import io.datarouter.web.dispatcher.DispatcherServletTestServlet;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.role.DatarouterUserRole;

public abstract class NavBar implements DispatcherServletListener{

	private final String logoSrc;
	private final String logoAlt;
	private List<NavBarMenuItem> menuItems;

	protected final boolean useDatarouterAuthentication;
	protected List<DispatcherServlet> dispatcherServlets = new ArrayList<>();

	private NavBar(String logoSrc, String logoAlt, boolean useDatarouterAuthentication){
		this.logoSrc = logoSrc;
		this.logoAlt = logoAlt;
		this.useDatarouterAuthentication = useDatarouterAuthentication;
		this.menuItems = new ArrayList<>();
	}

	protected NavBar(String logoSrc, String logoAlt, Optional<DatarouterAuthenticationConfig> authenticationConfig){
		this(logoSrc, logoAlt, authenticationConfig.map(DatarouterAuthenticationConfig::useDatarouterAuthentication)
				.orElse(false));
	}

	protected NavBar(String logoSrc, String logoAlt){
		this(logoSrc, logoAlt, Optional.empty());
	}

	public void addMenuItems(NavBarMenuItem... menuItems){
		this.menuItems.addAll(Arrays.asList(menuItems));
	}

	public String getLogoSrc(){
		return logoSrc;
	}

	public String getLogoAlt(){
		return logoAlt;
	}

	public List<NavBarMenuItem> getMenuItems(HttpServletRequest request){
		return IterableTool.include(menuItems, item -> item.isAllowed(request));
	}

	@Override
	public void onDispatcherServletInit(DispatcherServlet dispatcherServlet){
		dispatcherServlets.add(dispatcherServlet);
	}

	@Override
	public void onDispatcherServletDestroy(DispatcherServlet dispatcherServlet){
		dispatcherServlets.remove(dispatcherServlet);
	}

	protected Optional<DispatchRule> getDispatchRule(URI href){
		if(!useDatarouterAuthentication){
			return Optional.empty();
		}
		if(href.isAbsolute()){
			return Optional.empty();
		}
		return dispatcherServlets.stream()
				.map(servlet -> servlet.findRuleInContext(href.toString()))
				.flatMap(Optional::stream)
				.findFirst();
	}

	public static class NavBarTests{
		private static final String SRC = "src";
		private static final String ALT = "alt";

		public static NavBar getNoAuthNavBar(){
			return new NavBar(SRC, ALT, false){};
		}

		public static NavBar getAuthNavBar(){
			return new NavBar(SRC, ALT, true){
				{
					dispatcherServlets = Arrays.asList(DispatcherServletTestServlet.getTestServlet());
				}
			};
		}

		public static NavBar getEmptyOptionalAuthNavBar(){
			return new NavBar(SRC, ALT, Optional.empty()){
				{
					dispatcherServlets = Arrays.asList(DispatcherServletTestServlet.getTestServlet());
				}
			};
		}

		@Test
		private void testConstruction(){
			NavBar navBar = getNoAuthNavBar();
			Assert.assertEquals(navBar.logoSrc, SRC);
			Assert.assertEquals(navBar.logoAlt, ALT);
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
				String pathForRole = BaseRouteSet.BaseRouteSetTests.getPathForRole(role);
				Assert.assertTrue(auth.getDispatchRule(URI.create(pathForRole))
						.orElse(null).getAllowedRoles().contains(role.getRole()));
			}
			String path = BaseRouteSet.BaseRouteSetTests.ANON_PATH;
			Assert.assertTrue(auth.getDispatchRule(URI.create(path)).orElse(null)
					.getAllowAnonymous());
		}
	}
}
