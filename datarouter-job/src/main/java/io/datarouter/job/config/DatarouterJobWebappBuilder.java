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
package io.datarouter.job.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.google.inject.Module;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.config.DatarouterJobPlugin.DatarouterJobDaoModule;
import io.datarouter.job.config.DatarouterJobPlugin.DatarouterJobPluginBuilder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.BasePlugin;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoGroup;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.config.DatarouterWebWebappBuilder;
import io.datarouter.web.config.DatarouterWebappConfig;
import io.datarouter.web.config.FieldKeyOverrider;
import io.datarouter.web.config.HttpsConfiguration;
import io.datarouter.web.config.WebappBuilder;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.file.FilesRoot;
import io.datarouter.web.homepage.HomepageHandler;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;

public class DatarouterJobWebappBuilder extends DatarouterWebWebappBuilder implements WebappBuilder{

	private final List<Class<? extends BaseTriggerGroup>> triggerGroups;
	private final List<BaseJobPlugin> jobPlugins;

	public DatarouterJobWebappBuilder(
			DatarouterService datarouterService,
			ServerTypes serverTypes,
			DatarouterProperties datarouterProperties,
			ClientId defaultClientId,
			ServletContextListener log4jServletContextListener){
		super(datarouterService, serverTypes, datarouterProperties, defaultClientId, log4jServletContextListener);
		this.triggerGroups = new ArrayList<>();
		this.jobPlugins = new ArrayList<>();
	}

	@Override
	public DatarouterWebappConfig build(){
		jobPlugins.forEach(this::addJobPluginWithoutInstalling);
		jobPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(jobPlugins);

		DatarouterJobPluginBuilder jobPluginBuilder = new DatarouterJobPluginBuilder(defaultClientId);
		addJobPluginWithoutInstalling(jobPluginBuilder.getSimplePluginData());
		DatarouterJobPlugin jobPlugin = jobPluginBuilder
				.setTriggerGroupClasses(triggerGroups)
				.setDaoModule(new DatarouterJobDaoModule(defaultClientId, defaultClientId))
				.build();

		modules.add(jobPlugin);
		return super.build();
	}

	/*-------------------------- add job plugins ----------------------------*/

	public DatarouterJobWebappBuilder addJobPlugin(BaseJobPlugin jobPlugin){
		boolean containsPlugin = jobPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(jobPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(jobPlugin.getName() + " has already been added. It needs to be overridden");
		}
		jobPlugins.add(jobPlugin);
		return this;
	}

	public DatarouterJobWebappBuilder overrideJobPlugin(BaseJobPlugin jobPlugin){
		Optional<BaseJobPlugin> pluginToOverride = jobPlugins.stream()
				.filter(plugin -> plugin.getName().equals(jobPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(jobPlugin.getName() + " has not been added yet. It cannot be overriden.");
		}
		jobPlugins.remove(pluginToOverride.get());
		jobPlugins.add(jobPlugin);
		return this;
	}

	protected DatarouterJobWebappBuilder addJobPluginWithoutInstalling(BaseJobPlugin plugin){
		addWebPluginWithoutInstalling(plugin);
		triggerGroups.addAll(plugin.getTriggerGroups());
		return this;
	}

	/*---------------------------- job helpers ------------------------------*/

	public DatarouterJobWebappBuilder addTriggerGroup(Class<? extends BaseTriggerGroup> triggerGroup){
		this.triggerGroups.add(triggerGroup);
		return this;
	}

	/*-------------------------- add web plugins ----------------------------*/

	@Override
	public DatarouterJobWebappBuilder addWebPlugin(BaseWebPlugin webPlugin){
		super.addWebPlugin(webPlugin);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder overrideWebPlugin(BaseWebPlugin webPlugin){
		super.overrideWebPlugin(webPlugin);
		return this;
	}

	@Override
	protected DatarouterJobWebappBuilder addWebPluginWithoutInstalling(BaseWebPlugin plugin){
		super.addWebPluginWithoutInstalling(plugin);
		return this;
	}

	@Override
	protected DatarouterJobWebappBuilder addStoragePluginWithoutInstalling(BasePlugin plugin){
		super.addStoragePluginWithoutInstalling(plugin);
		return this;
	}

	/*---------------------------- web helpers ------------------------------*/

	@Override
	public DatarouterJobWebappBuilder addFieldKeyOverrider(FieldKeyOverrider fieldKeyOverrider){
		super.addFieldKeyOverrider(fieldKeyOverrider);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addAppListener(Class<? extends DatarouterAppListener> appListener){
		super.addAppListener(appListener);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addModule(Module module){
		super.addModule(module);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addSettingRoot(Class<? extends SettingRoot> settingRoot){
		super.addSettingRoot(settingRoot);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder disableDatarouterAuth(){
		super.disableDatarouterAuth();
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setDatarouterUserExternalDetailService(
			Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail){
		super.setDatarouterUserExternalDetailService(datarouterUserExternalDetail);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setAuthenticationConfig(
			Class<? extends DatarouterAuthenticationConfig> authenticationConfig){
		super.setAuthenticationConfig(authenticationConfig);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setSettingOverrides(Class<? extends DatarouterSettingOverrides> settingOverrides){
		super.setSettingOverrides(settingOverrides);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setRoleManager(Class<? extends RoleManager> roleManager){
		super.setRoleManager(roleManager);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setUserSessionService(Class<? extends UserSessionService> userSessionService){
		super.setUserSessionService(userSessionService);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setFilesRoot(Class<? extends FilesRoot> filesRoot){
		super.setFilesRoot(filesRoot);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setCurrentSessionInfo(Class<? extends CurrentSessionInfo> currentSessionInfo){
		super.setCurrentSessionInfo(currentSessionInfo);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setServerTypeDetector(Class<? extends ServerTypeDetector> serverTypeDetector){
		super.setServerTypeDetector(serverTypeDetector);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addDao(Class<? extends BaseDao> dao){
		super.addDao(dao);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addDaoGroup(Class<? extends BaseDaoGroup> daoGroup){
		super.addDaoGroup(daoGroup);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addAdministratorEmail(String additionalAdministrator){
		super.addAdministratorEmail(additionalAdministrator);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addAdditionalPermissionRequestEmail(String additionalPermissionRequestEmail){
		super.addAdditionalPermissionRequestEmail(additionalPermissionRequestEmail);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addFilter(String path, Class<? extends Filter> filter){
		super.addFilter(path, filter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addFilters(Collection<String> paths, Class<? extends Filter> filter){
		super.addFilters(paths, filter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addRegexFilter(String regex, Class<? extends Filter> filter){
		super.addRegexFilter(regex, filter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addRegexFilters(Collection<String> regexes, Class<? extends Filter> filter){
		super.addRegexFilters(regexes, filter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addRootFilters(Class<? extends Filter> filter){
		super.addRootFilters(filter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setHttpsConfiguration(Class<? extends HttpsConfiguration> httpsConfiguration){
		super.setHttpsConfiguration(httpsConfiguration);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setAuthenticationFilter(Class<? extends Filter> authenticationFilter){
		super.setAuthenticationFilter(authenticationFilter);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addServlet(String path, Class<? extends HttpServlet> setvlet){
		super.addServlet(path, setvlet);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addRegexServlet(String regex, Class<? extends HttpServlet> setvlet){
		super.addRegexServlet(regex, setvlet);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder addRouteSet(Class<? extends BaseRouteSet> routeSet){
		super.addRouteSet(routeSet);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setClientOptionsFactory(
			Class<? extends ClientOptionsFactory> clientOptionsFactory){
		super.setClientOptionsFactory(clientOptionsFactory);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder renderJspsUsingServletContainer(){
		super.renderJspsUsingServletContainer();
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setAppNavBarRegistry(
			Class<? extends AppNavBarRegistrySupplier> appNavBarRegistry){
		super.setAppNavBarRegistry(appNavBarRegistry);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder setHomepageHandler(Class<? extends HomepageHandler> homepageHandler){
		super.setHomepageHandler(homepageHandler);
		return this;
	}

	@Override
	public DatarouterJobWebappBuilder withCustomStaticFileFilterRegex(String customStaticFileFilterRegex){
		super.withCustomStaticFileFilterRegex(customStaticFileFilterRegex);
		return this;
	}

	@Override
	public DatarouterWebWebappBuilder withDatabeanExporterLink(String databeanExporterLink){
		super.withDatabeanExporterLink(databeanExporterLink);
		return this;
	}

}
