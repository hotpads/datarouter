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
package io.datarouter.web.config;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.google.inject.Module;

import io.datarouter.httpclient.proxy.RequestProxySetter;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.FilesRoot.NoOpFilesRoot;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.BaseStoragePlugin;
import io.datarouter.storage.config.DatarouterStoragePlugin.DatarouterStoragePluginBuilder;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides.NoOpDatarouterSettingOverrides;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoGroup;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.SettingRootsSupplier;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.ordered.Ordered;
import io.datarouter.util.ordered.OrderedTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.DatarouterWebPlugin.DatarouterWebPluginBuilder;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.dispatcher.ServletParams;
import io.datarouter.web.filter.https.HttpsOnlyHttpsConfiguration;
import io.datarouter.web.homepage.DefaultHomepageRouteSet;
import io.datarouter.web.homepage.HomepageHandler;
import io.datarouter.web.homepage.HomepageRouteSet;
import io.datarouter.web.homepage.SimpleHomepageHandler;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterGuiceAppListenerServletContextListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.metriclinks.MetricLinkPage;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.user.authenticate.DatarouterAuthenticationFilter;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.CurrentSessionInfo.NoOpCurrentSessionInfo;
import io.datarouter.web.user.session.service.DatarouterRoleManager;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;
import io.datarouter.web.user.session.service.UserSessionService.NoOpUserSessionService;

public abstract class DatarouterWebWebappConfigBuilder<T extends DatarouterWebWebappConfigBuilder<T>>
implements WebappBuilder{

	// datarouter-storage
	private final String serviceName;
	private final String publicDomain;
	private final String privateDomain;
	private final String contextName;
	private final ServerTypes serverTypes;
	private final Set<String> subscribers;
	private final List<Class<? extends SettingRoot>> settingRoots;
	private final List<Class<? extends Dao>> daoClasses;
	protected final List<ClientId> defaultClientIds;
	private final List<FieldKeyOverrider> fieldKeyOverriders;
	private final List<Module> testModules;

	private Class<? extends FilesRoot> filesRoot;
	private Class<? extends ServerTypeDetector> serverTypeDetector;
	private Class<? extends ClientOptionsFactory> clientOptionsFactory;
	private Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactory;
	private Class<? extends DatarouterSettingOverrides> settingOverrides;

	// datarouter-web
	private final List<Ordered<Class<? extends DatarouterAppListener>>> appListenersOrdered;
	private final List<Class<? extends DatarouterAppListener>> appListenersUnordered;
	private final List<Class<? extends DatarouterAppListener>> appListenersUnorderedToExecuteLast;
	private final List<Ordered<Class<? extends DatarouterWebAppListener>>> webAppListenersOrdered;
	private final List<Class<? extends DatarouterWebAppListener>> webAppListenersUnordered;
	private final List<Ordered<Class<? extends BaseRouteSet>>> routeSetOrdered;
	private final List<Class<? extends BaseRouteSet>> routeSetsUnordered;
	private final List<Class<? extends TestableService>> testableServiceClasses;
	private final Map<String,Pair<String,Boolean>> documentationNamesAndLinks;
	private final List<Class<? extends DailyDigest>> dailyDigest;
	private final List<Class<? extends MetricLinkPage>> metricLinkPages;

	private Class<? extends RoleManager> roleManager;
	private Class<? extends CurrentSessionInfo> currentSessionInfo;
	private Class<? extends UserSessionService> userSessionService;
	private Class<? extends DatarouterAuthenticationConfig> authenticationConfig;
	private Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail;
	private Class<? extends HomepageRouteSet> homepageRouteSet = DefaultHomepageRouteSet.class;
	private Class<? extends HomepageHandler> homepageHandler;
	private String customStaticFileFilterRegex;
	private String nodeWidgetDatabeanExporterLink;
	private String nodeWidgetTableCountLink;
	private String serviceDescription;
	protected boolean useDatarouterAuth;
	private Class<? extends RequestProxySetter> requestProxy;
	private ZoneId defaultEmailDistributionListZoneId;
	private ZoneId dailyDigestEmailZoneId;

	// datarouter-web servlet
	private final List<Ordered<FilterParams>> filterParamsOrdered;
	private final List<FilterParams> filterParamsUnordered;
	private final List<NavBarItem> datarouterNavBarPluginItems;
	private final List<NavBarItem> appNavBarPluginItems;
	private final List<Class<? extends DynamicNavBarItem>> dynamicNavBarItems;
	private final List<ServletParams> servletParams;

	private boolean renderJspsUsingServletContainer;
	private Class<? extends HttpsConfiguration> httpsConfiguration;
	private Class<? extends Filter> authenticationFilter;
	private Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier;
	private final ServletContextListener log4jServletContextListener;

	// additional
	protected final List<Module> modules;
	protected final List<BaseStoragePlugin> storagePlugins;
	protected final List<BaseWebPlugin> webPlugins;
	protected final List<String> registeredPlugins;

	public static class DatarouterWebWebappBuilderImpl
	extends DatarouterWebWebappConfigBuilder<DatarouterWebWebappBuilderImpl>{

		public DatarouterWebWebappBuilderImpl(
				String serviceName,
				String publicDomain,
				String privateDomain,
				String contextName,
				ServerTypes serverTypes,
				List<ClientId> defaultClientIds,
				ServletContextListener log4jServletContextListener){
			super(
					serviceName,
					publicDomain,
					privateDomain,
					contextName,
					serverTypes,
					defaultClientIds,
					log4jServletContextListener);
		}

		@Override
		protected DatarouterWebWebappBuilderImpl getSelf(){
			return this;
		}

	}

	protected DatarouterWebWebappConfigBuilder(
			String serviceName,
			String publicDomain,
			String privateDomain,
			String contextName,
			ServerTypes serverTypes,
			List<ClientId> defaultClientIds,
			ServletContextListener log4jServletContextListener){
		this.serviceName = serviceName;
		this.publicDomain = publicDomain;
		this.privateDomain = privateDomain;
		this.contextName = contextName;
		this.serverTypes = serverTypes;
		this.defaultClientIds = defaultClientIds;
		this.log4jServletContextListener = log4jServletContextListener;

		// datarouter-storage
		this.settingRoots = new ArrayList<>();
		this.settingOverrides = NoOpDatarouterSettingOverrides.class;
		this.daoClasses = new ArrayList<>();
		this.fieldKeyOverriders = new ArrayList<>();
		this.testModules = new ArrayList<>();

		// datarouter-web
		this.roleManager = DatarouterRoleManager.class;
		this.userSessionService = NoOpUserSessionService.class;
		this.filesRoot = NoOpFilesRoot.class;
		this.currentSessionInfo = NoOpCurrentSessionInfo.class;
		this.appListenersOrdered = new ArrayList<>();
		this.appListenersUnordered = new ArrayList<>();
		this.appListenersUnorderedToExecuteLast = new ArrayList<>();
		this.webAppListenersOrdered = new ArrayList<>();
		this.webAppListenersUnordered = new ArrayList<>();
		this.authenticationConfig = null;
		this.subscribers = new HashSet<>();
		this.customStaticFileFilterRegex = null;
		this.homepageRouteSet = DefaultHomepageRouteSet.class;
		this.homepageHandler = SimpleHomepageHandler.class;
		this.documentationNamesAndLinks = new HashMap<>();
		this.dailyDigest = new ArrayList<>();
		this.metricLinkPages = new ArrayList<>();
		this.useDatarouterAuth = true;
		this.defaultEmailDistributionListZoneId = ZoneId.systemDefault();
		this.dailyDigestEmailZoneId = ZoneId.systemDefault();
		this.testableServiceClasses = new ArrayList<>();

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
		this.dynamicNavBarItems = new ArrayList<>();
		this.requestProxy = NoOpRequestProxySetter.class;

		// additional
		this.modules = new ArrayList<>();
		this.storagePlugins = new ArrayList<>();
		this.webPlugins = new ArrayList<>();
		this.modules.add(new DatarouterWebGuiceModule());
		this.registeredPlugins = new ArrayList<>();
	}

	protected abstract T getSelf();

	protected void onBuild(){
	}

	public DatarouterWebappConfig build(){
		onBuild();

		storagePlugins.forEach(this::addStoragePluginWithoutInstalling);
		webPlugins.forEach(this::addWebPluginWithoutInstalling);
		fieldKeyOverriders.forEach(FieldKeyOverrider::override);
		storagePlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		webPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(storagePlugins);
		modules.addAll(webPlugins);

		DatarouterWebPluginBuilder webPluginBuilder = new DatarouterWebPluginBuilder(
				serviceName,
				publicDomain,
				privateDomain,
				contextName,
				defaultClientIds)
				.setCustomStaticFileFilterRegex(customStaticFileFilterRegex)
				.setHomepageRouteSet(homepageRouteSet);
		addWebPluginWithoutInstalling(webPluginBuilder.getSimplePluginData());
		appListenersUnordered.addAll(appListenersUnorderedToExecuteLast);
		DatarouterWebPlugin webPlugin = webPluginBuilder
				.setFilesClass(filesRoot)
				.setDatarouterAuthConfig(authenticationConfig)
				.setCurrentSessionInfoClass(currentSessionInfo)
				.addSubscribers(subscribers)
				.setRoleManagerClass(roleManager)
				.setUserSesssionServiceClass(userSessionService)
				.setAppListenerClasses(OrderedTool.combine(appListenersOrdered, appListenersUnordered))
				.setWebAppListenerClasses(OrderedTool.combine(webAppListenersOrdered, webAppListenersUnordered))
				.setDatarouterNavBarMenuItems(datarouterNavBarPluginItems)
				.setAppNavBarMenuItems(appNavBarPluginItems)
				.setDatarouterUserExternalDetails(datarouterUserExternalDetail)
				.setAppNavBarRegistrySupplier(appNavBarRegistrySupplier)
				.setHomepageHandler(homepageHandler)
				.setCustomStaticFileFilterRegex(customStaticFileFilterRegex)
				.withRegisteredPlugins(registeredPlugins)
				.withNodeWidgetDatabeanExporterLink(nodeWidgetDatabeanExporterLink)
				.withNodeWidgetTableCountLink(nodeWidgetTableCountLink)
				.setServiceDescription(serviceDescription)
				.setDocumentationNamesAndLinks(documentationNamesAndLinks)
				.setTestableServiceClasses(testableServiceClasses)
				.setDynamicNavBarItems(dynamicNavBarItems)
				.setDailyDigest(dailyDigest)
				.setRequestProxy(requestProxy)
				.setMetricLinkPages(metricLinkPages)
				.setDefaultEmailDistributionListZoneId(defaultEmailDistributionListZoneId)
				.setDailyDigestEmailZoneId(dailyDigestEmailZoneId)
				.build();
		webPlugin.getStoragePlugins().forEach(this::addStoragePluginWithoutInstalling);
		webPlugin.getWebPlugins().forEach(this::addWebPluginWithoutInstalling);
		webPlugin.getStoragePlugins().stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		webPlugin.getWebPlugins().stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(webPlugin.getStoragePlugins());
		modules.addAll(webPlugin.getWebPlugins());

		DatarouterStoragePluginBuilder storagePluginBuilder = new DatarouterStoragePluginBuilder(
				serverTypes,
				defaultClientIds)
				.setSettingOverridesClass(settingOverrides)
				.setSettingRootsClass(new SettingRootsSupplier(settingRoots))
				.setClientOptionsFactoryClass(clientOptionsFactory)
				.setSchemaUpdateOptionsFactoryClass(schemaUpdateOptionsFactory);
		daoClasses.addAll(storagePluginBuilder.getSimplePluginData().getDatarouterStorageDaoClasses());
		storagePluginBuilder.addDaosClasses(daoClasses);
		if(serverTypeDetector != null){
			storagePluginBuilder.setServerTypeDetector(serverTypeDetector);
		}
		addStoragePluginWithoutInstalling(storagePluginBuilder.getSimplePluginData());
		// duplicates the one above at line 267? TODO remove?
		storagePluginBuilder.setSettingRootsClass(new SettingRootsSupplier(settingRoots));

		modules.add(webPlugin);
		modules.add(storagePluginBuilder.build());

		List<Class<? extends BaseRouteSet>> finalRouteSetClasses = OrderedTool.combine(routeSetOrdered,
				routeSetsUnordered);

		Map<FilterParamGrouping,List<Ordered<FilterParams>>> orderedFilterParams = Scanner.of(filterParamsOrdered)
				.groupBy(filterParam -> filterParam.item.grouping);
		Map<FilterParamGrouping,List<FilterParams>> unorderedFilterParams = Scanner.of(filterParamsUnordered)
				.groupBy(filterParam -> filterParam.grouping);
		List<FilterParams> finalFilterParams = new ArrayList<>();
		for(FilterParamGrouping group : FilterParamGrouping.values()){
			List<Ordered<FilterParams>> orderedInGroup = orderedFilterParams.getOrDefault(group, List.of());
			List<FilterParams> unorderedInGroup = unorderedFilterParams.getOrDefault(group, List.of());
			finalFilterParams.addAll(OrderedTool.combine(orderedInGroup, unorderedInGroup));
		}

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

		List<ServletContextListener> servletContextListeners = List.of(
				log4jServletContextListener,
				new DatarouterGuiceServletContextListener(modules),
				appListenerServletContextListener);

		return new DatarouterWebappConfig(modules, testModules, servletContextListeners);
	}

	/*-------------------------- add web plugins ----------------------------*/

	public T addStoragePlugin(BaseStoragePlugin storagePlugin){
		addStoragePluginInternal(storagePlugin);
		storagePlugin.getStoragePlugins().forEach(this::addStoragePluginInternal);
		return getSelf();
	}

	public T addWebPlugin(BaseWebPlugin webPlugin){
		addWebPluginInternal(webPlugin);
		webPlugin.getStoragePlugins().forEach(this::addStoragePluginInternal);
		webPlugin.getWebPlugins().forEach(this::addWebPluginInternal);
		return getSelf();
	}

	protected void addStoragePluginInternal(BaseStoragePlugin storagePlugin){
		boolean containsPlugin = storagePlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(storagePlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(storagePlugin.getName()
					+ " has already been added. It needs to be overridden");
		}
		storagePlugins.add(storagePlugin);
	}

	protected void addWebPluginInternal(BaseWebPlugin webPlugin){
		boolean containsPlugin = webPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(webPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(webPlugin.getName() + " has already been added. It needs to be overridden");
		}
		webPlugins.add(webPlugin);
	}

	public T overrideWebPlugin(BaseWebPlugin webPlugin){
		Optional<BaseWebPlugin> pluginToOverride = webPlugins.stream()
				.filter(plugin -> plugin.getName().equals(webPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(webPlugin.getName() + " has not been added yet. It cannot be overridden.");
		}
		webPlugins.remove(pluginToOverride.get());
		webPlugins.add(webPlugin);
		return getSelf();
	}

	protected T addWebPluginWithoutInstalling(BaseWebPlugin plugin){
		addStoragePluginWithoutInstalling(plugin);

		filterParamsOrdered.addAll(plugin.getFilterParamsOrdered());
		filterParamsUnordered.addAll(plugin.getFilterParamsUnordered());

		routeSetOrdered.addAll(plugin.getRouteSetsOrdered());
		routeSetsUnordered.addAll(plugin.getRouteSetsUnordered());

		appListenersOrdered.addAll(plugin.getAppListenersOrdered());
		appListenersUnordered.addAll(plugin.getAppListenersUnordered());
		appListenersUnorderedToExecuteLast.addAll(plugin.getAppListenersToExecuteLast());

		webAppListenersOrdered.addAll(plugin.getWebAppListenersOrdered());
		webAppListenersUnordered.addAll(plugin.getWebAppListenersUnordered());

		datarouterNavBarPluginItems.addAll(plugin.getDatarouterNavBarItems());
		appNavBarPluginItems.addAll(plugin.getAppNavBarItems());
		dynamicNavBarItems.addAll(plugin.getDynamicNavBarItems());

		fieldKeyOverriders.addAll(plugin.getFieldKeyOverrides());

		testableServiceClasses.addAll(plugin.getTestableServiceClasses());

		documentationNamesAndLinks.putAll(plugin.getDocumentationNamesAndLinks());

		dailyDigest.addAll(plugin.getDailyDigestRegistry());

		metricLinkPages.addAll(plugin.getMetricLinkPages());

		return getSelf();
	}

	protected T addStoragePluginWithoutInstalling(BaseStoragePlugin plugin){
		DaosModuleBuilder daosModule = plugin.getDaosModuleBuilder();
		daoClasses.addAll(daosModule.getDaoClasses());
		modules.add(daosModule);
		settingRoots.addAll(plugin.getSettingRoots());
		testModules.addAll(plugin.getTestModules());
		return getSelf();
	}

	/*---------------------------- web helpers ------------------------------*/

	public T addFieldKeyOverrider(FieldKeyOverrider fieldKeyOverrider){
		fieldKeyOverriders.add(fieldKeyOverrider);
		return getSelf();
	}

	public T addTestableService(Class<? extends TestableService> testableService){
		testableServiceClasses.add(testableService);
		return getSelf();
	}

	public T addAppListener(Class<? extends DatarouterAppListener> appListener){
		appListenersUnordered.add(appListener);
		return getSelf();
	}

	public T addAppListenerToExecuteLast(Class<? extends DatarouterAppListener> appListener){
		appListenersUnorderedToExecuteLast.add(appListener);
		return getSelf();
	}

	public T addModule(Module module){
		modules.add(module);
		return getSelf();
	}

	public T addSettingRoot(Class<? extends SettingRoot> settingRoot){
		this.settingRoots.add(settingRoot);
		return getSelf();
	}

	public T disableDatarouterAuth(){
		this.useDatarouterAuth = false;
		return getSelf();
	}

	public T setDatarouterUserExternalDetailService(
			Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail){
		this.datarouterUserExternalDetail = datarouterUserExternalDetail;
		return getSelf();
	}

	public T setAuthenticationConfig(
			Class<? extends DatarouterAuthenticationConfig> authenticationConfig){
		this.authenticationConfig = authenticationConfig;
		return getSelf();
	}

	public T setSettingOverrides(Class<? extends DatarouterSettingOverrides> settingOverrides){
		this.settingOverrides = settingOverrides;
		return getSelf();
	}

	public T setRoleManager(Class<? extends RoleManager> roleManager){
		this.roleManager = roleManager;
		return getSelf();
	}

	public T setUserSessionService(Class<? extends UserSessionService> userSessionService){
		this.userSessionService = userSessionService;
		return getSelf();
	}

	public T setFilesRoot(Class<? extends FilesRoot> filesRoot){
		this.filesRoot = filesRoot;
		return getSelf();
	}

	public T setCurrentSessionInfo(Class<? extends CurrentSessionInfo> currentSessionInfo){
		this.currentSessionInfo = currentSessionInfo;
		return getSelf();
	}

	public T setServerTypeDetector(Class<? extends ServerTypeDetector> serverTypeDetector){
		this.serverTypeDetector = serverTypeDetector;
		return getSelf();
	}

	public T addDao(Class<? extends BaseDao> dao){
		daoClasses.add(dao);
		return getSelf();
	}

	public T addDaoGroup(Class<? extends BaseDaoGroup> daoGroup){
		daoClasses.addAll(ReflectionTool.create(daoGroup).getDaoClasses());
		return getSelf();
	}

	@Deprecated
	public T addAdministratorEmail(String subscriber){
		addSubscriber(subscriber);
		return getSelf();
	}

	public T addSubscriber(String subscriber){
		subscribers.add(subscriber);
		return getSelf();
	}

	public T addFilter(String path, Class<? extends Filter> filter, FilterParamGrouping grouping){
		filterParamsUnordered.add(new FilterParams(false, path, filter, grouping));
		return getSelf();
	}

	public T addFilters(Collection<String> paths, Class<? extends Filter> filter, FilterParamGrouping grouping){
		paths.forEach(path -> addFilter(path, filter, grouping));
		return getSelf();
	}

	public T addRegexFilter(String regex, Class<? extends Filter> filter, FilterParamGrouping grouping){
		filterParamsUnordered.add(new FilterParams(true, regex, filter, grouping));
		return getSelf();
	}

	public T addRegexFilters(Collection<String> regexes, Class<? extends Filter> filter, FilterParamGrouping grouping){
		regexes.forEach(regex -> addRegexFilter(regex, filter, grouping));
		return getSelf();
	}

	public T addRootFilters(Class<? extends Filter> filter, FilterParamGrouping grouping){
		filterParamsUnordered.add(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, filter, grouping));
		return getSelf();
	}

	public T setHttpsConfiguration(Class<? extends HttpsConfiguration> httpsConfiguration){
		this.httpsConfiguration = httpsConfiguration;
		return getSelf();
	}

	public T setAuthenticationFilter(Class<? extends Filter> authenticationFilter){
		this.authenticationFilter = authenticationFilter;
		return getSelf();
	}

	public T addServlet(String path, Class<? extends HttpServlet> servlet){
		servletParams.add(new ServletParams(false, path, servlet));
		return getSelf();
	}

	public T addRegexServlet(String regex, Class<? extends HttpServlet> servlet){
		servletParams.add(new ServletParams(true, regex, servlet));
		return getSelf();
	}

	public T addRouteSet(Class<? extends BaseRouteSet> routeSet){
		routeSetsUnordered.add(routeSet);
		return getSelf();
	}

	public T setClientOptionsFactory(
			Class<? extends ClientOptionsFactory> clientOptionsFactory){
		this.clientOptionsFactory = clientOptionsFactory;
		return getSelf();
	}

	public T setSchemaUpdateOptionsFactory(
			Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactory){
		this.schemaUpdateOptionsFactory = schemaUpdateOptionsFactory;
		return getSelf();
	}

	public T renderJspsUsingServletContainer(){
		this.renderJspsUsingServletContainer = true;
		return getSelf();
	}

	public T setAppNavBarRegistry(
			Class<? extends AppNavBarRegistrySupplier> appNavBarRegistry){
		this.appNavBarRegistrySupplier = appNavBarRegistry;
		return getSelf();
	}

	public T setHomepageRouteSet(Class<? extends HomepageRouteSet> homepageRouteSet){
		this.homepageRouteSet = homepageRouteSet;
		return getSelf();
	}

	public T setHomepageHandler(Class<? extends HomepageHandler> homepageHandler){
		this.homepageHandler = homepageHandler;
		return getSelf();
	}

	public T withCustomStaticFileFilterRegex(String customStaticFileFilterRegex){
		this.customStaticFileFilterRegex = customStaticFileFilterRegex;
		return getSelf();
	}

	public T withNodeWidgetDatabeanExporterLink(String nodeWidgetDatabeanExporterLink){
		this.nodeWidgetDatabeanExporterLink = nodeWidgetDatabeanExporterLink;
		return getSelf();
	}

	public T withNodeWidgetTableCountLink(String nodeWidgetTableCountLink){
		this.nodeWidgetTableCountLink = nodeWidgetTableCountLink;
		return getSelf();
	}

	public T setServiceDescription(String serviceDescription){
		this.serviceDescription = serviceDescription;
		return getSelf();
	}

	public T addReadme(String name, String link){
		this.documentationNamesAndLinks.put(name, new Pair<>(link, false));
		return getSelf();
	}

	public T addSystemDocumentation(String name, String link){
		this.documentationNamesAndLinks.put(name, new Pair<>(link, true));
		return getSelf();
	}

	public T addAppNavBarItem(NavBarCategory category, PathNode path, String name){
		return addAppNavBarItem(category, path.toSlashedString(), name);
	}

	public T addAppNavBarItem(NavBarCategory category, String path, String name){
		this.appNavBarPluginItems.add(new NavBarItem(category, path, name));
		return getSelf();
	}

	public T addDatarouterNavBarItem(NavBarCategory category, String path, String name){
		this.datarouterNavBarPluginItems.add(new NavBarItem(category, path, name));
		return getSelf();
	}

	public T addDynamicNavBarItem(Class<? extends DynamicNavBarItem> dynamicNavBarItem){
		this.dynamicNavBarItems.add(dynamicNavBarItem);
		return getSelf();
	}

	public T addDailyDigest(Class<? extends DailyDigest> dailyDigest){
		this.dailyDigest.add(dailyDigest);
		return getSelf();
	}

	public T setRequestProxy(Class<? extends RequestProxySetter> requestProxy){
		this.requestProxy = requestProxy;
		return getSelf();
	}

	public T setDefaultEmailDistributionListZoneId(ZoneId defaultEmailDistributionListZoneId){
		this.defaultEmailDistributionListZoneId = defaultEmailDistributionListZoneId;
		return getSelf();
	}

	public T setDailyDigestEmailZoneId(ZoneId zoneId){
		this.dailyDigestEmailZoneId = zoneId;
		return getSelf();
	}

	public T addTestModule(Module testModule){
		this.testModules.add(testModule);
		return getSelf();
	}

}
