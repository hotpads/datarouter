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
import io.datarouter.util.ordered.Ordered;
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
 * RouteSets, Listeners, and FilterParams can be added with or without order. To specify the order, the "after" class
 * needs to be included. Both the ordered *after sorting) and the unordered lists will be combined at runtime.
 *
 * NavBars (both datarouter and app) are sorted alphabetically for all menu items and each of the sub menu items.
 */
public abstract class BaseWebPlugin extends BasePlugin{

	/*------------------------------ route sets -----------------------------*/

	private final List<Ordered<Class<? extends BaseRouteSet>>> orderedRouteSetClasses = new ArrayList<>();
	private final List<Class<? extends BaseRouteSet>> unorderedRouteSetClasses = new ArrayList<>();

	protected void addOrderedRouteSet(Class<? extends BaseRouteSet> routeSet, Class<? extends BaseRouteSet> after){
		orderedRouteSetClasses.add(new Ordered<>(routeSet, after));
	}

	protected void addUnorderedRouteSet(Class<? extends BaseRouteSet> routeSet){
		unorderedRouteSetClasses.add(routeSet);
	}

	public List<Ordered<Class<? extends BaseRouteSet>>> getOrderedRouteSetClasses(){
		return orderedRouteSetClasses;
	}

	public List<Class<? extends BaseRouteSet>> getUnorderedRouteSetClasses(){
		return unorderedRouteSetClasses;
	}

	/*------------------------------ listeners ------------------------------*/

	private final List<Ordered<Class<? extends DatarouterAppListener>>> orderedAppListeners = new ArrayList<>();
	private final List<Class<? extends DatarouterAppListener>> unorderedAppListeners = new ArrayList<>();

	private final List<Ordered<Class<? extends DatarouterWebAppListener>>> orderedWebListeners = new ArrayList<>();
	private final List<Class<? extends DatarouterWebAppListener>> unorderedWebListeners = new ArrayList<>();

	protected void addOrderedAppListener(Class<? extends DatarouterAppListener> listener,
			Class<? extends DatarouterAppListener> after){
		orderedAppListeners.add(new Ordered<>(listener, after));
	}

	protected void addUnorderedAppListener(Class<? extends DatarouterAppListener> listener){
		unorderedAppListeners.add(listener);
	}

	protected void addOrderedWebListener(Class<? extends DatarouterWebAppListener> listener,
			Class<? extends DatarouterWebAppListener> after){
		orderedWebListeners.add(new Ordered<>(listener, after));
	}

	protected void addUnorderedWebListener(Class<? extends DatarouterWebAppListener> listener){
		unorderedAppListeners.add(listener);
	}

	public List<Ordered<Class<? extends DatarouterAppListener>>> getOrderedAppListeners(){
		return orderedAppListeners;
	}

	public List<Class<? extends DatarouterAppListener>> getUnorderedAppListeners(){
		return unorderedAppListeners;
	}

	public List<Ordered<Class<? extends DatarouterWebAppListener>>> getOrderedWebAppListeners(){
		return orderedWebListeners;
	}

	public List<Class<? extends DatarouterWebAppListener>> getUnorderedWebAppListeners(){
		return unorderedWebListeners;
	}

	/*---------------------------- filter params ----------------------------*/

	private final List<Ordered<FilterParams>> orderedFilterParams = new ArrayList<>();
	private final List<FilterParams> unorderedFilterParams = new ArrayList<>();

	protected void addOrderedFilterParams(FilterParams filterParam, FilterParams after){
		orderedFilterParams.add(new Ordered<>(filterParam, after));
	}

	protected void addUnorderedFilterParams(FilterParams filterParam){
		unorderedFilterParams.add(filterParam);
	}

	public List<Ordered<FilterParams>> getOrderedFilterParams(){
		return orderedFilterParams;
	}

	public List<FilterParams> getUnorderedFilterParams(){
		return unorderedFilterParams;
	}

	/*------------------------------- nav bar--------------------------------*/

	private final List<NavBarItem> datarouterNavBarItems = new ArrayList<>();
	private final List<NavBarItem> appNavBarItems = new ArrayList<>();

	protected void addDatarouterNavBarItem(NavBarItem navBarItem){
		datarouterNavBarItems.add(navBarItem);
	}

	protected void addAppNavBarItem(NavBarItem navBarItem){
		appNavBarItems.add(navBarItem);
	}

	public List<NavBarItem> getDatarouterNavBarPluginItems(){
		return datarouterNavBarItems;
	}

	public List<NavBarItem> getAppNavBarPluginItems(){
		return appNavBarItems;
	}

}
