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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.DispatcherServlet;
import io.datarouter.web.dispatcher.DispatcherServletListener;

public abstract class NavBar implements DispatcherServletListener{

	private final String logoSrc;
	private final String logoAlt;
	protected List<NavBarMenuItem> menuItems;

	protected final boolean useDatarouterAuthentication;
	protected List<DispatcherServlet> dispatcherServlets = new ArrayList<>();

	protected NavBar(String logoSrc, String logoAlt, boolean useDatarouterAuthentication){
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
		return Scanner.of(menuItems).include(item -> item.isAllowed(request)).list();
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
				.map(servlet -> servlet.findRuleInContext(href.getPath()))
				.flatMap(Optional::stream)
				.findFirst();
	}

}
