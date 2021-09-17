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
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.inject.name.Names;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.client.DatarouterService.NoOpDatarouterService;
import io.datarouter.httpclient.client.service.ContextName;
import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.NoOpChangelogRecorder;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder.DatarouterSecretPluginBuilderImpl;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.DatarouterSubscribersSupplier.DatarouterSubscribers;
import io.datarouter.storage.config.guice.DatarouterStorageGuiceModule;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.ExceptionRecorder.NoOpExceptionRecorder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink.NoOpLatencyMonitoringGraphLink;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.AppNavBarPluginCreator;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier.NoOpAppNavBarRegistry;
import io.datarouter.web.navigation.AppPluginNavBarSupplier;
import io.datarouter.web.navigation.DatarouterNavBarCreator;
import io.datarouter.web.navigation.DatarouterNavBarSupplier;
import io.datarouter.web.navigation.DynamicNavBarItemRegistry;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.web.port.PortIdentifier;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier.NoOpDocumentationNamesAndLinks;
import io.datarouter.web.service.ServiceDescriptionSupplier;
import io.datarouter.web.service.ServiceDescriptionSupplier.NoOpServiceDescription;
import io.datarouter.web.test.TestableServiceClassRegistry;
import io.datarouter.web.test.TestableServiceClassRegistry.DefaultTestableServiceClassRegistry;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.BaseDatarouterSessionDao.NoOpDatarouterSessionDao;
import io.datarouter.web.user.authenticate.config.BaseDatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.BaseDatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.BaseDatarouterSamlDao.NoOpDatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.SamlRegistrar;
import io.datarouter.web.user.session.CurrentSessionInfo;
import io.datarouter.web.user.session.CurrentSessionInfo.NoOpCurrentSessionInfo;
import io.datarouter.web.user.session.service.DatarouterRoleManager;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;
import io.datarouter.web.user.session.service.UserSessionService.NoOpUserSessionService;

public class DatarouterWebGuiceModule extends BaseGuiceServletModule{

	@Override
	protected void configureServlets(){
		install(new DatarouterSecretPluginBuilderImpl().build().getAsDefaultBinderModule());
		install(new DatarouterStorageGuiceModule());
		bind(ServletContextSupplier.class).toInstance(new ServletContextProvider(getServletContext()));

		bind(JsonSerializer.class)
				.annotatedWith(Names.named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER))
				.to(GsonJsonSerializer.class);
		bind(PortIdentifier.class)
				.annotatedWith(Names.named(CompoundPortIdentifier.COMPOUND_PORT_IDENTIFIER))
				.to(CompoundPortIdentifier.class);

		bindDefault(DatarouterAuthenticationConfig.class, BaseDatarouterAuthenticationConfig.class);
		bindDefault(DatarouterService.class, NoOpDatarouterService.class);
		bindDefaultInstance(ContextName.class, new ContextName(""));

		bindDefault(BaseDatarouterSessionDao.class, NoOpDatarouterSessionDao.class);
		bindDefault(BaseDatarouterSamlDao.class, NoOpDatarouterSamlDao.class);

		bindDefault(ExceptionRecorder.class, NoOpExceptionRecorder.class);

		optionalBinder(AppNavBar.class);
		bindDefault(RoleManager.class, DatarouterRoleManager.class);
		optionalBinder(SamlRegistrar.class);
		bindDefault(SettingFinder.class, MemorySettingFinder.class);
		bindDefault(UserSessionService.class, NoOpUserSessionService.class);
		bindDefault(CurrentSessionInfo.class, NoOpCurrentSessionInfo.class);

		bindDefaultInstance(DatarouterSubscribersSupplier.class, new DatarouterSubscribers(Collections.emptySet()));
		bindDefault(LatencyMonitoringGraphLink.class, NoOpLatencyMonitoringGraphLink.class);
		bindDefaultInstance(DatarouterNavBarSupplier.class, new DatarouterNavBarCreator(Collections.emptyList()));
		bindDefaultInstance(AppPluginNavBarSupplier.class, new AppNavBarPluginCreator(Collections.emptyList()));
		bindDefault(AppNavBarRegistrySupplier.class, NoOpAppNavBarRegistry.class);
		bindDefaultInstance(DynamicNavBarItemRegistry.class, new DynamicNavBarItemRegistry(List.of()));
		bindDefault(ServiceDescriptionSupplier.class, NoOpServiceDescription.class);
		bindDefault(DocumentationNamesAndLinksSupplier.class, NoOpDocumentationNamesAndLinks.class);

		bindDefault(ChangelogRecorder.class, NoOpChangelogRecorder.class);
		bindDefaultInstance(TestableServiceClassRegistry.class, new DefaultTestableServiceClassRegistry(List.of()));

		// define as singleton for everybody
		bind(Gson.class).toInstance(GsonTool.GSON);

		bindDefaultInstance(DefaultEmailDistributionListZoneId.class,
				new DefaultEmailDistributionListZoneId(ZoneId.systemDefault()));
	}

	// allows this module to be installed multiple times
	@Override
	public boolean equals(Object that){
		if(that == null || getClass() != that.getClass()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		return DatarouterWebGuiceModule.class.hashCode();
	}

}
