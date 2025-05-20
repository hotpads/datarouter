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
import java.util.List;

import com.google.gson.Gson;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.role.DatarouterRoleManager;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.security.ExternalDatarouterAccountValidator;
import io.datarouter.auth.security.ExternalDatarouterAccountValidator.NoOpExternalDatarouterAccountValidator;
import io.datarouter.auth.session.CurrentSessionInfo;
import io.datarouter.auth.session.CurrentSessionInfo.NoOpCurrentSessionInfo;
import io.datarouter.auth.session.UserSessionService;
import io.datarouter.auth.session.UserSessionService.NoOpUserSessionService;
import io.datarouter.auth.storage.user.saml.BaseDatarouterSamlDao;
import io.datarouter.auth.storage.user.saml.BaseDatarouterSamlDao.NoOpDatarouterSamlDao;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao.NoOpDatarouterSessionDao;
import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.NoOpChangelogRecorder;
import io.datarouter.instrumentation.web.ContextName;
import io.datarouter.json.JsonSerializer;
import io.datarouter.relay.DatarouterRelayPlugin.DatarouterRelayPluginDefaults;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder.DatarouterSecretPluginBuilderImpl;
import io.datarouter.storage.config.guice.DatarouterStorageGuiceModule;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.config.service.PrivateDomain;
import io.datarouter.web.config.service.PublicDomain;
import io.datarouter.web.dispatcher.BackwardsCompatiblePayloadChecker;
import io.datarouter.web.dispatcher.BackwardsCompatiblePayloadChecker.NoOpBackwardsCompatiblePayloadChecker;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import io.datarouter.web.exception.ExceptionLinkBuilder.NoOpExceptionLinkBuilder;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.exception.ExceptionRecorder.NoOpExceptionRecorder;
import io.datarouter.web.handler.UserAgentTypeConfig;
import io.datarouter.web.handler.UserAgentTypeConfig.NoOpUserAgentTypeConfig;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.validator.HandlerAccountCallerValidator;
import io.datarouter.web.handler.validator.HandlerAccountCallerValidator.NoOpHandlerAccountCallerValidator;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink.NoOpLatencyMonitoringGraphLink;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier.NoOpAppNavBarRegistry;
import io.datarouter.web.user.authenticate.config.BaseDatarouterAuthenticationConfig;
import io.datarouter.web.util.http.TrustedProxy;

public class DatarouterWebGuiceModule extends BaseGuiceServletModule{

	@Override
	protected void configureServlets(){
		install(new DatarouterSecretPluginBuilderImpl().build().getAsDefaultBinderModule());
		install(new DatarouterRelayPluginDefaults());
		install(new DatarouterStorageGuiceModule());
		bind(ServletContextSupplier.class).toInstance(new ServletContextProvider(getServletContext()));

		bind(JsonSerializer.class)
				.annotatedWith(Names.named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER))
				.to(GsonJsonSerializer.class);

		bindDefault(DatarouterAuthenticationConfig.class, BaseDatarouterAuthenticationConfig.class);

		bindDefaultInstance(ServiceName.class, new ServiceName(""));
		bindDefaultInstance(PublicDomain.class, new PublicDomain(""));
		bindDefaultInstance(PrivateDomain.class, new PrivateDomain(""));
		bindDefaultInstance(ContextName.class, new ContextName(""));

		bindDefault(BaseDatarouterSessionDao.class, NoOpDatarouterSessionDao.class);
		bindDefault(BaseDatarouterSamlDao.class, NoOpDatarouterSamlDao.class);

		bindDefault(ExceptionRecorder.class, NoOpExceptionRecorder.class);
		bindDefault(ExceptionLinkBuilder.class, NoOpExceptionLinkBuilder.class);

		optionalBinder(AppNavBar.class);
		bindDefault(RoleManager.class, DatarouterRoleManager.class);
		bindDefault(SettingFinder.class, MemorySettingFinder.class);
		bindDefault(UserSessionService.class, NoOpUserSessionService.class);
		bindDefault(CurrentSessionInfo.class, NoOpCurrentSessionInfo.class);
		bindDefault(UserAgentTypeConfig.class, NoOpUserAgentTypeConfig.class);

		bindDefault(LatencyMonitoringGraphLink.class, NoOpLatencyMonitoringGraphLink.class);
		bindDefault(AppNavBarRegistrySupplier.class, NoOpAppNavBarRegistry.class);

		bindDefault(ChangelogRecorder.class, NoOpChangelogRecorder.class);

		// define as singleton for everybody
		bind(Gson.class).toProvider(GsonInjectionPreventionProvider.class);

		bindDefaultInstance(DefaultEmailDistributionListZoneId.class,
				new DefaultEmailDistributionListZoneId(ZoneId.systemDefault()));

		bindDefault(HandlerAccountCallerValidator.class, NoOpHandlerAccountCallerValidator.class);
		bindDefault(BackwardsCompatiblePayloadChecker.class, NoOpBackwardsCompatiblePayloadChecker.class);
		bindDefault(ExternalDatarouterAccountValidator.class, NoOpExternalDatarouterAccountValidator.class);

		bindDefaultInstance(TrustedProxy.class, new TrustedProxy(List.of(), List.of()));
	}

	// allows this module to be installed multiple times
	@Override
	public boolean equals(Object that){
		return that != null && getClass() == that.getClass();
	}

	@Override
	public int hashCode(){
		return DatarouterWebGuiceModule.class.hashCode();
	}

	private static class GsonInjectionPreventionProvider implements Provider<Gson>{
		@Override
		public Gson get(){
			throw new RuntimeException("I am here to prevent Gson injection.");
		}
	}

}
