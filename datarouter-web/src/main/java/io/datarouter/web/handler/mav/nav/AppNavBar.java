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

import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;

public class AppNavBar extends NavBar{

	protected AppNavBar(Optional<DatarouterAuthenticationConfig> config){
		this("", "", config);
	}

	protected AppNavBar(String logoSrc, String logoAlt, Optional<DatarouterAuthenticationConfig> config){
		super(logoSrc, logoAlt, config);
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
		return new NavBarMenuItem("Admin",
				new NavBarMenuItem(config.getCreateUserPath(), "Create User", navBar),
				new NavBarMenuItem(config.getViewUsersPath(), "View Users", navBar),
				new NavBarMenuItem(config.getAccountManagerPath(), "Account Manager", navBar));
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
