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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.proxy.RequestProxySetter;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.FilesRoot.NoOpFilesRoot;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.DatarouterSubscribersSupplier.DatarouterSubscribers;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.setting.SettingBootstrapIntegrationService;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.browse.widget.NodeWidgetDatabeanExporterLinkSupplier;
import io.datarouter.web.browse.widget.NodeWidgetDatabeanExporterLinkSupplier.NodeWidgetDatabeanExporterLink;
import io.datarouter.web.browse.widget.NodeWidgetTableCountLinkSupplier;
import io.datarouter.web.browse.widget.NodeWidgetTableCountLinkSupplier.NodeWidgetTableCountLink;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestRegistry;
import io.datarouter.web.dispatcher.DatarouterWebDocsRouteSet;
import io.datarouter.web.dispatcher.DatarouterWebRouteSet;
import io.datarouter.web.dispatcher.FilterParamGrouping;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionHandlingConfig.NoOpExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.filter.StaticFileFilter;
import io.datarouter.web.filter.https.HttpsFilter;
import io.datarouter.web.filter.requestcaching.GuiceRequestCachingFilter;
import io.datarouter.web.homepage.DefaultHomepageRouteSet;
import io.datarouter.web.homepage.HomepageHandler;
import io.datarouter.web.homepage.HomepageRouteSet;
import io.datarouter.web.homepage.SimpleHomepageHandler;
import io.datarouter.web.listener.AppListenersClasses;
import io.datarouter.web.listener.AppListenersClasses.DatarouterAppListenersClasses;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterShutdownAppListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.listener.ExecutorsAppListener;
import io.datarouter.web.listener.HttpClientAppListener;
import io.datarouter.web.listener.InitializeEagerClientsAppListener;
import io.datarouter.web.listener.JspWebappListener;
import io.datarouter.web.listener.NoJavaSessionWebAppListener;
import io.datarouter.web.listener.TomcatWebAppNamesWebAppListener;
import io.datarouter.web.listener.WebAppListenersClasses;
import io.datarouter.web.listener.WebAppListenersClasses.DatarouterWebAppListenersClasses;
import io.datarouter.web.metriclinks.AppHandlerMetricLinkPage;
import io.datarouter.web.metriclinks.DatarouterHandlerMetricLinkPage;
import io.datarouter.web.metriclinks.MetricLinkPage;
import io.datarouter.web.metriclinks.MetricLinkPageRegistry;
import io.datarouter.web.metriclinks.MetricLinkPageRegistry.DefaultMetricLinkPageRegistry;
import io.datarouter.web.navigation.AppNavBarPluginCreator;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppPluginNavBarSupplier;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.DatarouterNavBarCreator;
import io.datarouter.web.navigation.DatarouterNavBarSupplier;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.DynamicNavBarItemRegistry;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.ReadmeDocsNavBarItem;
import io.datarouter.web.navigation.SystemDocsNavBarItem;
import io.datarouter.web.plugin.PluginRegistrySupplier;
import io.datarouter.web.plugin.PluginRegistrySupplier.PluginRegistry;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier.DefaultDocumentationNamesAndLinks;
import io.datarouter.web.service.ServiceDescriptionSupplier;
import io.datarouter.web.service.ServiceDescriptionSupplier.DatarouterServiceDescription;
import io.datarouter.web.test.TestableServiceClassRegistry;
import io.datarouter.web.test.TestableServiceClassRegistry.DefaultTestableServiceClassRegistry;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.DatarouterSessionDao.DatarouterSessionDaoParams;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.CurrentSessionInfo.NoOpCurrentSessionInfo;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;
import io.datarouter.web.user.session.service.UserSessionService.NoOpUserSessionService;

public class DatarouterWebPlugin extends BaseWebPlugin{

	private static final FilterParams DEFAULT_STATIC_FILE_FILTER_PARAMS = new FilterParams(
			false,
			DatarouterServletGuiceModule.ROOT_PATH,
			StaticFileFilter.class,
			FilterParamGrouping.DATAROUTER);

	public static final FilterParams REQUEST_CACHING_FILTER_PARAMS = new FilterParams(
			false,
			DatarouterServletGuiceModule.ROOT_PATH,
			GuiceRequestCachingFilter.class,
			FilterParamGrouping.DATAROUTER);

	private static final DatarouterWebPaths PATHS = new DatarouterWebPaths();

	private final DatarouterService datarouterService;
	private final Class<? extends FilesRoot> filesClass;
	private final Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass;
	private final Class<? extends CurrentSessionInfo> currentSessionInfoClass;
	private final Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
	private final Class<? extends ExceptionRecorder> exceptionRecorderClass;
	private final Set<String> subscribers;
	private final List<Class<? extends DatarouterAppListener>> appListenerClasses;
	private final List<Class<? extends DatarouterWebAppListener>> webAppListenerClasses;

	private final Class<? extends RoleManager> roleManagerClass;
	private final Class<? extends UserSessionService> userSessionServiceClass;
	private final List<NavBarItem> datarouterNavBarPluginItems;
	private final List<NavBarItem> appNavBarPluginItems;
	private final Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetailClass;
	private final Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier;
	private final Class<? extends HomepageRouteSet> homepageRouteSet;
	private final Class<? extends HomepageHandler> homepageHandler;
	private final List<String> registeredPlugins;
	private final String nodeWidgetDatabeanExporterLink;
	private final String nodeWidgetTableCountLink;
	private final String serviceDescription;
	private final Map<String,Pair<String,Boolean>> documentationNamesAndLinks;
	private final List<Class<? extends TestableService>> testableServiceClasses;
	private final List<Class<? extends DynamicNavBarItem>> dynamicNavBarItems;
	private final List<Class<? extends DailyDigest>> dailyDigest;
	private final Class<? extends RequestProxySetter> requestProxy;
	private final List<Class<? extends MetricLinkPage>> metricLinkPages;
	private final ZoneId defaultEmailDistributionListZoneId;

	// only used to get simple data from plugin
	private DatarouterWebPlugin(
			DatarouterWebDaoModule daosModuleBuilder,
			Class<? extends HomepageRouteSet> homepageRouteSet,
			String customStaticFileFilterRegex){
		this(null, null, null, null, null, null, null, null, null, null, null, daosModuleBuilder, null, null,
				null, null, homepageRouteSet, null, customStaticFileFilterRegex, null, null, null, null, null, null,
				null, null, null, null, null);
	}

	private DatarouterWebPlugin(
			DatarouterService datarouterService,
			Class<? extends FilesRoot> filesClass,
			Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass,
			Class<? extends CurrentSessionInfo> currentSessionInfoClass,
			Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass,
			Class<? extends ExceptionRecorder> exceptionRecorderClass,
			Set<String> subscribers,
			List<Class<? extends DatarouterAppListener>> appListenerClasses,
			List<Class<? extends DatarouterWebAppListener>> webAppListenerClasses,
			Class<? extends RoleManager> roleManagerClass,
			Class<? extends UserSessionService> userSessionServiceClass,
			DatarouterWebDaoModule daosModuleBuilder,
			List<NavBarItem> datarouterNavBarPluginItems,
			List<NavBarItem> appNavBarPluginItems,
			Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetailClass,
			Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier,
			Class<? extends HomepageRouteSet> homepageRouteSet,
			Class<? extends HomepageHandler> homepageHandler,
			String customStaticFileFilterRegex,
			List<String> registeredPlugins,
			String nodeWidgetDatabeanExporterLink,
			String nodeWidgetTableCountLink,
			String serviceDescription,
			Map<String,Pair<String,Boolean>> documentationNamesAndLinks,
			List<Class<? extends TestableService>> testableServiceClasses,
			List<Class<? extends DynamicNavBarItem>> dynamicNavBarItems,
			List<Class<? extends DailyDigest>> dailyDigest,
			Class<? extends RequestProxySetter> requestProxy,
			List<Class<? extends MetricLinkPage>> metricLinkPages,
			ZoneId defaultEmailDistributionListZoneId){
		addRouteSetOrdered(DatarouterWebRouteSet.class, null);
		addRouteSet(homepageRouteSet);
		addRouteSet(DatarouterWebDocsRouteSet.class);

		addSettingRoot(DatarouterWebSettingRoot.class);
		setDaosModule(daosModuleBuilder);

		addAppListenerOrdered(InitializeEagerClientsAppListener.class, null);
		addAppListenerOrdered(DatarouterShutdownAppListener.class, InitializeEagerClientsAppListener.class);
		addAppListenerOrdered(HttpClientAppListener.class, DatarouterShutdownAppListener.class);
		addAppListenerOrdered(ExecutorsAppListener.class, HttpClientAppListener.class);

		addWebListenerOrdered(TomcatWebAppNamesWebAppListener.class, null);
		addWebListenerOrdered(JspWebappListener.class, TomcatWebAppNamesWebAppListener.class);
		addWebListenerOrdered(NoJavaSessionWebAppListener.class, JspWebappListener.class);

		FilterParams staticFileFilterParams;

		if(customStaticFileFilterRegex == null){
			staticFileFilterParams = DEFAULT_STATIC_FILE_FILTER_PARAMS;
		}else{
			staticFileFilterParams = new FilterParams(
					true,
					customStaticFileFilterRegex,
					StaticFileFilter.class,
					FilterParamGrouping.DATAROUTER);
		}

		addFilterParamsOrdered(staticFileFilterParams, null);
		addFilterParamsOrdered(REQUEST_CACHING_FILTER_PARAMS, staticFileFilterParams);
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, HttpsFilter.class,
				FilterParamGrouping.DATAROUTER));

		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.executors, "Executors");
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.memory.view, "Server Status");
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.dailyDigest.viewActionable,
				"Daily Digest - Actionable");
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING, PATHS.datarouter.dailyDigest.viewSummary,
				"Daily Digest - Summary");

		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.tableConfiguration,
				"Custom Table Configs");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.filterParams, "Filters");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.listeners, "Listeners");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.routeSets, "RouteSets");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.properties,
				"Datarouter Properties");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.plugins, "Plugins");
		addDatarouterNavBarItem(DatarouterNavBarCategory.INFO, PATHS.datarouter.info.endpoints, "Endpoints");

		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.emailTest, "Email Test");
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.http.tester, "HTTP Tester");
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, PATHS.datarouter.http.dnsLookup, "DNS Lookup");


		addDynamicNavBarItem(ReadmeDocsNavBarItem.class);
		addDynamicNavBarItem(SystemDocsNavBarItem.class);

		addTestable(DatarouterWebBoostrapIntegrationService.class);
		addTestable(SettingBootstrapIntegrationService.class);

		addDatarouterGithubDocLink("datarouter-web");

		addMetricLinkPages(DatarouterHandlerMetricLinkPage.class);
		addMetricLinkPages(AppHandlerMetricLinkPage.class);

		//addStoragePlugin(new DatarouterEmailPlugin());

		this.datarouterService = datarouterService;
		this.filesClass = filesClass;
		this.authenticationConfigClass = authenticationConfigClass;
		this.currentSessionInfoClass = currentSessionInfoClass;
		this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
		this.exceptionRecorderClass = exceptionRecorderClass;
		this.subscribers = subscribers;
		this.appListenerClasses = appListenerClasses;
		this.webAppListenerClasses = webAppListenerClasses;
		this.roleManagerClass = roleManagerClass;
		this.userSessionServiceClass = userSessionServiceClass;
		this.datarouterNavBarPluginItems = datarouterNavBarPluginItems;
		this.appNavBarPluginItems = appNavBarPluginItems;
		this.datarouterUserExternalDetailClass = datarouterUserExternalDetailClass;
		this.appNavBarRegistrySupplier = appNavBarRegistrySupplier;
		this.homepageHandler = homepageHandler;
		this.homepageRouteSet = homepageRouteSet;
		this.registeredPlugins = registeredPlugins;
		this.nodeWidgetDatabeanExporterLink = nodeWidgetDatabeanExporterLink;
		this.nodeWidgetTableCountLink = nodeWidgetTableCountLink;
		this.serviceDescription = serviceDescription;
		this.documentationNamesAndLinks = documentationNamesAndLinks;
		this.testableServiceClasses = testableServiceClasses;
		this.dynamicNavBarItems = dynamicNavBarItems;
		this.dailyDigest = dailyDigest;
		this.requestProxy = requestProxy;
		this.metricLinkPages = metricLinkPages;
		this.defaultEmailDistributionListZoneId = defaultEmailDistributionListZoneId;
	}

	@Override
	public void configure(){
		bind(FilesRoot.class).to(filesClass);
		bindActualNullSafe(ExceptionRecorder.class, exceptionRecorderClass);
		bind(DatarouterService.class).toInstance(datarouterService);

		bindActualNullSafe(DatarouterAuthenticationConfig.class, authenticationConfigClass);
		bindActualNullSafe(CurrentSessionInfo.class, currentSessionInfoClass);
		bindDefault(ExceptionHandlingConfig.class, exceptionHandlingConfigClass);
		bindActualInstanceNullSafe(DatarouterSubscribersSupplier.class, new DatarouterSubscribers(subscribers));
		bindActualInstance(AppListenersClasses.class, new DatarouterAppListenersClasses(appListenerClasses));
		bindActualInstance(WebAppListenersClasses.class, new DatarouterWebAppListenersClasses(webAppListenerClasses));
		bindActualNullSafe(RoleManager.class, roleManagerClass);
		bindActualNullSafe(UserSessionService.class, userSessionServiceClass);
		bindActualInstanceNullSafe(DatarouterNavBarSupplier.class,
				new DatarouterNavBarCreator(datarouterNavBarPluginItems));
		bindActualInstanceNullSafe(AppPluginNavBarSupplier.class, new AppNavBarPluginCreator(appNavBarPluginItems));
		bindActualInstanceNullSafe(DynamicNavBarItemRegistry.class, new DynamicNavBarItemRegistry(dynamicNavBarItems));
		bindActualNullSafe(DatarouterUserExternalDetailService.class, datarouterUserExternalDetailClass);
		bindActualNullSafe(AppNavBarRegistrySupplier.class, appNavBarRegistrySupplier);
		bind(HomepageHandler.class).to(homepageHandler);
		bind(HomepageRouteSet.class).to(homepageRouteSet);
		bindActualInstance(PluginRegistrySupplier.class, new PluginRegistry(registeredPlugins));
		bindActualInstance(NodeWidgetDatabeanExporterLinkSupplier.class,
				new NodeWidgetDatabeanExporterLink(nodeWidgetDatabeanExporterLink));
		bindActualInstance(NodeWidgetTableCountLinkSupplier.class, new NodeWidgetTableCountLink(
				nodeWidgetTableCountLink));
		if(serviceDescription != null){
			bindActualInstance(ServiceDescriptionSupplier.class, new DatarouterServiceDescription(serviceDescription));
		}
		bindActualInstance(DocumentationNamesAndLinksSupplier.class,
				new DefaultDocumentationNamesAndLinks(documentationNamesAndLinks));
		bindActualInstance(TestableServiceClassRegistry.class,
				new DefaultTestableServiceClassRegistry(testableServiceClasses));
		bindActualInstance(DailyDigestRegistry.class, new DailyDigestRegistry(dailyDigest));
		bind(RequestProxySetter.class).to(requestProxy);
		bindActualInstance(MetricLinkPageRegistry.class, new DefaultMetricLinkPageRegistry(metricLinkPages));
		bindActualInstance(DefaultEmailDistributionListZoneId.class,
				new DefaultEmailDistributionListZoneId(defaultEmailDistributionListZoneId));
	}

	public List<Class<? extends DatarouterAppListener>> getFinalAppListeners(){
		return appListenerClasses;
	}

	public List<Class<? extends DatarouterWebAppListener>> getFinalWebAppListeners(){
		return webAppListenerClasses;
	}

	private <T> void bindActualNullSafe(Class<T> type, Class<? extends T> actualClass){
		if(actualClass != null){
			bindActual(type, actualClass);
		}
	}

	private <T> void bindActualInstanceNullSafe(Class<T> type, T actualInstance){
		if(actualInstance != null){
			bindActualInstance(type, actualInstance);
		}
	}

	public static class DatarouterWebDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterSessionClientId;

		public DatarouterWebDaoModule(List<ClientId> datarouterSessionClientId){
			this.datarouterSessionClientId = datarouterSessionClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DatarouterSessionDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterSessionDaoParams.class)
					.toInstance(new DatarouterSessionDaoParams(datarouterSessionClientId));
		}

	}

	public static class DatarouterWebPluginBuilder{

		private final DatarouterService datarouterService;
		private final List<ClientId> defaultClientIds;

		private Class<? extends FilesRoot> filesClass = NoOpFilesRoot.class;
		private Class<? extends DatarouterAuthenticationConfig> authenticationConfig;
		private Class<? extends CurrentSessionInfo> currentSessionInfo = NoOpCurrentSessionInfo.class;
		private Class<? extends ExceptionHandlingConfig> exceptionHandlingConfig = NoOpExceptionHandlingConfig.class;
		private Class<? extends ExceptionRecorder> exceptionRecorder;
		private Set<String> subscribers = new HashSet<>();
		private List<Class<? extends DatarouterAppListener>> appListenerClasses;
		private List<Class<? extends DatarouterWebAppListener>> webAppListenerClasses;
		private Class<? extends RoleManager> roleManagerClass;
		private Class<? extends UserSessionService> userSessionServiceClass = NoOpUserSessionService.class;
		private List<NavBarItem> datarouterNavBarPluginItems;
		private List<NavBarItem> appNavBarPluginItems;
		private Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail;
		private Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier;
		private Class<? extends HomepageRouteSet> homepageRouteSet = DefaultHomepageRouteSet.class;
		private Class<? extends HomepageHandler> homepageHandler = SimpleHomepageHandler.class;
		private String customStaticFileFilterRegex;
		private List<String> registeredPlugins = Collections.emptyList();
		private String nodeWidgetDatabeanExporterLink;
		private String nodeWidgetTableCountLink;
		private String serviceDescription;
		private Map<String,Pair<String,Boolean>> documentationNamesAndLinks = new HashMap<>();
		private List<Class<? extends TestableService>> testableServiceClasses = new ArrayList<>();
		private List<Class<? extends DynamicNavBarItem>> dynamicNavBarItems = new ArrayList<>();
		private List<Class<? extends DailyDigest>> dailyDigest = new ArrayList<>();
		private Class<? extends RequestProxySetter> requestProxy = NoOpRequestProxySetter.class;
		private final List<Class<? extends MetricLinkPage>> metricLinkPages = new ArrayList<>();
		private ZoneId defaultEmailDistributionListZoneId;

		public DatarouterWebPluginBuilder(DatarouterService datarouterService, List<ClientId> defaultClientIds){
			this.datarouterService = datarouterService;
			this.defaultClientIds = defaultClientIds;
		}

		public DatarouterWebPluginBuilder(DatarouterService datarouterService, ClientId defaultClientId){
			this(datarouterService, List.of(defaultClientId));
		}

		public DatarouterWebPluginBuilder setFilesClass(Class<? extends FilesRoot> filesClass){
			this.filesClass = filesClass;
			return this;
		}

		public DatarouterWebPluginBuilder setDatarouterAuthConfig(
				Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass){
			this.authenticationConfig = authenticationConfigClass;
			return this;
		}

		public DatarouterWebPluginBuilder setCurrentSessionInfoClass(
				Class<? extends CurrentSessionInfo> currentSessionInfoClass){
			this.currentSessionInfo = currentSessionInfoClass;
			return this;
		}

		public DatarouterWebPluginBuilder setExceptionHandlingClass(
				Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass){
			this.exceptionHandlingConfig = exceptionHandlingConfigClass;
			return this;
		}

		public DatarouterWebPluginBuilder setExceptionRecorderClass(
				Class<? extends ExceptionRecorder> exceptionRecorderClass){
			this.exceptionRecorder = exceptionRecorderClass;
			return this;
		}

		@Deprecated
		public DatarouterWebPluginBuilder setAdditionalAdministrators(Set<String> additionalAdministrators){
			this.subscribers = additionalAdministrators;
			return this;
		}

		public DatarouterWebPluginBuilder addSubscriber(String subscriber){
			this.subscribers.add(subscriber);
			return this;
		}

		public DatarouterWebPluginBuilder addSubscribers(Collection<String> subscribers){
			this.subscribers.addAll(subscribers);
			return this;
		}

		public DatarouterWebPluginBuilder setRoleManagerClass(Class<? extends RoleManager> roleManagerClass){
			this.roleManagerClass = roleManagerClass;
			return this;
		}

		public DatarouterWebPluginBuilder setUserSesssionServiceClass(
				Class<? extends UserSessionService> userSessionServiceClass){
			this.userSessionServiceClass = userSessionServiceClass;
			return this;
		}

		public DatarouterWebPluginBuilder setAppListenerClasses(
				List<Class<? extends DatarouterAppListener>> appListenerClasses){
			this.appListenerClasses = appListenerClasses;
			return this;
		}

		public DatarouterWebPluginBuilder setWebAppListenerClasses(
				List<Class<? extends DatarouterWebAppListener>> webAppListenerClasses){
			this.webAppListenerClasses = webAppListenerClasses;
			return this;
		}

		public DatarouterWebPluginBuilder setDatarouterNavBarMenuItems(
				List<NavBarItem> datarouterNavBarPluginItems){
			this.datarouterNavBarPluginItems = datarouterNavBarPluginItems;
			return this;
		}

		public DatarouterWebPluginBuilder setAppNavBarMenuItems(List<NavBarItem> appNavBarPluginItems){
			this.appNavBarPluginItems = appNavBarPluginItems;
			return this;
		}

		public DatarouterWebPluginBuilder setAppNavBarRegistrySupplier(
				Class<? extends AppNavBarRegistrySupplier> appNavBarRegistrySupplier){
			this.appNavBarRegistrySupplier = appNavBarRegistrySupplier;
			return this;
		}

		public DatarouterWebPluginBuilder setDatarouterUserExternalDetails(
				Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetailClass){
			this.datarouterUserExternalDetail = datarouterUserExternalDetailClass;
			return this;
		}

		public DatarouterWebPluginBuilder setHomepageRouteSet(Class<? extends HomepageRouteSet> homepageRouteSet){
			this.homepageRouteSet = homepageRouteSet;
			return this;
		}

		public DatarouterWebPluginBuilder setHomepageHandler(Class<? extends HomepageHandler> homepageHandlerClass){
			this.homepageHandler = homepageHandlerClass;
			return this;
		}

		public DatarouterWebPluginBuilder setCustomStaticFileFilterRegex(String customStaticFileFilterRegex){
			this.customStaticFileFilterRegex = customStaticFileFilterRegex;
			return this;
		}

		public DatarouterWebPluginBuilder withRegisteredPlugins(List<String> registeredPlugins){
			this.registeredPlugins = registeredPlugins;
			return this;
		}

		public DatarouterWebPluginBuilder withNodeWidgetDatabeanExporterLink(String nodeWidgetDatabeanExporterLink){
			this.nodeWidgetDatabeanExporterLink = nodeWidgetDatabeanExporterLink;
			return this;
		}

		public DatarouterWebPluginBuilder withNodeWidgetTableCountLink(String nodeWidgetTableCountLink){
			this.nodeWidgetTableCountLink = nodeWidgetTableCountLink;
			return this;
		}

		public DatarouterWebPluginBuilder setServiceDescription(String serviceDescription){
			this.serviceDescription = serviceDescription;
			return this;
		}

		public DatarouterWebPluginBuilder setDocumentationNamesAndLinks(
				Map<String,Pair<String,Boolean>> documentationNamesAndLinks){
			this.documentationNamesAndLinks = documentationNamesAndLinks;
			return this;
		}

		public DatarouterWebPluginBuilder setTestableServiceClasses(
				List<Class<? extends TestableService>> testableServiceClasses){
			this.testableServiceClasses = testableServiceClasses;
			return this;
		}

		public DatarouterWebPluginBuilder setDynamicNavBarItems(
				List<Class<? extends DynamicNavBarItem>> dynamicNavBarItems){
			this.dynamicNavBarItems.addAll(dynamicNavBarItems);
			return this;
		}

		public DatarouterWebPluginBuilder setDailyDigest(
				List<Class<? extends DailyDigest>> dailyDigest){
			this.dailyDigest.addAll(dailyDigest);
			return this;
		}

		public DatarouterWebPluginBuilder setRequestProxy(Class<? extends RequestProxySetter> requestProxy){
			this.requestProxy = requestProxy;
			return this;
		}

		public DatarouterWebPluginBuilder setMetricLinkPages(List<Class<? extends MetricLinkPage>> metricLinkPages){
			this.metricLinkPages.addAll(metricLinkPages);
			return this;
		}

		public DatarouterWebPluginBuilder setDefaultEmailDistributionListZoneId(ZoneId zoneId){
			this.defaultEmailDistributionListZoneId = zoneId;
			return this;
		}

		public DatarouterWebPlugin getSimplePluginData(){
			return new DatarouterWebPlugin(
					new DatarouterWebDaoModule(defaultClientIds),
					homepageRouteSet,
					customStaticFileFilterRegex);
		}

		public DatarouterWebPlugin build(){

			return new DatarouterWebPlugin(
					datarouterService,
					filesClass,
					authenticationConfig,
					currentSessionInfo,
					exceptionHandlingConfig,
					exceptionRecorder,
					subscribers,
					appListenerClasses,
					webAppListenerClasses,
					roleManagerClass,
					userSessionServiceClass,
					new DatarouterWebDaoModule(defaultClientIds),
					datarouterNavBarPluginItems,
					appNavBarPluginItems,
					datarouterUserExternalDetail,
					appNavBarRegistrySupplier,
					homepageRouteSet,
					homepageHandler,
					customStaticFileFilterRegex,
					registeredPlugins,
					nodeWidgetDatabeanExporterLink,
					nodeWidgetTableCountLink,
					serviceDescription,
					documentationNamesAndLinks,
					testableServiceClasses,
					dynamicNavBarItems,
					dailyDigest,
					requestProxy,
					metricLinkPages,
					defaultEmailDistributionListZoneId);
		}

	}

}
