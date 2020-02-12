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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.google.inject.Module;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.BasePlugin;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.DatarouterStoragePlugin.DatarouterStoragePluginBuilder;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides.NoOpDatarouterSettingOverrides;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoGroup;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.AdditionalSettingRoots;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.ordered.Ordered;
import io.datarouter.util.ordered.OrderedTool;
import io.datarouter.web.config.DatarouterWebPlugin.DatarouterWebDaoModule;
import io.datarouter.web.config.DatarouterWebPlugin.DatarouterWebPluginBuilder;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.dispatcher.ServletParams;
import io.datarouter.web.file.FilesRoot;
import io.datarouter.web.file.FilesRoot.NoOpFilesRoot;
import io.datarouter.web.filter.https.HttpsOnlyHttpsConfiguration;
import io.datarouter.web.homepage.HomepageHandler;
import io.datarouter.web.homepage.SimpleHomepageHandler;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterGuiceAppListenerServletContextListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.user.authenticate.DatarouterAuthenticationFilter;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.user.session.CurrentUserSessionInfo.NoOpCurrentUserSessionInfo;
import io.datarouter.web.user.session.service.DatarouterRoleManager;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;
import io.datarouter.web.user.session.service.UserSessionService.NoOpUserSessionService;

public class DatarouterWebWebappBuilder implements WebappBuilder{

	// datarouter-storage
	private final DatarouterProperties datarouterProperties;
	private final DatarouterService datarouterService;
	private final ServerTypes serverTypes;
	private final Set<String> additionalAdministrators;
	private final List<Class<? extends SettingRoot>> settingRoots;
	private final List<Class<? extends Dao>> daoClasses;
	protected final ClientId defaultClientId;
	private final List<FieldKeyOverrider> fieldKeyOverriders;

	private Class<? extends FilesRoot> filesRoot;
	private Class<? extends ServerTypeDetector> serverTypeDetector;
	private Class<? extends ClientOptionsFactory> clientOptionsFactory;
	private Class<? extends DatarouterSettingOverrides> settingOverrides;

	// datarouter-web
	private final List<Ordered<Class<? extends DatarouterAppListener>>> appListenersOrdered;
	private final List<Class<? extends DatarouterAppListener>> appListenersUnordered;
	private final List<Ordered<Class<? extends DatarouterWebAppListener>>> webAppListenersOrdered;
	private final List<Class<? extends DatarouterWebAppListener>> webAppListenersUnordered;
	private final List<Ordered<Class<? extends BaseRouteSet>>> routeSetOrdered;
	private final List<Class<? extends BaseRouteSet>> routeSetsUnordered;
	private final Set<String> additionalPermissionRequestEmails;

	private Class<? extends RoleManager> roleManager;
	private Class<? extends CurrentUserSessionInfo> currentUserSessionInfo;
	private Class<? extends UserSessionService> userSessionService;
	private Class<? extends DatarouterAuthenticationConfig> authenticationConfig;
	private Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail;
	private Class<? extends HomepageHandler> homepageHandler;
	private String customStaticFileFilterRegex;
	protected boolean useDatarouterAuth;

	// datarouter-web servlet
	private final List<Ordered<FilterParams>> filterParamsOrdered;
	private final List<FilterParams> filterParamsUnordered;
	private final List<NavBarItem> datarouterNavBarPluginItems;
	private final List<NavBarItem> appNavBarPluginItems;
	private final List<ServletParams> servletParams;

	private boolean renderJspsUsingServletContainer;
	private Class<? extends HttpsConfiguration> httpsConfiguration;
	private Class<? extends Filter> authenticationFilter;
	private Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier;
	private final ServletContextListener log4jServletContextListener;

	// additional
	protected final List<Module> modules;
	protected final List<BaseWebPlugin> webPlugins;
	protected final List<String> registeredPlugins;

	public DatarouterWebWebappBuilder(
			DatarouterService datarouterService,
			ServerTypes serverTypes,
			DatarouterProperties datarouterProperties,
			ClientId defaultClientId,
			ServletContextListener log4jServletContextListener){
		this.datarouterService = datarouterService;
		this.serverTypes = serverTypes;
		this.datarouterProperties = datarouterProperties;
		this.defaultClientId = defaultClientId;
		this.log4jServletContextListener = log4jServletContextListener;

		// datarouter-storage
		this.settingRoots = new ArrayList<>();
		this.settingOverrides = NoOpDatarouterSettingOverrides.class;
		this.daoClasses = new ArrayList<>();
		this.fieldKeyOverriders = new ArrayList<>();

		// datarouter-web
		this.roleManager = DatarouterRoleManager.class;
		this.userSessionService = NoOpUserSessionService.class;
		this.filesRoot = NoOpFilesRoot.class;
		this.currentUserSessionInfo = NoOpCurrentUserSessionInfo.class;
		this.appListenersOrdered = new ArrayList<>();
		this.appListenersUnordered = new ArrayList<>();
		this.webAppListenersOrdered = new ArrayList<>();
		this.webAppListenersUnordered = new ArrayList<>();
		this.authenticationConfig = null;
		this.additionalAdministrators = new HashSet<>();
		this.additionalPermissionRequestEmails = new HashSet<>();
		this.customStaticFileFilterRegex = null;
		this.homepageHandler = SimpleHomepageHandler.class;
		this.useDatarouterAuth = true;

		// datarouter-web servlet
		this.filterParamsOrdered = new ArrayList<>();
		this.filterParamsUnordered = new ArrayList<>();
		this.httpsConfiguration = HttpsOnlyHttpsConfiguration.class;
		this.authenticationFilter = DatarouterAuthenticationFilter.class;
		this.routeSetOrdered = new ArrayList<>();
		this.routeSetsUnordered = new ArrayList<>();
		this.servletParams = new ArrayList<>();
		this.datarouterNavBarPluginItems = new ArrayList<>();
		this.appNavBarPluginItems = new ArrayList<>();

		// additional
		this.modules = new ArrayList<>();
		this.webPlugins = new ArrayList<>();
		this.modules.add(new DatarouterWebGuiceModule());
		this.registeredPlugins = new ArrayList<>();
	}

	protected void onBuild(){
	}

	public DatarouterWebappConfig build(){
		onBuild();

		fieldKeyOverriders.forEach(FieldKeyOverrider::override);

		webPlugins.forEach(this::addWebPluginWithoutInstalling);
		webPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(webPlugins);

		DatarouterWebPluginBuilder webPluginBuilder = new DatarouterWebPluginBuilder(
				datarouterService,
				defaultClientId);
		addWebPluginWithoutInstalling(webPluginBuilder.getSimplePluginData());
		DatarouterWebPlugin webPlugin = webPluginBuilder
				.setFilesClass(filesRoot)
				.setDatarouterAuthConfig(authenticationConfig)
				.setCurrentUserSessionInfoClass(currentUserSessionInfo)
				.setAdditionalAdministrators(additionalAdministrators)
				.setAdditionalPermissionRequestEmails(additionalPermissionRequestEmails)
				.setRoleManagerClass(roleManager)
				.setUserSesssionServiceClass(userSessionService)
				.setAppListenerClasses(OrderedTool.combine(appListenersOrdered, appListenersUnordered))
				.setWebAppListenerClasses(OrderedTool.combine(webAppListenersOrdered, webAppListenersUnordered))
				.setDaoModule(new DatarouterWebDaoModule(defaultClientId))
				.setDatarouterNavBarMenuItems(datarouterNavBarPluginItems)
				.setAppNavBarMenuItems(appNavBarPluginItems)
				.setDatarouterUserExternalDetailClass(datarouterUserExternalDetail)
				.setAppNavBarRegistrySupplier(appNavBarRegistrySupplier)
				.setHomepageHandler(homepageHandler)
				.setCustomStaticFileFilterRegex(customStaticFileFilterRegex)
				.withRegisteredPlugins(registeredPlugins)
				.build();

		DatarouterStoragePluginBuilder storagePluginBuilder = new DatarouterStoragePluginBuilder(
				serverTypes,
				datarouterProperties)
				.setServerTypeDetector(serverTypeDetector)
				.setSettingOverridesClass(settingOverrides)
				.setAdditionalSettingRootsClass(new AdditionalSettingRoots(settingRoots))
				.setClientOptionsFactoryClass(clientOptionsFactory)
				.addDaosClasses(daoClasses);
		addStoragePluginWithoutInstalling(storagePluginBuilder.getSimplePluginData());
		storagePluginBuilder.setAdditionalSettingRootsClass(new AdditionalSettingRoots(settingRoots));

		modules.add(webPlugin);
		modules.add(storagePluginBuilder.build());

		List<Class<? extends BaseRouteSet>> finalRouteSetClasses = OrderedTool.combine(routeSetOrdered,
				routeSetsUnordered);

		List<FilterParams> finalFilterParams = OrderedTool.combine(filterParamsOrdered, filterParamsUnordered);

		Module defaultServletModule = new DatarouterServletGuiceModule(
				finalFilterParams,
				httpsConfiguration,
				authenticationFilter,
				finalRouteSetClasses,
				servletParams,
				renderJspsUsingServletContainer);
		modules.add(defaultServletModule);

		var appListenerServletContextListener = new DatarouterGuiceAppListenerServletContextListener(
				webPlugin.getFinalAppListeners(),
				webPlugin.getFinalWebAppListeners());

		List<ServletContextListener> servletContextListeners = Arrays.asList(
				log4jServletContextListener,
				new DatarouterGuiceServletContextListener(modules),
				appListenerServletContextListener);

		return new DatarouterWebappConfig(modules, servletContextListeners);
	}

	/*-------------------------- add web plugins ----------------------------*/

	public DatarouterWebWebappBuilder addWebPlugin(BaseWebPlugin webPlugin){
		boolean containsPlugin = webPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(webPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(webPlugin.getName() + " has already been added. It needs to be overridden");
		}
		webPlugins.add(webPlugin);
		return this;
	}

	public DatarouterWebWebappBuilder overrideWebPlugin(BaseWebPlugin webPlugin){
		Optional<BaseWebPlugin> pluginToOverride = webPlugins.stream()
				.filter(plugin -> plugin.getName().equals(webPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(webPlugin.getName() + " has not been added yet. It cannot be overriden.");
		}
		webPlugins.remove(pluginToOverride.get());
		webPlugins.add(webPlugin);
		return this;
	}

	protected DatarouterWebWebappBuilder addWebPluginWithoutInstalling(BaseWebPlugin plugin){
		addStoragePluginWithoutInstalling(plugin);

		filterParamsOrdered.addAll(plugin.getdFilterParamsOrdered());
		filterParamsUnordered.addAll(plugin.getFilterParamsUnordered());

		routeSetOrdered.addAll(plugin.getRouteSetsOrdered());
		routeSetsUnordered.addAll(plugin.getRouteSetsUnordered());

		appListenersOrdered.addAll(plugin.getAppListenersOrdered());
		appListenersUnordered.addAll(plugin.getAppListenersUnordered());

		webAppListenersOrdered.addAll(plugin.getWebAppListenersOrdered());
		webAppListenersUnordered.addAll(plugin.getWebAppListenersUnordered());

		datarouterNavBarPluginItems.addAll(plugin.getDatarouterNavBarItems());
		appNavBarPluginItems.addAll(plugin.getAppNavBarItems());

		return this;
	}

	protected DatarouterWebWebappBuilder addStoragePluginWithoutInstalling(BasePlugin plugin){
		DaosModuleBuilder daosModule = plugin.getDaosModuleBuilder();
		daoClasses.addAll(daosModule.getDaoClasses());
		modules.add(plugin.getDaosModuleBuilder());
		settingRoots.addAll(plugin.getSettingRoots());
		return this;
	}

	/*---------------------------- web helpers ------------------------------*/

	public DatarouterWebWebappBuilder addFieldKeyOverrider(FieldKeyOverrider fieldKeyOverrider){
		fieldKeyOverriders.add(fieldKeyOverrider);
		return this;
	}

	public DatarouterWebWebappBuilder addAppListener(Class<? extends DatarouterAppListener> appListener){
		appListenersUnordered.add(appListener);
		return this;
	}

	public DatarouterWebWebappBuilder addModule(Module module){
		modules.add(module);
		return this;
	}

	public DatarouterWebWebappBuilder addSettingRoot(Class<? extends SettingRoot> settingRoot){
		this.settingRoots.add(settingRoot);
		return this;
	}

	public DatarouterWebWebappBuilder disableDatarouterAuth(){
		this.useDatarouterAuth = false;
		return this;
	}

	public DatarouterWebWebappBuilder setDatarouterUserExternalDetailService(
			Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail){
		this.datarouterUserExternalDetail = datarouterUserExternalDetail;
		return this;
	}

	public DatarouterWebWebappBuilder setAuthenticationConfig(
			Class<? extends DatarouterAuthenticationConfig> authenticationConfig){
		this.authenticationConfig = authenticationConfig;
		return this;
	}

	public DatarouterWebWebappBuilder setSettingOverrides(Class<? extends DatarouterSettingOverrides> settingOverrides){
		this.settingOverrides = settingOverrides;
		return this;
	}

	public DatarouterWebWebappBuilder setRoleManager(Class<? extends RoleManager> roleManager){
		this.roleManager = roleManager;
		return this;
	}

	public DatarouterWebWebappBuilder setUserSessionService(Class<? extends UserSessionService> userSessionService){
		this.userSessionService = userSessionService;
		return this;
	}

	public DatarouterWebWebappBuilder setFilesRoot(Class<? extends FilesRoot> filesRoot){
		this.filesRoot = filesRoot;
		return this;
	}

	public DatarouterWebWebappBuilder setCurrentUserSessionInfo(
			Class<? extends CurrentUserSessionInfo> currentUserSessionInfo){
		this.currentUserSessionInfo = currentUserSessionInfo;
		return this;
	}

	public DatarouterWebWebappBuilder setServerTypeDetector(Class<? extends ServerTypeDetector> serverTypeDetector){
		this.serverTypeDetector = serverTypeDetector;
		return this;
	}

	@Deprecated
	public DatarouterWebWebappBuilder addRouter(Class<? extends Dao> router){
		daoClasses.add(router);
		return this;
	}

	public DatarouterWebWebappBuilder addDao(Class<? extends BaseDao> dao){
		daoClasses.add(dao);
		return this;
	}

	public DatarouterWebWebappBuilder addDaoGroup(Class<? extends BaseDaoGroup> daoGroup){
		daoClasses.addAll(ReflectionTool.create(daoGroup).getDaoClasses());
		return this;
	}

	public DatarouterWebWebappBuilder addAdministratorEmail(String additionalAdministrator){
		additionalAdministrators.add(additionalAdministrator);
		return this;
	}

	public DatarouterWebWebappBuilder addAdditionalPermissionRequestEmail(String additionalPermissionRequestEmail){
		additionalPermissionRequestEmails.add(additionalPermissionRequestEmail);
		return this;
	}

	public DatarouterWebWebappBuilder addFilter(String path, Class<? extends Filter> filter){
		filterParamsUnordered.add(new FilterParams(false, path, filter));
		return this;
	}

	public DatarouterWebWebappBuilder addFilters(Collection<String> paths, Class<? extends Filter> filter){
		paths.forEach(path -> addFilter(path, filter));
		return this;
	}

	public DatarouterWebWebappBuilder addRegexFilter(String regex, Class<? extends Filter> filter){
		filterParamsUnordered.add(new FilterParams(true, regex, filter));
		return this;
	}

	public DatarouterWebWebappBuilder addRegexFilters(Collection<String> regexes, Class<? extends Filter> filter){
		regexes.forEach(regex -> addRegexFilter(regex, filter));
		return this;
	}

	public DatarouterWebWebappBuilder addRootFilters(Class<? extends Filter> filter){
		filterParamsUnordered.add(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, filter));
		return this;
	}

	public DatarouterWebWebappBuilder setHttpsConfiguration(Class<? extends HttpsConfiguration> httpsConfiguration){
		this.httpsConfiguration = httpsConfiguration;
		return this;
	}

	public DatarouterWebWebappBuilder setAuthenticationFilter(Class<? extends Filter> authenticationFilter){
		this.authenticationFilter = authenticationFilter;
		return this;
	}

	public DatarouterWebWebappBuilder addServlet(String path, Class<? extends HttpServlet> servlet){
		servletParams.add(new ServletParams(false, path, servlet));
		return this;
	}

	public DatarouterWebWebappBuilder addRegexServlet(String regex, Class<? extends HttpServlet> servlet){
		servletParams.add(new ServletParams(true, regex, servlet));
		return this;
	}

	public DatarouterWebWebappBuilder addRouteSet(Class<? extends BaseRouteSet> routeSet){
		routeSetsUnordered.add(routeSet);
		return this;
	}

	public DatarouterWebWebappBuilder setClientOptionsFactory(
			Class<? extends ClientOptionsFactory> clientOptionsFactory){
		this.clientOptionsFactory = clientOptionsFactory;
		return this;
	}

	public DatarouterWebWebappBuilder renderJspsUsingServletContainer(){
		this.renderJspsUsingServletContainer = true;
		return this;
	}

	public DatarouterWebWebappBuilder setAppNavBarRegistry(
			Class<? extends AppNavBarRegistrySupplier> appNavBarRegistry){
		this.appNavBarRegistrySupplier = appNavBarRegistry;
		return this;
	}

	public DatarouterWebWebappBuilder setHomepageHandler(Class<? extends HomepageHandler> homepageHandler){
		this.homepageHandler = homepageHandler;
		return this;
	}

	public DatarouterWebWebappBuilder withCustomStaticFileFilterRegex(String customStaticFileFilterRegex){
		this.customStaticFileFilterRegex = customStaticFileFilterRegex;
		return this;
	}

}
