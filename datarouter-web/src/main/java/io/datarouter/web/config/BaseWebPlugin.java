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
package io.datarouter.web.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.storage.config.BasePlugin;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.navigation.NavBarItem;

/**
 * BaseWebPlugin is an extension of BasePlugin. It allows all the features from datarouter-storage and the additional
 * features from datarouter-web to be easily configured.
 *
 * BaseWebPlugin brings in auto registration and dynamic configuration of RouteSets, AppListeners, FilterParams,
 * DatarouterNavBar items and AppNavBar items.
 *
 * NavBars (both datarouter and app) are sorted alphabetically for all menu items and each of the sub menu items.
 */
public abstract class BaseWebPlugin extends BasePlugin{

	private final List<Class<? extends BaseRouteSet>> routeSetClasses = new ArrayList<>();
	private final List<Class<? extends DatarouterAppListener>> appListeners = new ArrayList<>();
	private final List<Class<? extends DatarouterWebAppListener>> webListeners = new ArrayList<>();
	private final List<FilterParams> filterParams = new ArrayList<>();
	private final List<NavBarItem> datarouterNavBarItems = new ArrayList<>();
	private final List<NavBarItem> appNavBarItems = new ArrayList<>();

	protected void addRouteSet(Class<? extends BaseRouteSet> routeSet){
		routeSetClasses.add(routeSet);
	}

	protected void addAppListener(Class<? extends DatarouterAppListener> listener){
		appListeners.add(listener);
	}

	protected void addWebListener(Class<? extends DatarouterWebAppListener> listener){
		webListeners.add(listener);
	}

	protected void addFilterParams(FilterParams filterParam){
		filterParams.add(filterParam);
	}

	protected void addDatarouterNavBarItem(NavBarItem navBarItem){
		datarouterNavBarItems.add(navBarItem);
	}

	protected void addAppNavBarItem(NavBarItem navBarItem){
		appNavBarItems.add(navBarItem);
	}

	public List<Class<? extends BaseRouteSet>> getRouteSetClasses(){
		return routeSetClasses;
	}

	public List<Class<? extends DatarouterAppListener>> getAppListeners(){
		return appListeners;
	}

	public List<Class<? extends DatarouterWebAppListener>> getWebAppListeners(){
		return webListeners;
	}

	public List<FilterParams> getFilterParams(){
		return filterParams;
	}

	public List<NavBarItem> getDatarouterNavBarPluginItems(){
		return datarouterNavBarItems;
	}

	public List<NavBarItem> getAppNavBarPluginItems(){
		return appNavBarItems;
	}

}
