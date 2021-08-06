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
package io.datarouter.web.handler.mav;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DatarouterNavBar;

//getters used by prelude.jspf
public class MavProperties{

	private final HttpServletRequest request;

	private final int cssVersion;
	private final int jsVersion;
	private final boolean isAdmin;
	private final boolean isProduction;
	private final Map<String,String> tomcatWebApps;
	private final Optional<AppNavBar> appNavBar;
	private final DatarouterNavBar datarouterNavBar;

	MavProperties(
			HttpServletRequest request,
			int cssVersion,
			int jsVersion,
			boolean isAdmin,
			Map<String,String> tomcatWebApps,
			Optional<AppNavBar> appNavBar,
			boolean isProduction,
			DatarouterNavBar datarouterNavBar){
		this.request = request;
		this.cssVersion = cssVersion;
		this.jsVersion = jsVersion;
		this.isAdmin = isAdmin;
		this.tomcatWebApps = tomcatWebApps;
		this.appNavBar = appNavBar;
		this.isProduction = isProduction;
		this.datarouterNavBar = datarouterNavBar;
	}

	public HttpServletRequest getRequest(){
		return request;
	}

	public String getContextPath(){
		return request.getContextPath();
	}

	public String getBasePath(){
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
	}

	public String getFullPath(){
		return getBasePath() + getContextPath();
	}

	public Integer getCssVersion(){
		return cssVersion;
	}

	public Integer getJsVersion(){
		return jsVersion;
	}

	public Boolean getIsAdmin(){
		return isAdmin;
	}

	public Map<String,String> getTomcatWebApps(){
		return tomcatWebApps;
	}

	public AppNavBar getNavBar(){
		return appNavBar.orElse(null);
	}

	public DatarouterNavBar getDatarouterNavBar(){
		return datarouterNavBar;
	}

	public Boolean getIsProduction(){
		return isProduction;
	}

	public boolean getIsDatarouterPage(){
		return request.getRequestURI().contains("datarouter");
	}

}
