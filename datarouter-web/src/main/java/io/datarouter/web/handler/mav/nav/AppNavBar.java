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

import java.util.Optional;

import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

public class AppNavBar extends NavBar{

	private final ServerTypeDetector serverTypeDetector;

	protected AppNavBar(Optional<DatarouterAuthenticationConfig> config, ServerTypeDetector serverTypeDetector){
		this("", "", config, serverTypeDetector);
	}

	protected AppNavBar(String logoSrc, String logoAlt, Optional<DatarouterAuthenticationConfig> config,
			ServerTypeDetector serverTypeDetector){
		super(logoSrc, logoAlt, config);
		this.serverTypeDetector = serverTypeDetector;
		config.ifPresent(conf -> {
			if(conf.useDatarouterAuthentication()){
				addDefaultItems(conf, this);
			}else{
				addNoDatarouterAutheticationItems(conf, this);
			}
		});
	}

	public void addDefaultItems(DatarouterAuthenticationConfig config, NavBar navBar){
		navBar.addMenuItems(getHomeButton(config, navBar), getUserMenu(config, navBar), getAdminMenu(config, navBar));
	}

	public NavBarMenuItem getHomeButton(DatarouterAuthenticationConfig config, NavBar navBar){
		return new NavBarMenuItem(config.getHomePath(), "Home", navBar);
	}

	public NavBarMenuItem getUserMenu(DatarouterAuthenticationConfig config, NavBar navBar){
		return new NavBarMenuItem("User",
				new NavBarMenuItem(config.getEditUserPath(), "Edit User", navBar),
				new NavBarMenuItem(config.getPermissionRequestPath(), "Permission Request", navBar),
				new NavBarMenuItem(config.getResetPasswordPath(), "Reset Password", navBar));
	}

	public NavBarMenuItem getAdminMenu(DatarouterAuthenticationConfig config, NavBar navBar){
		NavBarMenuItem createUser = new NavBarMenuItem(config.getCreateUserPath(), "Create User", navBar);
		NavBarMenuItem viewUsers = new NavBarMenuItem(config.getViewUsersPath(), "View Users", navBar);
		NavBarMenuItem accountManager = new NavBarMenuItem(config.getAccountManagerPath(), "Account Manager", navBar);
		if(serverTypeDetector.mightBeProduction()){
			return new NavBarMenuItem("Admin", viewUsers, accountManager);
		}
		return new NavBarMenuItem("Admin", createUser, viewUsers, accountManager);
	}

	public NavBarMenuItem getNoDatarouterAutheticationAdminMenu(DatarouterAuthenticationConfig config,
			NavBar navBar){
		return new NavBarMenuItem("Admin", new NavBarMenuItem(config.getAccountManagerPath(), "Account Manager",
				navBar));
	}

	public void addNoDatarouterAutheticationItems(DatarouterAuthenticationConfig config, NavBar navBar){
		navBar.addMenuItems(getHomeButton(config, navBar), getNoDatarouterAutheticationAdminMenu(config, navBar));
	}

}
