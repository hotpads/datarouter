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
package io.datarouter.joblet.config;

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
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.job.config.DatarouterJobWebappBuilder;
import io.datarouter.joblet.config.DatarouterJobletPlugin.DatarouterJobletDaoModule;
import io.datarouter.joblet.config.DatarouterJobletPlugin.DatarouterJobletPluginBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder.NoOpJobletExternalLinkBuilder;
import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeGroup;
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

public class DatarouterJobletWebappBuilder extends DatarouterJobWebappBuilder implements WebappBuilder{

	private final ClientId defaultQueueClientId;
	private final List<JobletType<?>> jobletTypes;
	private final List<BaseJobletPlugin> jobletPlugins;

	private Class<? extends JobletExternalLinkBuilder> jobletExternalLinkBuilder;

	public DatarouterJobletWebappBuilder(
			DatarouterService datarouterService,
			ServerTypes serverTypes,
			DatarouterProperties datarouterProperties,
			ClientId defaultClientId,
			ClientId defaultQueueClientId,
			ServletContextListener log4jServletContextListener){
		super(datarouterService, serverTypes, datarouterProperties, defaultClientId, log4jServletContextListener);
		this.defaultQueueClientId = defaultQueueClientId;
		this.jobletTypes = new ArrayList<>();
		this.jobletExternalLinkBuilder = NoOpJobletExternalLinkBuilder.class;
		this.jobletPlugins = new ArrayList<>();
	}

	@Override
	public DatarouterWebappConfig build(){
		jobletPlugins.forEach(this::addJobletPluginWithoutInstalling);
		jobletPlugins.stream()
				.map(BasePlugin::getName)
				.forEach(registeredPlugins::add);
		modules.addAll(jobletPlugins);

		DatarouterJobletPluginBuilder jobletPluginBuilder = new DatarouterJobletPluginBuilder(defaultClientId,
				defaultQueueClientId);
		addJobletPluginWithoutInstalling(jobletPluginBuilder.getSimplePluginData());
		DatarouterJobletPlugin jobletPlugin = jobletPluginBuilder
				.setJobletTypes(jobletTypes)
				.setDaoModule(new DatarouterJobletDaoModule(defaultClientId, defaultQueueClientId, defaultClientId))
				.setExternalLinkBuilderClass(jobletExternalLinkBuilder)
				.build();
		modules.add(jobletPlugin);
		return super.build();
	}

	/*------------------------- add joblet plugins --------------------------*/

	public DatarouterJobletWebappBuilder addJobletPlugin(BaseJobletPlugin jobletPlugin){
		boolean containsPlugin = jobletPlugins.stream()
				.anyMatch(plugin -> plugin.getName().equals(jobletPlugin.getName()));
		if(containsPlugin){
			throw new IllegalStateException(jobletPlugin.getName()
					+ " has already been added. It needs to be overridden");
		}
		jobletPlugins.add(jobletPlugin);
		return this;
	}

	public DatarouterJobletWebappBuilder overrideJobletPlugin(BaseJobletPlugin jobletPlugin){
		Optional<BaseJobletPlugin> pluginToOverride = jobletPlugins.stream()
				.filter(plugin -> plugin.getName().equals(jobletPlugin.getName()))
				.findFirst();
		if(pluginToOverride.isEmpty()){
			throw new IllegalStateException(jobletPlugin.getName()
					+ " has not been added yet. It cannot be overriden.");
		}
		jobletPlugins.remove(pluginToOverride.get());
		jobletPlugins.add(jobletPlugin);
		return this;
	}

	private DatarouterJobletWebappBuilder addJobletPluginWithoutInstalling(BaseJobletPlugin plugin){
		addJobPluginWithoutInstalling(plugin);
		jobletTypes.addAll(plugin.getJobletTypes());
		return this;
	}

	/*--------------------------- joblet helpers ----------------------------*/

	public DatarouterJobletWebappBuilder setJobletExternalLinkBuilder(
			Class<? extends JobletExternalLinkBuilder> jobletExternalLinkBuilder){
		this.jobletExternalLinkBuilder = jobletExternalLinkBuilder;
		return this;
	}

	public DatarouterJobletWebappBuilder addJobletTypeGroup(JobletTypeGroup jobletTypeGroup){
		this.jobletTypes.addAll(jobletTypeGroup.getAll());
		return this;
	}

	public DatarouterJobletWebappBuilder addJobletTypes(List<JobletType<?>> jobletTypes){
		this.jobletTypes.addAll(jobletTypes);
		return this;
	}

	/*-------------------------- add job plugins ----------------------------*/

	@Override
	public DatarouterJobletWebappBuilder addJobPlugin(BaseJobPlugin jobPlugin){
		super.addJobPlugin(jobPlugin);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder overrideJobPlugin(BaseJobPlugin jobPlugin){
		super.overrideJobPlugin(jobPlugin);
		return this;
	}

	@Override
	protected DatarouterJobletWebappBuilder addJobPluginWithoutInstalling(BaseJobPlugin jobPlugin){
		super.addJobPluginWithoutInstalling(jobPlugin);
		return this;
	}

	/*---------------------------- job helpers ------------------------------*/

	@Override
	public DatarouterJobletWebappBuilder addTriggerGroup(Class<? extends BaseTriggerGroup> triggerGroup){
		super.addTriggerGroup(triggerGroup);
		return this;
	}

	/*-------------------------- add web plugins ----------------------------*/

	@Override
	public DatarouterJobletWebappBuilder addWebPlugin(BaseWebPlugin webPlugin){
		super.addWebPlugin(webPlugin);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder overrideWebPlugin(BaseWebPlugin webPlugin){
		super.overrideWebPlugin(webPlugin);
		return this;
	}

	@Override
	protected DatarouterJobletWebappBuilder addWebPluginWithoutInstalling(BaseWebPlugin plugin){
		super.addWebPluginWithoutInstalling(plugin);
		return this;
	}

	@Override
	protected DatarouterJobletWebappBuilder addStoragePluginWithoutInstalling(BasePlugin plugin){
		super.addStoragePluginWithoutInstalling(plugin);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addFieldKeyOverrider(FieldKeyOverrider fieldKeyOverrider){
		super.addFieldKeyOverrider(fieldKeyOverrider);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addAppListener(Class<? extends DatarouterAppListener> appListener){
		super.addAppListener(appListener);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addModule(Module module){
		super.addModule(module);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addSettingRoot(Class<? extends SettingRoot> settingRoot){
		super.addSettingRoot(settingRoot);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder disableDatarouterAuth(){
		super.disableDatarouterAuth();
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setDatarouterUserExternalDetailService(
			Class<? extends DatarouterUserExternalDetailService> datarouterUserExternalDetail){
		super.setDatarouterUserExternalDetailService(datarouterUserExternalDetail);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setAuthenticationConfig(
			Class<? extends DatarouterAuthenticationConfig> authenticationConfig){
		super.setAuthenticationConfig(authenticationConfig);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setSettingOverrides(
			Class<? extends DatarouterSettingOverrides> settingOverrides){
		super.setSettingOverrides(settingOverrides);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setRoleManager(Class<? extends RoleManager> roleManager){
		super.setRoleManager(roleManager);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setUserSessionService(Class<? extends UserSessionService> userSessionService){
		super.setUserSessionService(userSessionService);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setFilesRoot(Class<? extends FilesRoot> filesRoot){
		super.setFilesRoot(filesRoot);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setCurrentSessionInfo(Class<? extends CurrentSessionInfo> currentSessionInfo){
		super.setCurrentSessionInfo(currentSessionInfo);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setServerTypeDetector(Class<? extends ServerTypeDetector> serverTypeDetector){
		super.setServerTypeDetector(serverTypeDetector);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addDao(Class<? extends BaseDao> dao){
		super.addDao(dao);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addDaoGroup(Class<? extends BaseDaoGroup> daoGroup){
		super.addDaoGroup(daoGroup);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addAdministratorEmail(String additionalAdministrator){
		super.addAdministratorEmail(additionalAdministrator);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addAdditionalPermissionRequestEmail(String additionalPermissionRequestEmail){
		super.addAdditionalPermissionRequestEmail(additionalPermissionRequestEmail);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addFilter(String path, Class<? extends Filter> filter){
		super.addFilter(path, filter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addFilters(Collection<String> paths, Class<? extends Filter> filter){
		super.addFilters(paths, filter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addRegexFilter(String regex, Class<? extends Filter> filter){
		super.addRegexFilter(regex, filter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addRegexFilters(Collection<String> regexes, Class<? extends Filter> filter){
		super.addRegexFilters(regexes, filter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addRootFilters(Class<? extends Filter> filter){
		super.addRootFilters(filter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setHttpsConfiguration(Class<? extends HttpsConfiguration> httpsConfiguration){
		super.setHttpsConfiguration(httpsConfiguration);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setAuthenticationFilter(Class<? extends Filter> authenticationFilter){
		super.setAuthenticationFilter(authenticationFilter);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addServlet(String path, Class<? extends HttpServlet> servlet){
		super.addServlet(path, servlet);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addRegexServlet(String regex, Class<? extends HttpServlet> servlet){
		super.addRegexServlet(regex, servlet);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder addRouteSet(Class<? extends BaseRouteSet> routeSet){
		super.addRouteSet(routeSet);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setClientOptionsFactory(
			Class<? extends ClientOptionsFactory> clientOptionsFactory){
		super.setClientOptionsFactory(clientOptionsFactory);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder renderJspsUsingServletContainer(){
		super.renderJspsUsingServletContainer();
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setAppNavBarRegistry(Class<? extends AppNavBarRegistrySupplier> appNavBar){
		super.setAppNavBarRegistry(appNavBar);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder setHomepageHandler(Class<? extends HomepageHandler> homepageHandler){
		super.setHomepageHandler(homepageHandler);
		return this;
	}

	@Override
	public DatarouterJobletWebappBuilder withCustomStaticFileFilterRegex(String customStaticFileFilterRegex){
		super.withCustomStaticFileFilterRegex(customStaticFileFilterRegex);
		return this;
	}

}
